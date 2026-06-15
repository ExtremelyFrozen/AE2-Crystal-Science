package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;

import appeng.api.parts.IPartHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
