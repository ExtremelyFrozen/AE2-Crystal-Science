package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.config.AccessRestriction;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MeteoriteCrafterBlockEntity extends AENetworkedSelfPoweredBlockEntity
{
    /**
     * @param pos
     * @param blockState
     */
    public MeteoriteCrafterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.METEORITE_CRAFTER_BLOCK_ENTITY.get(), pos, blockState, 160000);
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return false;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return AccessRestriction.WRITE;
    }
}
