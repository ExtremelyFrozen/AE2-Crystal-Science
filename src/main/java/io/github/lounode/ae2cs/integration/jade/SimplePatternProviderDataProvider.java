package io.github.lounode.ae2cs.integration.jade;

import appeng.api.parts.IPartHost;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.block.entity.SimplePatternProviderBlockEntity;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import io.github.lounode.ae2cs.common.me.logic.MirroredSimplePatternProviderHost;
import io.github.lounode.ae2cs.common.me.part.SimplePatternProviderPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class SimplePatternProviderDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    public static final SimplePatternProviderDataProvider INSTANCE = new SimplePatternProviderDataProvider();
    private static final String MODE = "mode";
    private static final String TARGET_X = "target_x";
    private static final String TARGET_Y = "target_y";
    private static final String TARGET_Z = "target_z";
    private static final String TARGET_SIDE = "target_side";
    private static final String HAS_TARGET_SIDE = "has_target_side";
    private static final int MODE_NORMAL = 0;
    private static final int MODE_MIRROR = 1;
    private static final ResourceLocation UID = AE2CrystalScience.makeId("jade/simple_pattern_provider");

    private SimplePatternProviderDataProvider()
    {
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor)
    {
        MirroredSimplePatternProviderHost host = resolveHost(accessor);
        if (host == null)
        {
            return;
        }

        MirroredPatternProviderTarget target = host.getMirroringLogic().getMirrorTarget();
        if (target == null)
        {
            data.putInt(MODE, MODE_NORMAL);
            return;
        }

        data.putInt(MODE, MODE_MIRROR);
        var pos = target.pos().pos();
        data.putInt(TARGET_X, pos.getX());
        data.putInt(TARGET_Y, pos.getY());
        data.putInt(TARGET_Z, pos.getZ());
        data.putBoolean(HAS_TARGET_SIDE, target.side() != null);
        if (target.side() != null)
        {
            data.putString(TARGET_SIDE, target.side().getName());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(MODE, Tag.TAG_INT))
        {
            return;
        }

        if (data.getInt(MODE) == MODE_NORMAL)
        {
            tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.mode",
                    Component.translatable("jade.ae2cs.simple_pattern_provider.mode.normal")));
            return;
        }

        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.mode",
                Component.translatable("jade.ae2cs.simple_pattern_provider.mode.mirror")));
        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.target.pos",
                data.getInt(TARGET_X), data.getInt(TARGET_Y), data.getInt(TARGET_Z)));
        tooltip.add(Component.translatable("jade.ae2cs.simple_pattern_provider.target.side",
                data.getBoolean(HAS_TARGET_SIDE)
                        ? Component.literal(data.getString(TARGET_SIDE))
                        : Component.translatable("jade.ae2cs.simple_pattern_provider.target.side.block")));
    }

    private static MirroredSimplePatternProviderHost resolveHost(BlockAccessor accessor)
    {
        if (accessor.getBlockEntity() instanceof SimplePatternProviderBlockEntity be)
        {
            return be;
        }

        if (!(accessor.getBlockEntity() instanceof IPartHost partHost) || accessor.getHitResult() == null)
        {
            return null;
        }

        var selected = partHost.selectPartWorld(accessor.getHitResult().getLocation());
        if (selected.part instanceof SimplePatternProviderPart part)
        {
            return part;
        }

        return null;
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
