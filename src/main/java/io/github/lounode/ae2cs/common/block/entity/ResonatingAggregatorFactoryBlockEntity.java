package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.inventory.ResonatingFactoryUpgradeInventory;
import io.github.lounode.ae2cs.common.inventory.ResonatingStackingInventory;
import io.github.lounode.ae2cs.common.machine.MachineFluidHost;
import io.github.lounode.ae2cs.common.machine.MachineFluidTanks;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parallel aggregator. Cards set throughput and storage independently; every actual craft pays its full recipe cost.
 * Batches pause on missing power, materials or output space. Already paid energy remains attached to the batch.
 */
@ProvideCaps(IItemHandler.class)
@ProvideCaps(IFluidHandler.class)
public class ResonatingAggregatorFactoryBlockEntity extends AENetworkedSelfPoweredBlockEntity
                                                    implements IUpgradeableObject, CustomReturnableSubMenuHost, MachineFluidHost {

    private static final int[] PARALLELISM = { 8, 32, 64, 256, 1024 };
    private static final int[] CYCLE_TICKS = { 0, 64, 16, 4, 1 };
    private final IUpgradeInventory upgrades = new ResonatingFactoryUpgradeInventory(
            AECSBlocks.RESONATING_AGGREGATOR_FACTORY_BLOCK, this::saveChanges, this::canReduceStacking);
    private final MachineFluidTanks fluidTanks = new MachineFluidTanks(16_000, this::setChanged, this::setChanged);
    @Nullable
    private RecipeHolder<CrystalAggregatorRecipe> activeRecipe;
    @Nullable
    private ResourceLocation savedRecipeId;
    private int batchSize;
    private double paidEnergy;

    public ResonatingAggregatorFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(AECSBlockEntities.RESONATING_AGGREGATOR_FACTORY_BLOCK_ENTITY.get(), pos, state,
                80_000.0 * 1024, false, AccessRestriction.WRITE);
        getMainNode().setIdlePowerUsage(0);
        var input = new ResonatingStackingInventory(9, this::getStackCapacity, this::setChanged);
        var output = new ResonatingStackingInventory(3, this::getStackCapacity, this::setChanged);
        getMachineComponents().add(new AppEngInvComponent()
                .addPort(InvPort.INPUT, input).addPort(InvPort.WORK, input).addPort(InvPort.OUTPUT, output));
        getMachineComponents().add(new SideConfigComponent());
    }

    public ResonatingStackingInventory getInputInv() {
        return (ResonatingStackingInventory) getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.INPUT);
    }

    public ResonatingStackingInventory getOutputInv() {
        return (ResonatingStackingInventory) getMachineComponents().getService(AppEngInvComponent.class).port(InvPort.OUTPUT);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public int getParallelism() {
        return PARALLELISM[upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD)];
    }

    public int getCycleTicks() {
        return CYCLE_TICKS[upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD)];
    }

    public int getStackCapacity() {
        return 64 << (2 * upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD));
    }

    private boolean canReduceStacking(int cards) {
        int capacity = 64 << (2 * cards);
        for (var inv : List.of(getInputInv(), getOutputInv())) {
            for (ItemStack stack : inv) {
                int limit = stack.isStackable() ? stack.getMaxStackSize() * capacity / 64 : 1;
                if (stack.getCount() > limit) return false;
            }
        }
        return true;
    }

    public MachineFluidTanks getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidTanks;
    }

    public int getRecipeProgress() {
        long target = getBatchEnergy();
        return target == 0 ? 0 : (int) Math.min(10_000, paidEnergy * 10_000 / target);
    }

    public int getActiveRecipeEnergyCost() {
        return activeRecipe == null ? 0 : 10_000;
    }

    public long getBatchEnergy() {
        return activeRecipe == null ? 0 : (long) Math.max(1, activeRecipe.value().energyCost()) * batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null || level.isClientSide()) return;
        boolean powered = getAECurrentPower() > 0;
        BlockState state = getBlockState();
        if (state.getValue(AECSBlockProperties.ACTIVE) != powered) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, powered), 3);
        }
        if (activeRecipe == null || batchSize <= 0) {
            activeRecipe = null;
            for (var candidate : level.getRecipeManager().byType(AECSRecipeTypes.CRYSTAL_AGGREGATOR.get())) {
                Batch plan = planBatch(candidate.value(), getParallelism());
                if (plan.count() == 0) continue;
                activeRecipe = candidate;
                batchSize = plan.count();
                paidEnergy = 0;
                break;
            }
        }
        if (activeRecipe == null) return;
        CrystalAggregatorRecipe recipe = activeRecipe.value();
        Batch available = planBatch(recipe, batchSize);
        // Blocked inputs or outputs pause work before any more energy is spent.
        if (available.count() == 0) return;
        long target = getBatchEnergy();
        int ticks = getCycleTicks();
        double rate = ticks == 0 ? 200.0 * batchSize : Math.ceil((double) target / ticks);
        if (paidEnergy < target) {
            paidEnergy += extractAEPower(Math.min(rate, target - paidEnergy), Actionable.MODULATE);
            setChanged();
        }
        if (paidEnergy < target) return;
        // Commit a fully simulated plan atomically within this server tick.
        for (int slot = 0; slot < getInputInv().size(); slot++) getInputInv().setItemDirect(slot, available.input()[slot]);
        for (int slot = 0; slot < getOutputInv().size(); slot++) getOutputInv().setItemDirect(slot, available.output()[slot]);
        if (recipe.fluidInput() != null) {
            fluidTanks.input().drain(recipe.fluidInput().amount() * available.count(), IFluidHandler.FluidAction.EXECUTE);
        }
        FluidStack fluidResult = recipe.fluidOutput();
        if (!fluidResult.isEmpty()) {
            fluidTanks.output().fill(fluidResult.copyWithAmount(fluidResult.getAmount() * available.count()), IFluidHandler.FluidAction.EXECUTE);
        }
        paidEnergy -= (long) Math.max(1, recipe.energyCost()) * available.count();
        batchSize -= available.count();
        if (batchSize == 0) activeRecipe = null;
        setChanged();
    }

    private Batch planBatch(CrystalAggregatorRecipe recipe, int maxCount) {
        ItemStack[] input = copyInventory(getInputInv());
        ItemStack[] output = copyInventory(getOutputInv());
        FluidStack fluidResult = recipe.fluidOutput();
        int count = 0;
        for (; count < maxCount; count++) {
            int[] match = new int[recipe.required().size()];
            if (match.length == 0 || !findMatch(recipe, input, match, 0)) break;
            if (recipe.fluidInput() != null) {
                FluidStack fluid = fluidTanks.input().getFluid();
                long required = (long) recipe.fluidInput().amount() * (count + 1);
                if (!recipe.matchesFluid(fluid) || fluid.getAmount() < required) break;
            }
            if (!fluidResult.isEmpty()) {
                long amount = (long) fluidResult.getAmount() * (count + 1);
                if (amount > Integer.MAX_VALUE || fluidTanks.output().fill(fluidResult.copyWithAmount((int) amount),
                        IFluidHandler.FluidAction.SIMULATE) < amount)
                    break;
            }
            ItemStack[] nextOutput = copyStacks(output);
            if (!insertResult(nextOutput, recipe.result())) break;
            output = nextOutput;
            for (int ingredient = 0; ingredient < match.length; ingredient++) {
                int slot = match[ingredient];
                input[slot] = input[slot].copyWithCount(input[slot].getCount() - recipe.required().get(ingredient).count());
            }
        }
        return new Batch(count, input, output);
    }

    private boolean findMatch(CrystalAggregatorRecipe recipe, ItemStack[] input, int[] match, int ingredient) {
        if (ingredient == match.length) return true;
        for (int slot = 0; slot < input.length; slot++) {
            boolean used = false;
            for (int previous = 0; previous < ingredient; previous++) used |= match[previous] == slot;
            if (used || !recipe.required().get(ingredient).test(input[slot])) continue;
            match[ingredient] = slot;
            if (findMatch(recipe, input, match, ingredient + 1)) return true;
        }
        return false;
    }

    private boolean insertResult(ItemStack[] output, ItemStack result) {
        int remaining = result.getCount();
        for (int pass = 0; pass < 2; pass++) {
            for (int slot = 0; slot < output.length; slot++) {
                ItemStack current = output[slot];
                if (current.isEmpty() != (pass == 1)) continue;
                if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, result)) continue;
                int added = Math.min(remaining, Math.max(0, getOutputInv().getItemLimit(result) - current.getCount()));
                if (added > 0) output[slot] = result.copyWithCount(current.getCount() + added);
                remaining -= added;
                if (remaining == 0) return true;
            }
        }
        return remaining == 0;
    }

    private static ItemStack[] copyInventory(ResonatingStackingInventory inventory) {
        ItemStack[] stacks = new ItemStack[inventory.size()];
        for (int slot = 0; slot < stacks.length; slot++) stacks[slot] = inventory.getStackInSlot(slot).copy();
        return stacks;
    }

    private static ItemStack[] copyStacks(ItemStack[] stacks) {
        ItemStack[] copy = new ItemStack[stacks.length];
        for (int slot = 0; slot < stacks.length; slot++) copy[slot] = stacks[slot].copy();
        return copy;
    }

    private record Batch(int count, ItemStack[] input, ItemStack[] output) {}

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        fluidTanks.writeToNbt(data, registries);
        data.putInt("factory_batch_size", batchSize);
        data.putDouble("factory_paid_energy", paidEnergy);
        if (activeRecipe != null) data.putString("factory_recipe_id", activeRecipe.id().toString());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        fluidTanks.readFromNbt(data, registries);
        batchSize = Math.clamp(data.getInt("factory_batch_size"), 0, 1024);
        paidEnergy = Math.max(0, data.getDouble("factory_paid_energy"));
        savedRecipeId = ResourceLocation.tryParse(data.getString("factory_recipe_id"));
        activeRecipe = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (savedRecipeId != null && level != null) {
            var recipe = level.getRecipeManager().byKey(savedRecipeId).orElse(null);
            if (recipe != null && recipe.value() instanceof CrystalAggregatorRecipe aggregator) {
                activeRecipe = new RecipeHolder<>(recipe.id(), aggregator);
            }
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        List<ItemStack> raw = new ArrayList<>();
        super.addAdditionalDrops(level, pos, raw);
        for (ItemStack stack : raw) {
            int count = stack.getCount();
            while (count > 0) {
                int amount = Math.min(count, stack.getMaxStackSize());
                drops.add(stack.copyWithCount(amount));
                count -= amount;
            }
        }
        for (ItemStack stack : upgrades) drops.add(stack.copy());
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
