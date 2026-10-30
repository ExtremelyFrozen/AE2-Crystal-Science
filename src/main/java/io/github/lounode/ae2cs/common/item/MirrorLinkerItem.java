package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

public class MirrorLinkerItem extends Item {

    private static final double MAX_BATCH_DISTANCE_SQR = 64.0D;

    public MirrorLinkerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return SimplePatternProviderMirrorHelper.tryBind(stack, context);
        }

        MirrorPatternProviderHost mirrorHost = PatternProviderBindingHelper.resolveClickedMirrorProvider(context);
        if (mirrorHost != null) {
            if (context.getLevel().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            return applyStoredTarget(stack, player, mirrorHost) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        InteractionResult result = SimplePatternProviderMirrorHelper.tryClear(stack, player);
        if (result != InteractionResult.PASS) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_linker.select_source").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_linker.bind_mirror").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_linker.bind_connected").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_linker.clear_source").withStyle(ChatFormatting.GRAY));
        var target = SimplePatternProviderMirrorHelper.getTarget(stack);
        if (target == null) {
            tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.unbound").withStyle(ChatFormatting.GRAY));
            return;
        }

        var pos = target.pos().pos();
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.pos", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.side",
                target.side() == null ? Component.translatable("ae2cs.item.mirror_pattern_provider.target.side.block") : Component.literal(target.side().getName()))
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean applyStoredTarget(ItemStack stack, Player player, MirrorPatternProviderHost host) {
        MirroredPatternProviderTarget target = SimplePatternProviderMirrorHelper.getTarget(stack);
        if (!player.level().isClientSide()) {
            host.getMirroringLogic().setMirrorTarget(target);
            player.displayClientMessage(Component.translatable(target == null ? "ae2cs.msg.mirror_linker.cleared_provider" : "ae2cs.msg.mirror_linker.applied")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return true;
    }

    public static int applyStoredTargetToCluster(ItemStack stack, Player player, Level level, BlockPos centerPos, Vec3 clickLocation) {
        if (level.isClientSide() || !level.hasChunkAt(centerPos)) {
            return 0;
        }

        if (player.distanceToSqr(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D) > MAX_BATCH_DISTANCE_SQR) {
            return 0;
        }

        if (!level.mayInteract(player, centerPos) || !player.mayUseItemAt(centerPos, Direction.UP, stack)) {
            return 0;
        }

        MirrorPatternProviderHost selected = PatternProviderBindingHelper.resolveMirrorProvider(level, centerPos, clickLocation);
        if (selected == null) {
            return 0;
        }

        Set<MirrorPatternProviderHost> hosts = new LinkedHashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> pending = new ArrayDeque<>();

        pending.add(centerPos);
        visited.add(centerPos);

        while (!pending.isEmpty()) {
            BlockPos current = pending.poll();
            if (!level.hasChunkAt(current) || !level.mayInteract(player, current) || !player.mayUseItemAt(current, Direction.UP, stack)) {
                continue;
            }

            List<MirrorPatternProviderHost> atCurrent = PatternProviderBindingHelper.getMirrorProvidersAt(level, current);
            if (atCurrent.isEmpty()) {
                continue;
            }

            hosts.addAll(atCurrent);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (visited.add(next)) {
                    pending.add(next);
                }
            }
        }

        MirroredPatternProviderTarget target = SimplePatternProviderMirrorHelper.getTarget(stack);
        for (MirrorPatternProviderHost host : hosts) {
            host.getMirroringLogic().setMirrorTarget(target);
        }

        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable(target == null ? "ae2cs.msg.mirror_linker.cleared_batch" : "ae2cs.msg.mirror_linker.applied_batch",
                    hosts.size()).withStyle(ChatFormatting.GRAY), true);
        }

        return hosts.size();
    }
}
