package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ResonatingLinkerItem extends Item implements IResonatingTargetModeItem {

    private static final int MAX_BATCH_RADIUS = 16;
    private static final int MAX_BATCH_PROVIDERS = 256;
    private static final double MAX_BATCH_DISTANCE_SQR = 64.0D;

    public ResonatingLinkerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ResonatingPatternProviderHost host = PatternProviderBindingHelper.resolveClickedResonatingProvider(context);
        if (player.isShiftKeyDown() && host != null) {
            if (context.getLevel().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            host.readDefaultsFromItem(stack);
            host.markForLogicClientUpdate();
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.applied")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }

        return ResonatingProviderItemHelper.onItemUseFirst(stack, context);
    }

    @Override
    public void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next) {
        ResonatingProviderItemHelper.scrollSelectedInputAndToast(player, stack, next);
    }

    public static boolean hasStoredTargets(ItemStack stack) {
        return ResonatingProviderDefaults.hasAnyTarget(ResonatingProviderDefaults.readTargets(stack));
    }

    public static int applyStoredTargetsToCluster(ItemStack stack, Player player, Level level, BlockPos centerPos, Vec3 clickLocation) {
        if (level.isClientSide() || !level.hasChunkAt(centerPos)) {
            return 0;
        }

        if (player.distanceToSqr(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D) > MAX_BATCH_DISTANCE_SQR) {
            return 0;
        }

        if (!player.mayUseItemAt(centerPos, Direction.UP, stack)) {
            return 0;
        }

        ResonatingPatternProviderHost selected = PatternProviderBindingHelper.resolveResonatingProvider(level, centerPos, clickLocation);
        if (selected == null) {
            return 0;
        }

        Set<ResonatingPatternProviderHost> hosts = new LinkedHashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> pending = new ArrayDeque<>();
        pending.add(centerPos);

        while (!pending.isEmpty() && hosts.size() < MAX_BATCH_PROVIDERS) {
            BlockPos current = pending.poll();
            if (!visited.add(current) || !isWithinBatchBounds(centerPos, current) || !level.hasChunkAt(current)) {
                continue;
            }

            List<ResonatingPatternProviderHost> atCurrent = PatternProviderBindingHelper.getResonatingProvidersAt(level, current).stream()
                    .filter(host -> host.getClass() == selected.getClass())
                    .toList();
            if (atCurrent.isEmpty()) {
                continue;
            }

            hosts.addAll(atCurrent);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next) && isWithinBatchBounds(centerPos, next)) {
                    pending.add(next);
                }
            }
        }

        for (ResonatingPatternProviderHost host : hosts) {
            host.readDefaultsFromItem(stack);
            host.markForLogicClientUpdate();
        }

        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.applied")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return hosts.size();
    }

    private static boolean isWithinBatchBounds(BlockPos center, BlockPos pos) {
        return Math.abs(pos.getX() - center.getX()) <= MAX_BATCH_RADIUS && Math.abs(pos.getY() - center.getY()) <= MAX_BATCH_RADIUS && Math.abs(pos.getZ() - center.getZ()) <= MAX_BATCH_RADIUS;
    }
}
