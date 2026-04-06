package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class EnderChannelDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    public static final EnderChannelDataProvider INSTANCE = new EnderChannelDataProvider();

    private static final String USED_CHANNELS = "used_channels";
    private static final String TOTAL_CHANNELS = "total_channels";
    private static final String MODE = "mode";
    private static final String BAND_NAME = "band_name";
    private static final int MODE_EMITTER = 0;
    private static final int MODE_BROADCASTER_SENDER = 1;
    private static final int MODE_BROADCASTER_RECEIVER = 2;
    private static final int MODE_BROADCASTER_NONE = 3;
    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/ender_channels");

    private EnderChannelDataProvider()
    {
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor)
    {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof EnderEmitterBlockEntity emitter)
        {
            data.putLong(USED_CHANNELS, emitter.getUsedLinkChannels());
            data.putLong(TOTAL_CHANNELS, emitter.getMaxLinkChannels());
            data.putInt(MODE, MODE_EMITTER);
        }
        else if (blockEntity instanceof EnderBroadcasterBlockEntity broadcaster)
        {
            String bandName = broadcaster.getBandName();
            if (bandName != null && !bandName.isEmpty())
            {
                data.putString(BAND_NAME, bandName);
            }

            data.putInt(MODE, switch (broadcaster.getConnectionType())
            {
                case AS_SENDER -> MODE_BROADCASTER_SENDER;
                case AS_RECEIVER -> MODE_BROADCASTER_RECEIVER;
                case NO_CONNECTION -> MODE_BROADCASTER_NONE;
            });

            switch (broadcaster.getConnectionType())
            {
                case AS_SENDER -> appendSenderData(data, broadcaster);
                case AS_RECEIVER -> appendReceiverData(data, broadcaster);
                case NO_CONNECTION ->
                {
                    data.putLong(USED_CHANNELS, 0);
                    data.putLong(TOTAL_CHANNELS, 0);
                }
            }
        }
    }

    private static void appendSenderData(CompoundTag data, EnderBroadcasterBlockEntity broadcaster)
    {
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(broadcaster.getBandName());
        if (band == null)
        {
            data.putLong(USED_CHANNELS, 0);
            data.putLong(TOTAL_CHANNELS, 0);
            return;
        }

        data.putLong(USED_CHANNELS, band.getUsedChannels());
        data.putLong(TOTAL_CHANNELS, band.getUsableChannels());
    }

    private static void appendReceiverData(CompoundTag data, EnderBroadcasterBlockEntity broadcaster)
    {
        var node = broadcaster.getMainNode().getNode();
        if (node == null)
        {
            data.putLong(USED_CHANNELS, 0);
            data.putLong(TOTAL_CHANNELS, 0);
            return;
        }

        data.putLong(USED_CHANNELS, node.getUsedChannels());
        data.putLong(TOTAL_CHANNELS, node.getMaxChannels());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(USED_CHANNELS, Tag.TAG_LONG) || !data.contains(TOTAL_CHANNELS, Tag.TAG_LONG))
        {
            return;
        }

        long used = data.getLong(USED_CHANNELS);
        long total = data.getLong(TOTAL_CHANNELS);
        int mode = data.getInt(MODE);

        if (mode != MODE_EMITTER)
        {
            tooltip.add(switch (mode)
            {
                case MODE_BROADCASTER_RECEIVER -> Component.translatable("jade.ae2cs.ender_broadcaster.mode", Component.translatable("jade.ae2cs.ender_broadcaster.mode.receiver"));
                case MODE_BROADCASTER_SENDER -> Component.translatable("jade.ae2cs.ender_broadcaster.mode", Component.translatable("jade.ae2cs.ender_broadcaster.mode.sender"));
                default -> Component.translatable("jade.ae2cs.ender_broadcaster.mode", Component.translatable("jade.ae2cs.ender_broadcaster.mode.none"));
            });

            tooltip.add(Component.translatable("jade.ae2cs.ender_broadcaster.band", data.contains(BAND_NAME) ? data.getString(BAND_NAME) : Component.translatable("jade.ae2cs.ender_broadcaster.band.none")));
        }

        tooltip.add(switch (mode)
        {
            case MODE_BROADCASTER_RECEIVER -> Component.translatable("jade.ae2cs.ender_broadcaster.receiver_channels", used, total);
            case MODE_BROADCASTER_SENDER -> Component.translatable("jade.ae2cs.ender_broadcaster.sender_channels", used, total);
            case MODE_BROADCASTER_NONE -> Component.translatable("jade.ae2cs.ender_broadcaster.no_connection");
            default -> Component.translatable("jade.ae2cs.ender_emitter.channels", used, total);
        });
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
