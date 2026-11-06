package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderReference;
import io.github.lounode.ae2cs.common.me.logic.mirroring.PatternProviderSources;

import appeng.api.parts.IPartHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PatternProviderBindingHelper {

    private PatternProviderBindingHelper() {}

    public static @Nullable PatternProviderLogicHost resolveClickedPatternProvider(UseOnContext context) {
        return resolvePatternProvider(context.getLevel(), context.getClickedPos(), context.getClickLocation());
    }

    /** 返回普通或高级主供应器的精确方块/面板引用；不接受镜像作为主源。 */
    public static @Nullable MirroredPatternProviderTarget referenceClickedMirrorSource(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        GlobalPos pos = GlobalPos.of(context.getLevel().dimension(), context.getClickedPos());
        if (blockEntity instanceof IPartHost partHost) {
            var selected = partHost.selectPartWorld(context.getClickLocation());
            if (PatternProviderSources.fromHost(selected.part) != null) {
                return new MirroredPatternProviderTarget(pos, selected.side);
            }
        }
        return PatternProviderSources.fromHost(blockEntity) == null ? null : new MirroredPatternProviderTarget(pos, null);
    }

    public static @Nullable PatternProviderLogicHost resolvePatternProvider(Level level, BlockPos pos, Vec3 clickLocation) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IPartHost partHost) {
            var selected = partHost.selectPartWorld(clickLocation);
            if (selected.part instanceof PatternProviderLogicHost host) {
                return host;
            }
        }

        return be instanceof PatternProviderLogicHost host ? host : null;
    }

    public static @Nullable ResonatingPatternProviderHost resolveClickedResonatingProvider(UseOnContext context) {
        PatternProviderLogicHost host = resolveClickedPatternProvider(context);
        return host instanceof ResonatingPatternProviderHost resonating ? resonating : null;
    }

    /**
     * 生成可在后续交互中重新定位当前点击供应器的引用。
     */
    public static @Nullable ResonatingPatternProviderReference referenceClickedResonatingProvider(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof ResonatingPatternProviderHost) {
            return new ResonatingPatternProviderReference(
                    GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), null);
        }
        if (blockEntity instanceof IPartHost partHost) {
            var selected = partHost.selectPartWorld(context.getClickLocation());
            if (selected.part instanceof ResonatingPatternProviderHost) {
                return new ResonatingPatternProviderReference(
                        GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), context.getClickedFace());
            }
        }
        return null;
    }

    public static @Nullable MirrorPatternProviderHost resolveClickedMirrorProvider(UseOnContext context) {
        PatternProviderLogicHost host = resolveClickedPatternProvider(context);
        return host instanceof MirrorPatternProviderHost mirror ? mirror : null;
    }

    public static @Nullable MirrorPatternProviderHost resolveMirrorProvider(Level level, BlockPos pos, Vec3 clickLocation) {
        PatternProviderLogicHost host = resolvePatternProvider(level, pos, clickLocation);
        return host instanceof MirrorPatternProviderHost mirror ? mirror : null;
    }

    public static List<MirrorPatternProviderHost> getMirrorProvidersAt(Level level, BlockPos pos) {
        List<MirrorPatternProviderHost> hosts = new ArrayList<>();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MirrorPatternProviderHost mirror) {
            hosts.add(mirror);
        }

        if (be instanceof IPartHost partHost) {
            for (Direction side : Direction.values()) {
                var part = partHost.getPart(side);
                if (part instanceof MirrorPatternProviderHost mirror) {
                    hosts.add(mirror);
                }
            }
        }

        return hosts;
    }
}
