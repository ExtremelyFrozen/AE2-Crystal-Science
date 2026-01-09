package io.github.lounode.ae2cs.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IMachineComponent
{
    default void onConstruct(MachineComponentContainer container)
    {
    }

    default void onLoad(MachineContext ctx)
    {
    }

    default void onServerTick(MachineContext ctx)
    {
    }

    default void onClientTick(MachineContext ctx)
    {
    }

    default void writeNbt(CompoundTag tag, HolderLookup.Provider registries)
    {
    }

    default void readNbt(CompoundTag tag, HolderLookup.Provider registries)
    {
    }

    default boolean readStream(RegistryFriendlyByteBuf data)
    {
        return false;
    }

    default void writeStream(RegistryFriendlyByteBuf data)
    {
    }

    default void addDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
    }

    default void clearContent()
    {
    }
}
