package io.github.lounode.ae2cs.api.networking;

import appeng.menu.guisync.PacketWritable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

/** Immutable menu-synchronized representation of a machine fluid tank. */
public record FluidTankState(FluidStack fluid, int capacity) implements PacketWritable {

    public FluidTankState {
        fluid = fluid.copy();
    }

    public FluidTankState(RegistryFriendlyByteBuf buffer) {
        this(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarInt());
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
        buffer.writeVarInt(capacity);
    }
}
