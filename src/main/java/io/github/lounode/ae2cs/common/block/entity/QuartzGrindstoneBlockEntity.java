package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class QuartzGrindstoneBlockEntity extends AENetworkedSelfPoweredBlockEntity implements ICrankable,
        IUpgradeableObject
{
    /**
     * 基础能量消耗，每tick 200AE，即能量消耗和速率上限均更低的晶能粉碎机
     */
    private static final double BASIC_ENERGY_COST_PER_TICK = 200;

    /**
     * 输入仓
     */
    private final AppEngInternalInventory inputInv = new AppEngInternalInventory(3)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    /**
     * 工作仓
     */
    private final AppEngInternalInventory workingInv = new AppEngInternalInventory(1)
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
    private final AppEngInternalInventory outputInv = new AppEngInternalInventory(3)
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
     * 能力暴露-工作
     */
    FilteredInternalInventory filteredWorkingInv = new FilteredInternalInventory(workingInv, new IAEItemFilter()
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
    CombinedInternalInventory combinedInv = new CombinedInternalInventory(filteredInputInv, filteredWorkingInv, filteredOutputInv);

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
     * 该配方需要的总能量
     */
    private int activeRecipeEnergyCost = 0;

    /**
     * 当前配方进度
     */
    private int recipeProgress = 0;

    /**
     * 是否需要更新配方状态
     */
    private boolean needRefreshRecipeState = true;

    public QuartzGrindstoneBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.QUARTZ_GRINDSTONE_BLOCK_ENTITY.get(), pos, blockState,
                10000, false, AccessRestriction.WRITE);

        getMainNode().setIdlePowerUsage(0);

        // 工作仓只能由可以运行配方的物品进入
        workingInv.setFilter(new IAEItemFilter()
        {
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
            {
                if (level == null) return false;
                if (stack.isEmpty()) return false;

                // 先做一个简单的合并判断，然后再进入配方判别
                // 目的是，这个槽位，只能由能运行的配方的物品进入
                ItemStack currentStack = inv.getStackInSlot(slot);
                boolean canInsert = currentStack.isEmpty() || ItemStack.isSameItem(currentStack, stack);

                SingleRecipeInput input = new SingleRecipeInput(stack);
                canInsert = canInsert
                        && level.getRecipeManager().getRecipeFor(AECSRecipeTypes.CRYSTAL_PULVERIZER.get(), input, level).isPresent();

                return IAEItemFilter.super.allowInsert(inv, slot, stack) && canInsert;
            }
        });
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AECSBlockEntities.QUARTZ_GRINDSTONE_BLOCK_ENTITY.get(),
                (be, direction) -> be.combinedInv.toItemHandler()
        );
        event.registerBlockEntity(
                AECapabilities.CRANKABLE,
                AECSBlockEntities.QUARTZ_GRINDSTONE_BLOCK_ENTITY.get(),
                (be, direction) -> {
                    if (direction == Direction.UP) return be;
                    else return null;
                }
        );
    }

    public AppEngInternalInventory getInputInv()
    {
        return inputInv;
    }

    public AppEngInternalInventory getWorkingInv()
    {
        return workingInv;
    }

    public AppEngInternalInventory getOutputInv()
    {
        return outputInv;
    }

    public int getRecipeProgress()
    {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost()
    {
        return activeRecipeEnergyCost;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();

        if (getLevel() == null || getLevel().isClientSide()) return;

        // 前置-首先将物品从输入仓推入工作仓
        pushRawsIntoWork();

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
        if (recipeProgress < activeRecipeEnergyCost)
        {
            if (getAECurrentPower() <= 0) return;

            double neededEnergy = getEnergyPerTick();
            neededEnergy = Math.min(neededEnergy, activeRecipeEnergyCost - recipeProgress);
            double actualCost = extractAEPower(neededEnergy, Actionable.MODULATE);
            recipeProgress = Math.min(recipeProgress + (int) actualCost, activeRecipeEnergyCost);
            setChanged();
        }

        // 3) 已经完成：消耗资源并产出
        if (recipeProgress >= activeRecipeEnergyCost)
        {
            SingleRecipeInput input = new SingleRecipeInput(workingInv.getStackInSlot(0));
            ItemStack result = recipe.assemble(input, level.registryAccess());
            if (result.isEmpty()) // 如果我们拿不到输出，说明配方可能有问题，此时清空状态
            {
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            // 如果输出放不下，则将recipeProgress钳制在最大配方时间
            if (!outputInv.addItems(result, true).isEmpty())
            {
                recipeProgress = activeRecipeEnergyCost;
                return;
            }

            if (!consumeInputs(recipe))
            {
                // 输入不够：清缓存和状态，等待刷新
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
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
        return 1;
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
        var input = new SingleRecipeInput(workingInv.getStackInSlot(0));

        var opt = level.getRecipeManager().getRecipeFor(
                AECSRecipeTypes.CRYSTAL_PULVERIZER.get(),
                input,
                level
        );

        // 没有任何匹配配方：清空状态
        if (opt.isEmpty())
        {
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
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
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        // 配方未变：保持进度，仅刷新 match/time
        if (activeRecipe != null && activeRecipe.id().equals(holder.id()))
        {
            activeRecipeEnergyCost = recipe.energyCost();
            return;
        }

        // 配方变了：切换配方，重置进度
        activeRecipe = holder;
        activeRecipeEnergyCost = recipe.energyCost();
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
        ItemStack extracted = workingInv.extractItem(0, amount, true);
        if (extracted.isEmpty() || !required.test(extracted)) return false;

        // 执行扣除
        workingInv.extractItem(0, amount, false);
        return true;
    }

    private void pushRawsIntoWork()
    {
        for (int i = 0; i < inputInv.size() && workingInv.isEmpty(); i++)
        {
            ItemStack stack = inputInv.extractItem(i, 99, false);
            if (stack.isEmpty()) continue;

            ItemStack remaining = workingInv.insertItem(0, stack, false);
            if (!remaining.isEmpty())
            {
                inputInv.insertItem(i, remaining, false);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        inputInv.writeToNBT(data, "input_inv", registries);
        workingInv.writeToNBT(data, "working_inv", registries);
        outputInv.writeToNBT(data, "output_inv", registries);
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null)
        {
            data.putString("active_recipe_id", activeRecipe.id().toString());
        }
        // 配方时间会由后续的update根据配方更新
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        inputInv.readFromNBT(data, "input_inv", registries);
        workingInv.readFromNBT(data, "working_inv", registries);
        outputInv.readFromNBT(data, "output_inv", registries);
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
        for (ItemStack stack : workingInv)
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
        workingInv.clear();
    }

    @Override
    public boolean canTurn()
    {
        return true;
    }

    @Override
    public void applyTurn()
    {
        injectAEPower(1600, Actionable.MODULATE);
    }
}
