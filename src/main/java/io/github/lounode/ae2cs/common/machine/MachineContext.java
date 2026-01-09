package io.github.lounode.ae2cs.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record MachineContext(
        IMachineHost host,
        Level level,
        BlockPos pos,
        BlockState state
)
{
    public boolean isServer()
    {
        return level != null && !level.isClientSide();
    }

    public boolean isClient()
    {
        return level != null && !level.isClientSide();
    }
}