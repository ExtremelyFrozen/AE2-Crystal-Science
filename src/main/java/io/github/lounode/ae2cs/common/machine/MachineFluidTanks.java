package io.github.lounode.ae2cs.common.machine;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Separate recipe input and output tanks while exposing one conventional fluid capability. */
public final class MachineFluidTanks implements IFluidHandler {

    private final FluidTank input;
    private final FluidTank output;

    public MachineFluidTanks(int capacity, Runnable onInputChanged, Runnable onOutputChanged) {
        input = new FluidTank(capacity) {

            @Override
            protected void onContentsChanged() {
                onInputChanged.run();
            }
        };
        output = new FluidTank(capacity) {

            @Override
            protected void onContentsChanged() {
                onOutputChanged.run();
            }
        };
    }

    public FluidTank input() {
        return input;
    }

    public FluidTank output() {
        return output;
    }

    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inputTag = new CompoundTag();
        CompoundTag outputTag = new CompoundTag();
        input.writeToNBT(registries, inputTag);
        output.writeToNBT(registries, outputTag);
        tag.put("fluid_input", inputTag);
        tag.put("fluid_output", outputTag);
    }

    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("fluid_input")) input.readFromNBT(registries, tag.getCompound("fluid_input"));
        if (tag.contains("fluid_output")) output.readFromNBT(registries, tag.getCompound("fluid_output"));
    }

    public void clear() {
        input.setFluid(FluidStack.EMPTY);
        output.setFluid(FluidStack.EMPTY);
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? input.getFluid() : tank == 1 ? output.getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? input.getCapacity() : tank == 1 ? output.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && input.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return input.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return output.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return output.drain(maxDrain, action);
    }
}
