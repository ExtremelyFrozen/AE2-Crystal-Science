package io.github.lounode.ae2cs.common.machine;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Forge fluid view over the fluid entries in an AE2 generic inventory. */
public final class GenericInventoryFluidHandler implements IFluidHandler {

    public static final int CAPACITY = 16_000;

    private final GenericInternalInventory inventory;
    private final boolean allowFill;
    private final boolean allowDrain;

    public GenericInventoryFluidHandler(GenericInternalInventory inventory, boolean allowFill, boolean allowDrain) {
        this.inventory = inventory;
        this.allowFill = allowFill;
        this.allowDrain = allowDrain;
    }

    @Override
    public int getTanks() {
        return inventory.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        AEKey key = inventory.getKey(tank);
        if (!(key instanceof AEFluidKey fluidKey)) return FluidStack.EMPTY;
        return new FluidStack(fluidKey.getFluid(), (int) Math.min(inventory.getAmount(tank), Integer.MAX_VALUE));
    }

    @Override
    public int getTankCapacity(int tank) {
        return CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return allowFill && !stack.isEmpty() && inventory.isAllowedIn(tank, AEFluidKey.of(stack.getFluid()));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!allowFill || resource.isEmpty()) return 0;

        AEFluidKey key = AEFluidKey.of(resource.getFluid());
        for (int slot = 0; slot < inventory.size(); slot++) {
            AEKey existing = inventory.getKey(slot);
            if (existing != null && !existing.equals(key)) continue;
            long room = CAPACITY - inventory.getAmount(slot);
            if (room <= 0) continue;
            long inserted = inventory.insert(slot, key, Math.min(room, resource.getAmount()),
                    action == FluidAction.EXECUTE ? Actionable.MODULATE : Actionable.SIMULATE);
            if (inserted > 0) return (int) inserted;
        }
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!allowDrain || resource.isEmpty()) return FluidStack.EMPTY;
        AEFluidKey key = AEFluidKey.of(resource.getFluid());
        for (int slot = 0; slot < inventory.size(); slot++) {
            long extracted = inventory.extract(slot, key, resource.getAmount(),
                    action == FluidAction.EXECUTE ? Actionable.MODULATE : Actionable.SIMULATE);
            if (extracted > 0) return new FluidStack(resource.getFluid(), (int) extracted);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!allowDrain || maxDrain <= 0) return FluidStack.EMPTY;
        for (int slot = 0; slot < inventory.size(); slot++) {
            FluidStack fluid = getFluidInTank(slot);
            if (!fluid.isEmpty()) return drain(new FluidStack(fluid.getFluid(), maxDrain), action);
        }
        return FluidStack.EMPTY;
    }
}
