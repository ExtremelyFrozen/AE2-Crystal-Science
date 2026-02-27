package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.util.ConfigInventory;
import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ProvideCaps(GenericInternalInventory.class)
public class EntropyVariationReactionChamberBlockEntity extends AENetworkedSelfPoweredBlockEntity implements
        IUpgradeableObject, IConfigurableObject, CustomReturnableSubMenuHost
{
    /**
     * 基础能量消耗，每tick 200AE，每多一个加速卡，则此数值翻倍，同时机器运行速率也翻倍。
     * <p>
     * 目前最大四张加速卡，则最大速率为16倍，同时最大每tick消耗也为16倍，即3200AE每tick
     */
    private static final double BASIC_ENERGY_COST_PER_TICK = 200;

    private static final int RECIPE_DEFAULT_COST_ENERGY = 1600;

    /**
     * 升级仓
     */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK,
            4, this::saveChanges);

    private final IConfigManager configManager;

    /**
     * 当前执行的配方
     */
    @Nullable
    private RecipeHolder<EntropyRecipe> activeRecipe;

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

    private final IActionSource actionSource;

    public EntropyVariationReactionChamberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState,
                80000, false, AccessRestriction.WRITE);

        configManager = IConfigManager.builder(this::onConfigChange)
                .registerSetting(AECSSettings.ENTROPY_CHANGE_MODE, EntropyMode.HEAT)
                .build();

        getMainNode().setIdlePowerUsage(0);
        actionSource = IActionSource.ofMachine(this);

        ConfigInventory inputInv = ConfigInventory.storage(1)
                .changeListener(() -> {
                    needRefreshRecipeState = true;
                    setChanged();
                }).build();
        ConfigInventory outputInv = ConfigInventory.storage(4)
                .changeListener(this::setChanged).build();
        inputInv.useRegisteredCapacities();
        outputInv.useRegisteredCapacities();

        GenericStackInvComponent genericStackInvComponent = new GenericStackInvComponent();
        genericStackInvComponent.addPort(InvPort.INPUT, inputInv);
        genericStackInvComponent.addPort(InvPort.WORK, inputInv);
        genericStackInvComponent.addPort(InvPort.OUTPUT, outputInv);
        getMachineComponents().add(genericStackInvComponent);
        getMachineComponents().add(new SideConfigComponent());
    }

    public GenericStackInv getInputInv()
    {
        return getMachineComponents().getService(GenericStackInvComponent.class).port(InvPort.INPUT);
    }

    public GenericStackInv getOutputInv()
    {
        return getMachineComponents().getService(GenericStackInvComponent.class).port(InvPort.OUTPUT);
    }

    public int getRecipeProgress()
    {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost()
    {
        return activeRecipeEnergyCost;
    }

    public EntropyMode getEntropyMode()
    {
        return configManager.getSetting(AECSSettings.ENTROPY_CHANGE_MODE);
    }

    public void checkActive(boolean active)
    {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active)
        {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    @Override
    public IConfigManager getConfigManager()
    {
        return this.configManager;
    }

    protected void onConfigChange()
    {
        needRefreshRecipeState = true;
        this.saveChanges();
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();

        if (getLevel() == null || getLevel().isClientSide()) return;

        checkActive(getAECurrentPower() > 0);

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
        EntropyRecipe recipe = activeRecipe.value();

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
            List<GenericStack> result = getRecipeOutput(recipe);
            if (result.isEmpty()) // 如果我们拿不到输出，说明配方可能有问题，此时清空状态
            {
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            // 如果输出放不下，则将recipeProgress钳制在最大配方时间
            for (GenericStack stack : result)
            {
                // TODO 就现在而言，理论上result可以有多个输出，逐步单输出的模拟对于具有多输出的配方失效
                // TODO 但是，所有AE原版的熵变配方都是单输出，因此，目前只用这种模拟方式
                if (getOutputInv().insert(stack.what(), stack.amount(), Actionable.SIMULATE, actionSource) < stack.amount())
                {
                    recipeProgress = activeRecipeEnergyCost;
                    return;
                }
            }

            if (!consumeInputs(recipe))
            {
                // 输入不够：清缓存和状态，等待刷新
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            for (GenericStack stack : result)
            {
                getOutputInv().insert(stack.what(), stack.amount(), Actionable.MODULATE, actionSource);
            }
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
        AEKey inputKey = getInputInv().getKey(0);
        BlockState inputBlockState = Blocks.VOID_AIR.defaultBlockState();
        FluidState inputFluidState = Fluids.EMPTY.defaultFluidState();
        if (inputKey instanceof AEItemKey itemKey && itemKey.getItem() instanceof BlockItem blockItem)
        {
            inputBlockState = blockItem.getBlock().defaultBlockState();
        }
        if (inputKey instanceof AEFluidKey fluidKey)
        {
            inputFluidState = fluidKey.getFluid().defaultFluidState();
        }


        var holder = findRecipe(level, getEntropyMode(), inputBlockState, inputFluidState);
        if (holder == null)
        {
            // 没有配方，清空进度
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        // 配方未变：保持进度，仅刷新 match/time
        if (activeRecipe != null && activeRecipe.id().equals(holder.id()))
        {
            activeRecipeEnergyCost = RECIPE_DEFAULT_COST_ENERGY;
            return;
        }

        // 配方变了：切换配方，重置进度
        activeRecipe = holder;
        activeRecipeEnergyCost = RECIPE_DEFAULT_COST_ENERGY;
        recipeProgress = 0;
    }

    /**
     * 尝试从输入槽中来抽取当前配方所需资源，如果能成功则返回true
     */
    private boolean consumeInputs(EntropyRecipe recipe)
    {
        EntropyRecipe.Input required = recipe.getInput();

        // 模拟抽取
        boolean canConsume = required.block().map(blockInput -> {
            Item blockItem = blockInput.block().asItem();
            if (blockItem != Items.AIR)
            {
                return getInputInv().extract(0, AEItemKey.of(blockItem), 1, Actionable.SIMULATE) >= 1;
            }
            else
                return true;
        }).orElse(true);
        canConsume = canConsume && required.fluid().map(fluidInput -> {
            Fluid fluid = fluidInput.fluid();
            if (fluid != Fluids.EMPTY)
            {
                return getInputInv().extract(0, AEFluidKey.of(fluid), 1000, Actionable.SIMULATE) >= 1000;
            }
            else
                return true;
        }).orElse(true);
        if (!canConsume) return false;

        // 实际抽取
        required.block().ifPresent(blockInput -> {
            Item blockItem = blockInput.block().asItem();
            if (blockItem != Items.AIR)
            {
                getInputInv().extract(0, AEItemKey.of(blockItem), 1, Actionable.MODULATE);
            }
        });
        required.fluid().ifPresent(fluidInput -> {
            Fluid fluid = fluidInput.fluid();
            if (fluid != Fluids.EMPTY)
            {
                getInputInv().extract(0, AEFluidKey.of(fluid), 1000, Actionable.MODULATE);
            }
        });
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        this.configManager.writeToNBT(data, registries);
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
        this.configManager.readFromNBT(data, registries);
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
            opt.ifPresent(recipeHolder -> activeRecipe = (RecipeHolder<EntropyRecipe>) recipeHolder);
        }
        if (level != null && !level.isClientSide())
        {
            updateActiveRecipe();
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : upgrades)
        {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getItemFromBlockEntity());
    }

    /**
     * 用来找出对应的配方
     */
    @Nullable
    private static RecipeHolder<EntropyRecipe> findRecipe(Level level, EntropyMode mode, BlockState blockState,
                                                          FluidState fluidState)
    {
        for (var holder : level.getRecipeManager().byType(AERecipeTypes.ENTROPY))
        {
            var recipe = holder.value();
            if (recipe.matches(mode, blockState, fluidState))
            {
                if (!getRecipeOutput(recipe).isEmpty())
                    return holder;
            }
        }
        return null;
    }

    /**
     * 把配方输出的东西转为可直接识别的GStack
     */
    private static List<GenericStack> getRecipeOutput(EntropyRecipe recipe)
    {
        List<GenericStack> outputList = new ArrayList<>();
        EntropyRecipe.Output output = recipe.getOutput();
        output.block().ifPresent(blockOutput -> {
            Item blockItem = blockOutput.block().asItem();
            if (blockItem != Items.AIR)
            {
                outputList.add(new GenericStack(AEItemKey.of(blockItem), 1));
            }
        });
        output.fluid().ifPresent(fluidOutput -> {
            Fluid fluid = fluidOutput.fluid();
            if (fluid != Fluids.EMPTY)
            {
                outputList.add(new GenericStack(AEFluidKey.of(fluid), 1000));
            }
        });
        output.drops().forEach(outputDrop -> {
            if (!outputDrop.isEmpty())
            {
                outputList.add(new GenericStack(AEItemKey.of(outputDrop), outputDrop.getCount()));
            }
        });
        return outputList;
    }
}