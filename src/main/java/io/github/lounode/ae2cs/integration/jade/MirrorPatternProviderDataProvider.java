package io.github.lounode.ae2cs.integration.jade;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.block.entity.MirrorPatternProviderBlockEntity;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import io.github.lounode.ae2cs.common.me.part.MirrorPatternProviderPart;

import appeng.api.parts.IPartHost;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class MirrorPatternProviderDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final MirrorPatternProviderDataProvider INSTANCE = new MirrorPatternProviderDataProvider();
    private static final String TARGET_X = "target_x";
    private static final String TARGET_Y = "target_y";
    private static final String TARGET_Z = "target_z";
    private static final String TARGET_SIDE = "target_side";
    private static final String HAS_TARGET = "has_target";
    private static final String HAS_TARGET_SIDE = "has_target_side";
    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/mirror_pattern_provider");

    private MirrorPatternProviderDataProvider() {}

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        MirrorPatternProviderHost host = resolveHost(accessor);
        if (host == null) {
            return;
        }

        MirroredPatternProviderTarget target = host.getMirroringLogic().getMirrorTarget();
        data.putBoolean(HAS_TARGET, target != null);
        if (target == null) {
            return;
        }

        var pos = target.pos().pos();
        data.putInt(TARGET_X, pos.getX());
        data.putInt(TARGET_Y, pos.getY());
        data.putInt(TARGET_Z, pos.getZ());
        data.putBoolean(HAS_TARGET_SIDE, target.side() != null);
        if (target.side() != null) {
            data.putString(TARGET_SIDE, target.side().getName());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(HAS_TARGET, Tag.TAG_BYTE)) {
            return;
        }

        if (!data.getBoolean(HAS_TARGET)) {
            tooltip.add(Component.translatable("jade.ae2cs.mirror_pattern_provider.target.unbound"));
            return;
        }

        tooltip.add(Component.translatable("jade.ae2cs.mirror_pattern_provider.target.pos",
                data.getInt(TARGET_X), data.getInt(TARGET_Y), data.getInt(TARGET_Z)));
        tooltip.add(Component.translatable("jade.ae2cs.mirror_pattern_provider.target.side",
                data.getBoolean(HAS_TARGET_SIDE) ? Component.literal(data.getString(TARGET_SIDE)) : Component.translatable("jade.ae2cs.mirror_pattern_provider.target.side.block")));
    }

    private static MirrorPatternProviderHost resolveHost(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MirrorPatternProviderBlockEntity be) {
            return be;
        }

        if (!(accessor.getBlockEntity() instanceof IPartHost partHost) || accessor.getHitResult() == null) {
            return null;
        }

        var selected = partHost.selectPartWorld(accessor.getHitResult().getLocation());
        if (selected.part instanceof MirrorPatternProviderPart part) {
            return part;
        }

        return null;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
