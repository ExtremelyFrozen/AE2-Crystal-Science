package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.config.AccessRestriction;
import appeng.api.networking.pathing.ChannelMode;
import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnderBroadcasterBlockEntity extends AENetworkedSelfPoweredBlockEntity implements CustomChannelProviderHost
{
    private int maxAffordChannels;

    public EnderBroadcasterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState, 80000);
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

    @Override
    public int getMaxChannels()
    {
        return maxAffordChannels;
    }

    @Override
    public void setMaxChannelsWithConfig(int maxChannels)
    {
        this.maxAffordChannels = maxChannels;
    }

    @Override
    public void setMaxChannelsWithOutConfig(int maxChannels)
    {
        getMainNode().ifPresent((iGrid, iGridNode) -> {
            ChannelMode mode = iGrid.getPathingService().getChannelMode();
            if(mode == ChannelMode.INFINITE)
            {
                this.maxAffordChannels = Integer.MAX_VALUE;
                return;
            }
            else
            {
                this.maxAffordChannels = maxChannels * mode.getCableCapacityFactor();
            }
        });
    }
}
