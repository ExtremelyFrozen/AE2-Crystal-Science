package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.recipe.SizedIngredient;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import io.github.lounode.ae2cs.common.recipe.input.SingleItemStackRecipeInput;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.capabilities.Capabilities;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Optional;

public class QuartzGrindstoneBlockEntity extends AENetworkedSelfPoweredBlockEntity implements ICrankable,
                                         IUpgradeableObject, CustomReturnableSubMenuHost {

    private LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.empty();
    private final EnumMap<Direction, LazyOptional<IItemHandler>> sidedItemHandlerOpts = new EnumMap<>(net.minecraft.core.Direction.class);
    private LazyOptional<ICrankable> crankableOpt = LazyOptional.empty();

    /**
     * 基础能量消耗，每tick 200AE，即能量消耗和速率上限均更低的晶能粉碎机
     */
    private static final double BASIC_ENERGY_COST_PER_TICK = 200;

    /**
     * 当前执行的配方
     */
    @Nullable
    private CrystalPulverizerRecipe activeRecipe;

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

    public QuartzGrindstoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.QUARTZ_GRINDSTONE_BLOCK_ENTITY.get(), pos, blockState,
                10000, false, AccessRestriction.WRITE);

        getMainNode().setIdlePowerUsage(0);

        AppEngInternalInventory inputInv = new AppEngInternalInventory(3) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };

        AppEngInternalInventory workingInv = new AppEngInternalInventory(1) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                needRefreshRecipeState = true;
                setChanged();
            }
        };

        AppEngInternalInventory outputInv = new AppEngInternalInventory(3) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };

        // 工作仓只能由可以运行配方的物品进入
        workingInv.setFilter(new IAEItemFilter() {

            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                if (level == null) return false;
                if (stack.isEmpty()) return false;

                // 先做一个简单的合并判断，然后再进入配方判别
                // 目的是，这个槽位，只能由能运行的配方的物品进入
                ItemStack currentStack = inv.getStackInSlot(slot);
                boolean canInsert = currentStack.isEmpty() || ItemStack.isSameItem(currentStack, stack);

                SingleItemStackRecipeInput input = SingleItemStackRecipeInput.of(stack);
                canInsert = canInsert && level.getRecipeManager().getRecipeFor(AECSRecipeTypes.CRYSTAL_PULVERIZER.get(), input, level).isPresent();

                return IAEItemFilter.super.allowInsert(inv, slot, stack) && canInsert;
            }
        });
        FilteredInternalInventory filteredWorkingInv = new FilteredInternalInventory(workingInv, new IAEItemFilter() {

            @Override
            public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                return false;
            }
        });

        AppEngInvComponent invComponent = new AppEngInvComponent();
        invComponent.addPort(InvPort.INPUT, inputInv);
        invComponent.addPort(InvPort.WORK, workingInv);
        invComponent.addPort(InvPort.OUTPUT, outputInv);
        getMachineComponents().add(invComponent);
        invComponent.setWrap(InvPort.WORK, filteredWorkingInv);
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.INPUT);
    }

    public AppEngInternalInventory getWorkingInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.WORK);
    }

    public AppEngInternalInventory getOutputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.OUTPUT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == Capabilities.CRANKABLE && side == net.minecraft.core.Direction.UP) {
            if (!crankableOpt.isPresent()) {
                crankableOpt = LazyOptional.of(() -> this);
            }
            return crankableOpt.cast();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                if (!itemHandlerOpt.isPresent()) {
                    IItemHandler handler = resolveItemHandler(null);
                    itemHandlerOpt = handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
                }
                return itemHandlerOpt.cast();
            }

            var opt = sidedItemHandlerOpts.get(side);
            if (opt == null) {
                IItemHandler handler = resolveItemHandler(side);
                opt = handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
                sidedItemHandlerOpts.put(side, opt);
            }
            return opt.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (itemHandlerOpt.isPresent()) itemHandlerOpt.invalidate();
        itemHandlerOpt = LazyOptional.empty();
        for (var opt : sidedItemHandlerOpts.values()) {
            if (opt.isPresent()) opt.invalidate();
        }
        sidedItemHandlerOpts.clear();

        if (crankableOpt.isPresent()) crankableOpt.invalidate();
        crankableOpt = LazyOptional.empty();
    }

    private @Nullable IItemHandler resolveItemHandler(@Nullable net.minecraft.core.Direction side) {
        if (getMachineComponents().hasService(SideConfigComponent.class)) {
            var inv = getMachineComponents().getService(SideConfigComponent.class).appEngInvForSide(side);
            return inv == null ? null : inv.toItemHandler();
        }
        return getMachineComponents().getService(AppEngInvComponent.class).combined().toItemHandler();
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost() {
        return activeRecipeEnergyCost;
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (getLevel() == null || getLevel().isClientSide()) return;

        // 前置-首先将物品从输入仓推入工作仓
        pushRawsIntoWork();

        // 1) 更新/确认活动配方
        if (needRefreshRecipeState) {
            updateActiveRecipe();
            needRefreshRecipeState = false;
        }
        if (activeRecipe == null) {
            recipeProgress = 0;
            return;
        }

        Level level = getLevel();
        CrystalPulverizerRecipe recipe = activeRecipe;

        // 2) 若未完成：推进进度 + 扣能量
        if (recipeProgress < activeRecipeEnergyCost) {
            if (getAECurrentPower() <= 0) return;

            double neededEnergy = getEnergyPerTick();
            neededEnergy = Math.min(neededEnergy, activeRecipeEnergyCost - recipeProgress);
            double actualCost = extractAEPower(neededEnergy, Actionable.MODULATE);
            recipeProgress = Math.min(recipeProgress + (int) actualCost, activeRecipeEnergyCost);
            setChanged();
        }

        // 3) 已经完成：消耗资源并产出
        if (recipeProgress >= activeRecipeEnergyCost) {
            SingleItemStackRecipeInput input = SingleItemStackRecipeInput.of(getWorkingInv().getStackInSlot(0));
            ItemStack result = recipe.assemble(input, level.registryAccess());
            if (result.isEmpty()) // 如果我们拿不到输出，说明配方可能有问题，此时清空状态
            {
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            // 如果输出放不下，则将recipeProgress钳制在最大配方时间
            if (!getOutputInv().addItems(result, true).isEmpty()) {
                recipeProgress = activeRecipeEnergyCost;
                return;
            }

            if (!consumeInputs(recipe)) {
                // 输入不够：清缓存和状态，等待刷新
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            getOutputInv().addItems(result, false);
            recipeProgress = 0;
            setChanged();
        }
    }

    // 计算能量消耗
    private int getSpeedMultiplier() {
        return 1;
    }

    private double getEnergyPerTick() {
        return BASIC_ENERGY_COST_PER_TICK * getSpeedMultiplier();
    }

    /**
     * 更新配方状态
     */
    private void updateActiveRecipe() {
        if (getLevel() == null || getLevel().isClientSide()) return;

        var level = getLevel();
        var input = SingleItemStackRecipeInput.of(getWorkingInv().getStackInSlot(0));

        var opt = level.getRecipeManager().getRecipeFor(
                AECSRecipeTypes.CRYSTAL_PULVERIZER.get(),
                input,
                level);

        // 没有任何匹配配方：清空状态
        if (opt.isEmpty()) {
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        var recipe = opt.get();

        boolean match = recipe.matches(input, level);
        if (!match) {
            // 理论上不该发生（因为 getRecipeFor 已经匹配过），但保底
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        // 配方未变：保持进度，仅刷新 match/time
        if (activeRecipe != null && activeRecipe.getId().equals(recipe.getId())) {
            activeRecipeEnergyCost = recipe.energyCost();
            return;
        }

        // 配方变了：切换配方，重置进度
        activeRecipe = recipe;
        activeRecipeEnergyCost = recipe.energyCost();
        recipeProgress = 0;
    }

    /**
     * 尝试从输入槽中来抽取当前配方所需资源，如果能成功则返回true
     */
    private boolean consumeInputs(CrystalPulverizerRecipe recipe) {
        SizedIngredient required = recipe.input();

        int amount = required.count();
        // 先进行模拟抽取
        ItemStack extracted = getWorkingInv().extractItem(0, amount, true);
        if (extracted.isEmpty() || !required.test(extracted)) return false;

        // 执行扣除
        getWorkingInv().extractItem(0, amount, false);
        return true;
    }

    private void pushRawsIntoWork() {
        for (int i = 0; i < getInputInv().size() && getWorkingInv().isEmpty(); i++) {
            ItemStack stack = getInputInv().extractItem(i, 99, false);
            if (stack.isEmpty()) continue;

            ItemStack remaining = getWorkingInv().insertItem(0, stack, false);
            if (!remaining.isEmpty()) {
                getInputInv().insertItem(i, remaining, false);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null) {
            data.putString("active_recipe_id", activeRecipe.getId().toString());
        }
        // 配方时间会由后续的update根据配方更新
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        recipeProgress = data.getInt("recipe_progress");
        if (data.contains("active_recipe_id")) {
            activeRecipeId = ResourceLocation.tryParse(data.getString("active_recipe_id"));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (activeRecipeId != null && level != null) {
            Optional<? extends Recipe<?>> opt = level.getRecipeManager().byKey(activeRecipeId);
            opt.ifPresent(recipeHolder -> activeRecipe = (CrystalPulverizerRecipe) recipeHolder);
        }
        updateActiveRecipe();
    }

    @Override
    public boolean canTurn() {
        return true;
    }

    @Override
    public void applyTurn() {
        injectAEPower(1600, Actionable.MODULATE);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }
}
