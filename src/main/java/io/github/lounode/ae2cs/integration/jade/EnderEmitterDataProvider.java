package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.EnderEmitterBlock;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class EnderEmitterDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final EnderEmitterDataProvider INSTANCE = new EnderEmitterDataProvider();

    private static final String CONNECTED_TO_BAND = "connected_to_band";
    private static final String BAND_NAME = "band_name";
    private static final String BAND_USED_CHANNELS = "band_used_channels";
    private static final String BAND_TOTAL_CHANNELS = "band_total_channels";
    private static final String LINKED_USED_CHANNELS = "linked_used_channels";
    private static final String LINKED_TOTAL_CHANNELS = "linked_total_channels";
    private static final String LINK_DISTANCE = "link_distance";
    private static final String MAX_LINK_DISTANCE = "max_link_distance";
    private static final ResourceLocation UID = AE2CrystalScience.makeId("ender_emitter");

    private EnderEmitterDataProvider() {}

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        EnderEmitterBlockEntity emitter = resolveEmitter(accessor);
        if (emitter == null) {
            return;
        }

        boolean connectedToBand = emitter.isConnectedToBand();
        data.putBoolean(CONNECTED_TO_BAND, connectedToBand);
        data.putInt(LINKED_USED_CHANNELS, emitter.getUsedLinkChannels());
        data.putInt(LINKED_TOTAL_CHANNELS, emitter.getDisplayedMaxLinkChannels());
        data.putInt(LINK_DISTANCE, emitter.getLinkDistance());
        data.putInt(MAX_LINK_DISTANCE, EnderEmitterBlockEntity.maxLinkDistance.get());

        if (!connectedToBand) {
            return;
        }

        data.putString(BAND_NAME, emitter.getBandName());
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(emitter.getBandName());
        if (band != null) {
            data.putLong(BAND_USED_CHANNELS, band.getUsedChannels());
            data.putLong(BAND_TOTAL_CHANNELS, band.getUsableChannels());
        } else {
            data.putLong(BAND_USED_CHANNELS, 0);
            data.putLong(BAND_TOTAL_CHANNELS, 0);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(LINKED_USED_CHANNELS, Tag.TAG_INT) || !data.contains(LINKED_TOTAL_CHANNELS, Tag.TAG_INT)) {
            return;
        }

        if (data.getBoolean(CONNECTED_TO_BAND)) {
            tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.band", data.getString(BAND_NAME)));
            tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.band_channels",
                    data.getLong(BAND_USED_CHANNELS), data.getLong(BAND_TOTAL_CHANNELS)));
        }

        tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.linked_channels",
                data.getInt(LINKED_USED_CHANNELS), data.getInt(LINKED_TOTAL_CHANNELS)));
        tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.range",
                data.getInt(LINK_DISTANCE), data.getInt(MAX_LINK_DISTANCE)));
    }

    private static EnderEmitterBlockEntity resolveEmitter(BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof EnderEmitterBlockEntity emitter) {
            return emitter;
        }

        if (!(accessor.getBlock() instanceof EnderEmitterBlock)) {
            return null;
        }

        if (accessor.getBlockState().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && accessor.getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            BlockEntity lowerBe = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
            if (lowerBe instanceof EnderEmitterBlockEntity emitter) {
                return emitter;
            }
        }

        return null;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
