package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.block.EnderEmitterBlock;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class EnderEmitterDataProvider implements IBlockComponentProvider {

    public static final EnderEmitterDataProvider INSTANCE = new EnderEmitterDataProvider();

    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/ender_emitter");

    private EnderEmitterDataProvider() {}

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        EnderEmitterBlockEntity emitter = resolveEmitter(accessor);
        if (emitter == null) {
            return;
        }

        if (emitter.isConnectedToBand()) {
            tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.band", emitter.getBandName()));
            tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.band_channels",
                    emitter.getBandUsedChannelsForClient(), emitter.getBandTotalChannelsForClient()));
        }

        tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.linked_channels", emitter.getUsedChannelsForClient()));
        tooltip.add(Component.translatable("jade.ae2cs.ender_emitter.range",
                emitter.getLinkDistance(), emitter.getMaxLinkDistanceForClient()));
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
