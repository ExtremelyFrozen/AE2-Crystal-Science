package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.networking.pathing.ChannelMode;
import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class EnderBroadcasterBlockEntity extends AENetworkedSelfPoweredBlockEntity implements CustomChannelProviderHost
{
    private String bandId = "";
    private ConnectionType connectionType = ConnectionType.NO_CONNECTION;

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

    public void connectToBand(String bandId, boolean asSender)
    {
        if(level == null || level.isClientSide()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if(band == null) return;
        if(asSender)
        {
            band.removeReceiver(level, worldPosition);
            band.updateSender(level, worldPosition);
            this.connectionType = ConnectionType.AS_SENDER;
        }
        else
        {
            band.removeSender(level, worldPosition);
            band.updateReceiver(level, worldPosition);
            this.connectionType = ConnectionType.AS_RECEIVER;
        }
        this.bandId = band.getName();
    }

    public void cleanConnection()
    {
        if(level == null || level.isClientSide()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(this.bandId);
        if(band == null) return;

        band.removeReceiver(level, worldPosition);
        band.removeSender(level, worldPosition);
        this.bandId = "";
        this.connectionType = ConnectionType.NO_CONNECTION;
    }

    public void cleanConnectionTemporary()
    {
        if(level == null || level.isClientSide()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(this.bandId);
        if(band == null) return;

        band.removeReceiver(level, worldPosition);
        band.removeSender(level, worldPosition);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        data.putString("bandId", bandId);
        data.putString("connectionType", connectionType.name());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        bandId = data.getString("bandId");
        connectionType = ConnectionType.valueOf(data.getString("connectionType"));
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        cleanConnectionTemporary(); // 区块卸载时，临时断开链接，但不清除持久化状态
    }

    /** 在这里重新启用链接，因为只有在这里，网络才初始化完毕 */
    @Override
    public void onReady()
    {
        super.onReady();
        if(connectionType == ConnectionType.AS_RECEIVER)
            connectToBand(bandId, false);
        else if(connectionType == ConnectionType.AS_SENDER)
            connectToBand(bandId, true);
        else
            cleanConnection();
    }

    private static enum ConnectionType
    {
        AS_SENDER,
        AS_RECEIVER,
        NO_CONNECTION
    }
}
