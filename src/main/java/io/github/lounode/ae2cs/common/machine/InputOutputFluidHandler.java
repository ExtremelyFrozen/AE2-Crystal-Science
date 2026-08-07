package io.github.lounode.ae2cs.common.machine;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Exposes one handler for machines whose fluid input and output use separate stores. */
public final class InputOutputFluidHandler implements IFluidHandler {

    private final IFluidHandler input;
    private final IFluidHandler output;

    public InputOutputFluidHandler(IFluidHandler input, IFluidHandler output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int getTanks() {
        return input.getTanks() + output.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank < input.getTanks() ? input.getFluidInTank(tank) : output.getFluidInTank(tank - input.getTanks());
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank < input.getTanks() ? input.getTankCapacity(tank) : output.getTankCapacity(tank - input.getTanks());
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank < input.getTanks() && input.isFluidValid(tank, stack);
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
