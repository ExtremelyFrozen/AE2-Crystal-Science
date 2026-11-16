package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.inventory.ResonatingFactoryUpgradeInventory;
import io.github.lounode.ae2cs.common.inventory.ResonatingStackingInventory;
import io.github.lounode.ae2cs.common.machine.ResonatingFactoryParallelism;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

@ProvideCaps(IItemHandler.class)
public class ResonatingGrowthFactoryBlockEntity extends AENetworkedSelfPoweredBlockEntity
                                                implements IUpgradeableObject, CustomReturnableSubMenuHost {

    public static final int PAIR_COUNT = 24;
    public static final int CLUSTER_TARGET = 3;
    public static final int DUST_TARGET = 4;
    public static final int CRYSTAL_TARGET = 5;
    private static final int[] INPUT_CAPACITY = { 1, 32, 64 };
    private static final int[] CYCLE_TICKS = { 80, 60, 40, 20, 4 };
    private static final int[] YIELD_PER_ROCK = { 1, 2, 3, 4, 6 };
    private static final int ENERGY_PER_ROCK = 100;

    private final IUpgradeInventory upgrades = new ResonatingFactoryUpgradeInventory(
            AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK, this::saveChanges,
            this::canReduceCapacity);
    private final ResonatingStackingInventory input = new ResonatingStackingInventory(
            PAIR_COUNT, this::getStackCapacity, this::saveChanges) {

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return CrystalGrowthChamberBlockEntity.isAcceleratorInput(stack);
        }

        @Override
        public int getItemLimit(ItemStack stack) {
            return getStackCapacity();
        }
    };
    private final ResonatingStackingInventory output = new ResonatingStackingInventory(
            PAIR_COUNT, this::getOutputCapacity, this::saveChanges);
    // A stack can only hold one grade. Keep any overflow until a mother rock slot can accept it.
    private final ResonatingStackingInventory pendingDecay = new ResonatingStackingInventory(
            PAIR_COUNT, () -> 64, this::saveChanges);
    private int targetStage = CLUSTER_TARGET;
    private int elapsedTicks;
    private int nextPair;

    public ResonatingGrowthFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(AECSBlockEntities.RESONATING_GROWTH_FACTORY_BLOCK_ENTITY.get(), pos, state,
                256 * ENERGY_PER_ROCK, false, AccessRestriction.WRITE);
        getMainNode().setIdlePowerUsage(0);
        getMachineComponents().add(new AppEngInvComponent().addPort(InvPort.INPUT, input)
                .addPort(InvPort.OUTPUT, output));
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInputInv() {
        return input;
    }

    public AppEngInternalInventory getOutputInv() {
        return output;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public int getStackCapacity() {
        return INPUT_CAPACITY[upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD)];
    }

    private int getOutputCapacity() {
        return 64 << (2 * upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD));
    }

    private boolean canReduceCapacity(int cards) {
        for (ItemStack stack : input) {
            if (stack.getCount() > INPUT_CAPACITY[cards]) return false;
        }
        return ResonatingStackingInventory.canReduceCapacity(cards, output);
    }

    public int getTargetStage() {
        return targetStage;
    }

    public void cycleTargetStage() {
        targetStage = (targetStage + 1) % (CRYSTAL_TARGET + 1);
        saveChanges();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        pendingDecay.writeToNBT(data, "pending_decay", registries);
        data.putInt("target_stage", targetStage);
        data.putInt("growth_ticks", elapsedTicks);
        data.putInt("next_pair", nextPair);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        pendingDecay.readFromNBT(data, data.contains("pending_decay") ? "pending_decay" : "decay_output", registries);
        targetStage = data.contains("target_stage") ? Math.clamp(data.getInt("target_stage"), 0, CRYSTAL_TARGET) : CLUSTER_TARGET;
        elapsedTicks = Math.clamp(data.getInt("growth_ticks"), 0, CYCLE_TICKS[0]);
        nextPair = Math.floorMod(data.getInt("next_pair"), PAIR_COUNT);
        if (data.contains("inv_work") && !data.contains("inv_input")) {
            var oldInventory = new ResonatingStackingInventory(PAIR_COUNT * 2, () -> 64, () -> {});
            oldInventory.readFromNBT(data, "inv_work", registries);
            for (int pair = 0; pair < PAIR_COUNT; pair++) {
                int oldSlot = pair + pair / 8 * 8;
                output.setItemDirect(pair, oldInventory.getStackInSlot(oldSlot));
                input.setItemDirect(pair, oldInventory.getStackInSlot(oldSlot + 8));
            }
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        List<ItemStack> raw = new ArrayList<>();
        super.addAdditionalDrops(level, pos, raw);
        for (ItemStack stack : pendingDecay) raw.add(stack);
        ResonatingStackingInventory.splitDrops(raw, drops);
        for (ItemStack stack : upgrades) drops.add(stack.copy());
    }

    @Override
    public void clearContent() {
        super.clearContent();
        pendingDecay.clear();
        upgrades.clear();
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        flushPendingDecay();
        super.serverTick();
        var node = getMainNode().getNode();
        boolean powered = node != null && node.isPowered();
        BlockState state = getBlockState();
        if (state.getValue(AECSBlockProperties.ACTIVE) != powered) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, powered), 3);
        }
        if (input.isEmpty()) {
            if (elapsedTicks != 0) {
                elapsedTicks = 0;
                setChanged();
            }
            return;
        }
        if (getAECurrentPower() < ENERGY_PER_ROCK) return;
        int cards = upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD);
        if (++elapsedTicks < CYCLE_TICKS[cards]) {
            setChanged();
            return;
        }
        elapsedTicks = 0;
        int remaining = ResonatingFactoryParallelism.forCards(cards);
        for (int visited = 0; visited < PAIR_COUNT && remaining > 0; visited++) {
            int pair = nextPair;
            nextPair = (nextPair + 1) % PAIR_COUNT;
            remaining -= grow(pair, remaining, YIELD_PER_ROCK[cards]);
        }
        flushPendingDecay();
        saveChanges();
    }

    private void flushPendingDecay() {
        for (int pair = 0; pair < PAIR_COUNT; pair++) {
            ItemStack pending = pendingDecay.getStackInSlot(pair);
            if (pending.isEmpty()) continue;
            int remaining = putDecayInInput(pair, pending, pending.getCount());
            for (int slot = 0; slot < PAIR_COUNT && remaining > 0; slot++) {
                if (slot == pair || input.getStackInSlot(slot).isEmpty()) continue;
                remaining = putDecayInInput(slot, pending, remaining);
            }
            for (int slot = 0; slot < PAIR_COUNT && remaining > 0; slot++) {
                if (slot == pair || !input.getStackInSlot(slot).isEmpty()) continue;
                remaining = putDecayInInput(slot, pending, remaining);
            }
            if (remaining != pending.getCount()) pendingDecay.setItemDirect(pair, pending.copyWithCount(remaining));
        }
    }

    private int putDecayInInput(int slot, ItemStack degraded, int remaining) {
        ItemStack current = input.getStackInSlot(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, degraded)) return remaining;
        int inserted = Math.min(remaining, getStackCapacity() - current.getCount());
        if (inserted <= 0) return remaining;
        input.setItemDirect(slot, degraded.copyWithCount(current.getCount() + inserted));
        return remaining - inserted;
    }

    private int grow(int pair, int parallel, int yield) {
        ItemStack rock = input.getStackInSlot(pair);
        if (rock.isEmpty() || !input.isItemValid(pair, rock) || !pendingDecay.getStackInSlot(pair).isEmpty()) return 0;
        Block block = Block.byItem(rock.getItem());
        ItemStack product = GrowthFactoryProducts.get(block, targetStage);
        if (product.isEmpty()) return 0;
        ItemStack existing = output.getStackInSlot(pair);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, product)) return 0;

        int outputRoom = output.getItemLimit(product) - existing.getCount();
        int poweredRocks = (int) (getAECurrentPower() / ENERGY_PER_ROCK);
        int count = Math.min(Math.min(parallel, poweredRocks),
                Math.min(Math.min(rock.getCount(), getStackCapacity()), outputRoom / yield));
        if (count <= 0) return 0;

        extractAEPower((double) ENERGY_PER_ROCK * count, Actionable.MODULATE);
        output.insertItem(pair, product.copyWithCount(count * yield), false);
        var decay = CrystalGrowthChamberBlockEntity.motherRockDecay(block);
        if (decay != null) {
            int degraded = 0;
            for (int i = 0; i < count; i++) {
                if (decay.roll(level.getRandom())) degraded++;
            }
            if (degraded > 0) {
                ItemStack degradedRock = new ItemStack(decay.result(), degraded);
                if (degraded == rock.getCount()) {
                    input.setItemDirect(pair, degradedRock);
                } else {
                    input.setItemDirect(pair, rock.copyWithCount(rock.getCount() - degraded));
                    pendingDecay.setItemDirect(pair, degradedRock);
                }
            }
        }
        return count;
    }
}
