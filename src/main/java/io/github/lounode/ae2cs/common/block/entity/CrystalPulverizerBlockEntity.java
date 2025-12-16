package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.api.util.ForgeEnergyAdapterUpgrade;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CrystalPulverizerBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject
{
    /**
     * 基础能量消耗，每tick 200AE，每多一个加速卡，则此数值翻倍，同时机器运行速率也翻倍。
     * <p>
     * 目前最大四张加速卡，则最大速率为16倍，同时最大每tick消耗也为16倍，即3200AE每tick
     */
    private static final double BASIC_ENERGY_COST_PER_TICK = 200;

    /**
     * 输入仓
     */
    private final AppEngInternalInventory inputInv = new AppEngInternalInventory(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            needRefreshRecipeState = true;
            setChanged();
        }
    };

    /**
     * 输出仓
     */
    private final AppEngInternalInventory outputInv = new AppEngInternalInventory(4)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    /**
     * 能力暴露-输入
     */
    FilteredInternalInventory filteredInputInv = new FilteredInternalInventory(inputInv, new IAEItemFilter()
    {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount)
        {
            return false;
        }
    });

    /**
     * 能力暴露-输出
     */
    FilteredInternalInventory filteredOutputInv = new FilteredInternalInventory(outputInv, new IAEItemFilter()
    {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
        {
            return false;
        }
    });

    /**
     * 能力暴露-结合库存
     */
    CombinedInternalInventory combinedInv = new CombinedInternalInventory(filteredInputInv, filteredOutputInv);

    /**
     * 升级仓
     */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK,
            4, this::saveChanges);

    /**
     * 当前执行的配方
     */
    @Nullable
    private RecipeHolder<CrystalPulverizerRecipe> activeRecipe;

    /**
     * 当前执行配方的id，在重新加载时保证机器运行进展不会因为配方检查被刷新掉
     */
    @Nullable
    private ResourceLocation activeRecipeId;

    /**
     * 该配方需要的总时间（tick）
     */
    private int activeRecipeTime = 0;

    /**
     * 当前配方运行时间
     */
    private int recipeProgress = 0;

    /**
     * 是否需要更新配方状态
     */
    private boolean needRefreshRecipeState = true;

    public CrystalPulverizerBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.CRYSTAL_PULVERIZER_BLOCK_ENTITY.get(), pos, blockState, 80000);

        getMainNode().setIdlePowerUsage(0);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.CRYSTAL_PULVERIZER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AECSBlockEntities.CRYSTAL_PULVERIZER_BLOCK_ENTITY.get(),
                (be, direction) -> new ForgeEnergyAdapterUpgrade(be, AccessRestriction.WRITE)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AECSBlockEntities.CRYSTAL_PULVERIZER_BLOCK_ENTITY.get(),
                (be, direction) -> be.combinedInv.toItemHandler()
        );
    }

    public AppEngInternalInventory getInputInv()
    {
        return inputInv;
    }

    public AppEngInternalInventory getOutputInv()
    {
        return outputInv;
    }

    public int getRecipeProgress()
    {
        return recipeProgress;
    }

    public int getActiveRecipeTime()
    {
        return activeRecipeTime;
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return false;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return AccessRestriction.WRITE;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();

        if (getLevel() == null || getLevel().isClientSide()) return;

        // 1) 更新/确认活动配方
        if (needRefreshRecipeState)
        {
            updateActiveRecipe();
            needRefreshRecipeState = false;
        }
        if (activeRecipe == null)
        {
            recipeProgress = 0;
            return;
        }

        Level level = getLevel();
        CrystalPulverizerRecipe recipe = activeRecipe.value();

        // 2) 若未完成：推进进度 + 扣能量
        if (recipeProgress < activeRecipeTime)
        {
            int speed = getSpeedMultiplier();
            double neededEnergy = getEnergyPerTick();

            if (getAECurrentPower() < neededEnergy) return;
            extractAEPower(neededEnergy, Actionable.MODULATE);

            recipeProgress = Math.min(activeRecipeTime, recipeProgress + speed);
            setChanged();
        }

        // 3) 已经完成：消耗资源并产出
        if (recipeProgress >= activeRecipeTime)
        {
            SingleRecipeInput input = new SingleRecipeInput(inputInv.getStackInSlot(0));
            ItemStack result = recipe.assemble(input, level.registryAccess());
            if (result.isEmpty()) // 如果我们拿不到输出，说明配方可能有问题，此时清空状态
            {
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeTime = 0;
                return;
            }

            // 如果输出放不下，则将recipeProgress钳制在最大配方时间
            if (!outputInv.addItems(result, true).isEmpty())
            {
                recipeProgress = activeRecipeTime;
                return;
            }

            if (!consumeInputs(recipe))
            {
                // 输入不够：清缓存和状态，等待刷新
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeTime = 0;
                return;
            }

            outputInv.addItems(result, false);
            recipeProgress = 0;
            setChanged();
        }
    }

    // 计算能量消耗
    private int getSpeedMultiplier()
    {
        int c = Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
        return 1 << c;
    }

    private double getEnergyPerTick()
    {
        return BASIC_ENERGY_COST_PER_TICK * getSpeedMultiplier();
    }

    /**
     * 更新配方状态
     */
    private void updateActiveRecipe()
    {
        if (getLevel() == null || getLevel().isClientSide()) return;

        var level = getLevel();
        var input = new SingleRecipeInput(inputInv.getStackInSlot(0));

        var opt = level.getRecipeManager().getRecipeFor(
                AECSRecipeTypes.CRYSTAL_PULVERIZER.get(),
                input,
                level
        );

        // 没有任何匹配配方：清空状态
        if (opt.isEmpty())
        {
            activeRecipe = null;
            activeRecipeTime = 0;
            recipeProgress = 0;
            return;
        }

        var holder = opt.get();
        var recipe = holder.value();

        boolean match = recipe.matches(input, level);
        if (!match)
        {
            // 理论上不该发生（因为 getRecipeFor 已经匹配过），但保底
            activeRecipe = null;
            activeRecipeTime = 0;
            recipeProgress = 0;
            return;
        }

        // 配方未变：保持进度，仅刷新 match/time
        if (activeRecipe != null && activeRecipe.id().equals(holder.id()))
        {
            activeRecipeTime = recipe.time();
            return;
        }

        // 配方变了：切换配方，重置进度
        activeRecipe = holder;
        activeRecipeTime = recipe.time();
        recipeProgress = 0;
    }

    /**
     * 尝试从输入槽中来抽取当前配方所需资源，如果能成功则返回true
     */
    private boolean consumeInputs(CrystalPulverizerRecipe recipe)
    {
        SizedIngredient required = recipe.input();

        int amount = required.count();
        // 先进行模拟抽取
        ItemStack extracted = inputInv.extractItem(0, amount, true);
        if (extracted.isEmpty() || !required.test(extracted)) return false;

        // 执行扣除
        inputInv.extractItem(0, amount, false);
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        inputInv.writeToNBT(data, "input_inv", registries);
        outputInv.writeToNBT(data, "output_inv", registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null)
        {
            data.putString("active_recipe_id", activeRecipe.id().toString());
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        inputInv.readFromNBT(data, "input_inv", registries);
        outputInv.readFromNBT(data, "output_inv", registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        recipeProgress = data.getInt("recipe_progress");
        if (data.contains("active_recipe_id"))
        {
            activeRecipeId = ResourceLocation.parse(data.getString("active_recipe_id"));
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (activeRecipeId != null && level != null)
        {
            Optional<RecipeHolder<?>> opt = level.getRecipeManager().byKey(activeRecipeId);
            opt.ifPresent(recipeHolder -> activeRecipe = (RecipeHolder<CrystalPulverizerRecipe>) recipeHolder);
        }
        updateActiveRecipe();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : inputInv)
        {
            drops.add(stack);
        }
        for (ItemStack stack : outputInv)
        {
            drops.add(stack);
        }
        for (ItemStack stack : upgrades)
        {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        inputInv.clear();
        outputInv.clear();
        upgrades.clear();
    }
}
