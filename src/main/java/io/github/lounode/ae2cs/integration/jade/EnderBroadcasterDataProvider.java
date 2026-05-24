package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class EnderBroadcasterDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    public static final EnderBroadcasterDataProvider INSTANCE = new EnderBroadcasterDataProvider();

    private static final String USED_CHANNELS = "used_channels";
    private static final String TOTAL_CHANNELS = "total_channels";
    private static final String LOCAL_USED_CHANNELS = "local_used_channels";
    private static final String MODE = "mode";
    private static final String BAND_NAME = "band_name";
    private static final int MODE_SENDER = 1;
    private static final int MODE_RECEIVER = 2;
    private static final int MODE_NONE = 3;
    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/ender_broadcaster");

    private EnderBroadcasterDataProvider()
    {
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor)
    {
        if (!(accessor.getBlockEntity() instanceof EnderBroadcasterBlockEntity broadcaster))
        {
            return;
        }

        String bandName = broadcaster.getBandName();
        if (bandName != null && !bandName.isEmpty())
        {
            data.putString(BAND_NAME, bandName);
        }

        data.putInt(MODE, switch (broadcaster.getConnectionType())
        {
            case AS_SENDER -> MODE_SENDER;
            case AS_RECEIVER -> MODE_RECEIVER;
            case NO_CONNECTION -> MODE_NONE;
        });

        switch (broadcaster.getConnectionType())
        {
            case AS_SENDER -> appendSenderData(data, broadcaster);
            case AS_RECEIVER -> appendReceiverData(data, broadcaster);
            case NO_CONNECTION -> {
                data.putLong(USED_CHANNELS, 0);
                data.putLong(TOTAL_CHANNELS, 0);
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
        data.putLong(LOCAL_USED_CHANNELS, node == null ? 0 : node.getUsedChannels());

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

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(MODE, Tag.TAG_INT))
        {
            return;
        }

        int mode = data.getInt(MODE);
        tooltip.add(Component.translatable("jade.ae2cs.ender_broadcaster.band",
                data.contains(BAND_NAME) ? data.getString(BAND_NAME) : Component.translatable("jade.ae2cs.ender_broadcaster.band.none")));

        tooltip.add(switch (mode)
        {
            case MODE_RECEIVER -> Component.translatable("jade.ae2cs.ender_broadcaster.mode",
                    Component.translatable("jade.ae2cs.ender_broadcaster.mode.receiver"));
            case MODE_SENDER -> Component.translatable("jade.ae2cs.ender_broadcaster.mode",
                    Component.translatable("jade.ae2cs.ender_broadcaster.mode.sender"));
            default -> Component.translatable("jade.ae2cs.ender_broadcaster.mode",
                    Component.translatable("jade.ae2cs.ender_broadcaster.mode.none"));
        });

        if (!data.contains(USED_CHANNELS, Tag.TAG_LONG) || !data.contains(TOTAL_CHANNELS, Tag.TAG_LONG))
        {
            return;
        }

        long used = data.getLong(USED_CHANNELS);
        long total = data.getLong(TOTAL_CHANNELS);
        tooltip.add(switch (mode)
        {
            case MODE_RECEIVER -> Component.translatable("jade.ae2cs.ender_broadcaster.band_channels", used, total);
            case MODE_SENDER -> Component.translatable("jade.ae2cs.ender_broadcaster.sender_channels", used, total);
            default -> Component.translatable("jade.ae2cs.ender_broadcaster.no_connection");
        });

        if (mode == MODE_RECEIVER)
        {
            tooltip.add(Component.translatable("jade.ae2cs.ender_broadcaster.receiver_local_channels", data.getLong(LOCAL_USED_CHANNELS)));
        }
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
