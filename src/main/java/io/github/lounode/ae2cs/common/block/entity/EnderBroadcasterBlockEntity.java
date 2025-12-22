package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.networking.pathing.ChannelMode;
import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class EnderBroadcasterBlockEntity extends AENetworkedSelfPoweredBlockEntity implements CustomChannelProviderHost
{
    private int maxAffordChannels;

    public EnderBroadcasterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState, 80000);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.ENDER_BROADCASTER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
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
            if (mode == ChannelMode.INFINITE)
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
