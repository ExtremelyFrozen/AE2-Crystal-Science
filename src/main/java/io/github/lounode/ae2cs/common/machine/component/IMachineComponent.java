package io.github.lounode.ae2cs.common.machine.component;

import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    default void importSettings(MachineContext ctx, DataComponentMap input, @Nullable Player player)
    {
    }

    default void exportSettings(MachineContext ctx, DataComponentMap.Builder builder, @Nullable Player player)
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
