package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.machine.MachineFluidHost;
import io.github.lounode.ae2cs.common.machine.MachineFluidTanks;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 谐振粉碎工厂的方块实体。
 *
 * <p>
 * 与晶能粉碎机共用同一套配方类型（{@link AECSRecipeTypes#CRYSTAL_PULVERIZER}），但输入输出各扩到九格，
 * 并支持并行处理。并行不缩短单个加工周期：一个周期的目标能量与每 tick 推进量都按并行数放大，
 * 因此每份产物的能量成本不变，只是周期结束时一次性产出多份。
 * </p>
 */
@ProvideCaps(IItemHandler.class)
@ProvideCaps(IFluidHandler.class)
public class ResonatingPulverizerFactoryBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
                                                    CustomReturnableSubMenuHost, MachineFluidHost {

    /**
     * 基础能量消耗，每tick 200AE，每多一个加速卡，则此数值翻倍，同时机器运行速率也翻倍。
     */
    private static final double BASIC_ENERGY_COST_PER_TICK = 200;

    /**
     * 并行处理上限。并行不改变单个加工周期的时间，只提高一个周期内消耗与产出的份数。
     */
    private static final int MAX_PARALLELISM = 32;

    /**
     * 每张加速卡额外提供的并行数
     */
    private static final int PARALLEL_PER_SPEED_CARD = 4;

    /**
     * 每张陨石超频卡额外提供的并行数
     */
    private static final int PARALLEL_PER_OVERLOAD_CARD = 16;

    /**
     * 输入/输出槽位数量（各三乘三）
     */
    private static final int INVENTORY_SIZE = 9;

    /**
     * 升级仓
     */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.RESONATING_PULVERIZER_FACTORY_BLOCK,
            4, this::onUpgradesChanged);

    private int speedMultiplier = 1;
    private int overclockCards = 0;

    /**
     * 并行处理数，由升级卡决定，范围 1..{@link #MAX_PARALLELISM}。
     * <p>
     * 并行是加速卡与陨石超频卡的额外作用，不取代它们原有的效果：单份加工时间仍由
     * {@link #getEnergyPerTick()} 决定，并行只放大一个周期内的消耗与产出。
     */
    private int parallelism = 1;

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

    private final MachineFluidTanks fluidTanks = new MachineFluidTanks(16_000,
            () -> {
                needRefreshRecipeState = true;
                setChanged();
            }, this::setChanged);

    public ResonatingPulverizerFactoryBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.RESONATING_PULVERIZER_FACTORY_BLOCK_ENTITY.get(), pos, blockState,
                80000, false, AccessRestriction.WRITE);

        getMainNode().setIdlePowerUsage(0);

        AppEngInternalInventory inputInv = new AppEngInternalInventory(INVENTORY_SIZE) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                needRefreshRecipeState = true;
                setChanged();
            }
        };
        AppEngInternalInventory outputInv = new AppEngInternalInventory(INVENTORY_SIZE) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };

        AppEngInvComponent invComponent = new AppEngInvComponent();
        invComponent.addPort(InvPort.INPUT, inputInv);
        invComponent.addPort(InvPort.WORK, inputInv);
        invComponent.addPort(InvPort.OUTPUT, outputInv);
        getMachineComponents().add(invComponent);
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.INPUT);
    }

    public AppEngInternalInventory getOutputInv() {
        return getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.OUTPUT);
    }

    public MachineFluidTanks getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidTanks;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getActiveRecipeEnergyCost() {
        return activeRecipeEnergyCost;
    }

    /**
     * 当前并行处理数（1..{@link #MAX_PARALLELISM}）
     */
    public int getParallelism() {
        return parallelism;
    }

    public void checkActive(boolean active) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void onUpgradesChanged() {
        this.overclockCards = Math.min(2, upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD));
        this.speedMultiplier = overclockCards > 0 ? 1 : 1 << Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
        // 两张升级卡的张数上限沿用原有规则（速度卡 4 张、超频卡 2 张），并行只是额外作用
        int speedCards = Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
        this.parallelism = Math.max(1, Math.min(MAX_PARALLELISM,
                speedCards * PARALLEL_PER_SPEED_CARD + overclockCards * PARALLEL_PER_OVERLOAD_CARD));
        saveChanges();
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (getLevel() == null || getLevel().isClientSide()) return;

        checkActive(getAECurrentPower() > 0);

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
        CrystalPulverizerRecipe recipe = activeRecipe.value();

        // 2) 若未完成：推进进度 + 扣能量
        // 并行数放大单周期的处理量：目标能量与每 tick 推进量都按并行数放大，
        // 而单位能量对应的进度不变，因此单份加工时间不变，只是一个周期完成后会一次性产出多份。
        int parallel = getParallelism();
        int batchEnergy = activeRecipeEnergyCost * parallel;
        if (recipeProgress < batchEnergy) {
            if (getAECurrentPower() <= 0) return;

            double neededEnergy = getEnergyPerTick() * parallel;
            neededEnergy = Math.min(neededEnergy, batchEnergy - recipeProgress);
            double actualCost = extractAEPower(neededEnergy, Actionable.MODULATE);
            recipeProgress = Math.min(recipeProgress + (int) actualCost, batchEnergy);
            setChanged();
        }

        // 3) 已经完成：按并行数批量消耗与产出
        if (recipeProgress >= batchEnergy) {
            ItemStack sample = findMatchingInput(recipe);
            ItemStack result = sample.isEmpty() ? ItemStack.EMPTY : recipe.assemble(new SingleRecipeInput(sample), level.registryAccess());
            if (result.isEmpty()) // 如果我们拿不到输出，说明配方可能有问题，此时清空状态
            {
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            // 如果输出放不下，则将recipeProgress钳制在最大配方时间
            FluidStack fluidResult = recipe.fluidOutput();
            if (!getOutputInv().addItems(result, true).isEmpty() || (!fluidResult.isEmpty() && fluidTanks.output().fill(fluidResult, IFluidHandler.FluidAction.SIMULATE) < fluidResult.getAmount())) {
                recipeProgress = batchEnergy;
                return;
            }

            int crafted = 0;
            for (int i = 0; i < parallel; i++) {
                if (!craftOne(recipe, result)) break;
                crafted++;
            }

            if (crafted == 0) {
                // 一份都没做出来：清缓存和状态，等待刷新
                recipeProgress = 0;
                activeRecipe = null;
                activeRecipeEnergyCost = 0;
                return;
            }

            // 只有实际产出的份数真正消耗能量；未产出部分的进度保留到下一轮，
            // 相当于这部分能量已经预付，避免输入或输出受限时白扣整批能量。
            recipeProgress = batchEnergy - crafted * activeRecipeEnergyCost;
            setChanged();
        }
    }

    // 计算能量消耗
    private double getEnergyPerTick() {
        double normalEnergy = BASIC_ENERGY_COST_PER_TICK * speedMultiplier;
        if (overclockCards == 0 || activeRecipeEnergyCost <= 0) {
            return normalEnergy;
        }

        int targetTicks = overclockCards == 1 ? 4 : 1;
        return Math.max(normalEnergy, Math.ceil((double) activeRecipeEnergyCost / targetTicks));
    }

    /**
     * 更新配方状态
     */
    private void updateActiveRecipe() {
        if (getLevel() == null || getLevel().isClientSide()) return;

        var level = getLevel();

        Optional<RecipeHolder<CrystalPulverizerRecipe>> opt = level.getRecipeManager()
                .byType(AECSRecipeTypes.CRYSTAL_PULVERIZER.get()).stream()
                .filter(holder -> hasMatchingInput(holder.value()) && holder.value().matchesFluid(fluidTanks.input().getFluid()))
                .findFirst();

        // 没有任何匹配配方：清空状态
        if (opt.isEmpty()) {
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        var holder = opt.get();
        var recipe = holder.value();

        if (!hasMatchingInput(recipe)) {
            // 理论上不该发生（筛选时已经匹配过），但保底
            activeRecipe = null;
            activeRecipeEnergyCost = 0;
            recipeProgress = 0;
            return;
        }

        // 配方未变：保持进度，仅刷新 match/time
        if (activeRecipe != null && activeRecipe.id().equals(holder.id())) {
            activeRecipeEnergyCost = recipe.energyCost();
            return;
        }

        // 配方变了：切换配方，重置进度
        activeRecipe = holder;
        activeRecipeEnergyCost = recipe.energyCost();
        recipeProgress = 0;
    }

    /**
     * 尝试从输入槽中抽取当前配方所需资源，如果能成功则返回true。
     * <p>
     * 输入槽有九格，材料可能放在任意一格，因此逐格寻找第一格够用的。
     */
    private boolean consumeInputs(CrystalPulverizerRecipe recipe) {
        SizedIngredient required = recipe.input();
        if (recipe.fluidInput() != null && !recipe.fluidInput().test(fluidTanks.input().getFluid())) return false;
        if (recipe.fluidInput() != null && fluidTanks.input().drain(recipe.fluidInput().amount(), IFluidHandler.FluidAction.SIMULATE).getAmount() < recipe.fluidInput().amount()) return false;

        int amount = required.count();
        // 先进行模拟抽取，确认这一格够用再执行扣除
        for (int i = 0; i < getInputInv().size(); i++) {
            ItemStack extracted = getInputInv().extractItem(i, amount, true);
            if (extracted.isEmpty() || !required.test(extracted)) continue;

            getInputInv().extractItem(i, amount, false);
            if (recipe.fluidInput() != null) fluidTanks.input().drain(recipe.fluidInput().amount(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    /**
     * 输入槽中是否有任意一格能匹配该配方
     */
    private boolean hasMatchingInput(CrystalPulverizerRecipe recipe) {
        return !findMatchingInput(recipe).isEmpty();
    }

    /**
     * 找出输入槽中第一格能匹配该配方的材料；找不到返回空。
     */
    private ItemStack findMatchingInput(CrystalPulverizerRecipe recipe) {
        Level level = getLevel();
        if (level == null) return ItemStack.EMPTY;

        for (int i = 0; i < getInputInv().size(); i++) {
            ItemStack stack = getInputInv().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (recipe.matches(new SingleRecipeInput(stack), level)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 尝试产出单份：输出空间与输入都满足时执行一次消耗 + 产出。
     */
    private boolean craftOne(CrystalPulverizerRecipe recipe, ItemStack result) {
        if (result.isEmpty()) return false;

        FluidStack fluidResult = recipe.fluidOutput();
        if (!getOutputInv().addItems(result.copy(), true).isEmpty()) return false;
        if (!fluidResult.isEmpty() && fluidTanks.output().fill(fluidResult, IFluidHandler.FluidAction.SIMULATE) < fluidResult.getAmount()) return false;
        if (!consumeInputs(recipe)) return false;

        getOutputInv().addItems(result.copy(), false);
        if (!fluidResult.isEmpty()) fluidTanks.output().fill(fluidResult, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        fluidTanks.writeToNbt(data, registries);
        data.putInt("recipe_progress", recipeProgress);
        if (activeRecipe != null) {
            data.putString("active_recipe_id", activeRecipe.id().toString());
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        fluidTanks.readFromNbt(data, registries);
        recipeProgress = data.getInt("recipe_progress");
        if (data.contains("active_recipe_id")) {
            activeRecipeId = ResourceLocation.parse(data.getString("active_recipe_id"));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onUpgradesChanged();
        if (activeRecipeId != null && level != null) {
            Optional<RecipeHolder<?>> opt = level.getRecipeManager().byKey(activeRecipeId);
            opt.ifPresent(recipeHolder -> activeRecipe = (RecipeHolder<CrystalPulverizerRecipe>) recipeHolder);
        }
        updateActiveRecipe();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : upgrades) {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
        fluidTanks.clear();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }
}
