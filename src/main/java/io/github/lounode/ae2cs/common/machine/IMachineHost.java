package io.github.lounode.ae2cs.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public interface IMachineHost
{

    MachineComponentContainer getMachineComponents();

    @Nullable Level getLevel();

    BlockPos getBlockPos();

    BlockState getBlockState();

    boolean isClientSide();

    void markChanged();

    void markForClientUpdate();

    void updateBlockState(BlockState newState, int flags);

    default <T extends Comparable<T>> void updateProperty(Property<T> prop, T value, int flags)
    {
        BlockState cur = getBlockState();
        if (!cur.hasProperty(prop)) return;
        if (cur.getValue(prop).equals(value)) return;
        updateBlockState(cur.setValue(prop, value), flags);
    }

}
