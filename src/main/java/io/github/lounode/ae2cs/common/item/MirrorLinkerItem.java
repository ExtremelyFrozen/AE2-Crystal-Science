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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class MirrorLinkerItem extends Item
{
    public MirrorLinkerItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown())
        {
            return InteractionResult.PASS;
        }

        MirrorPatternProviderHost mirrorHost = PatternProviderBindingHelper.resolveClickedMirrorProvider(context);
        if (mirrorHost != null)
        {
            if (context.getLevel().isClientSide())
            {
                return InteractionResult.SUCCESS;
            }

            return applyStoredTarget(stack, player, mirrorHost)
                    ? InteractionResult.CONSUME
                    : InteractionResult.PASS;
        }

        return SimplePatternProviderMirrorHelper.tryBind(stack, context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
    {
        ItemStack stack = player.getItemInHand(usedHand);
        InteractionResult result = SimplePatternProviderMirrorHelper.tryClear(stack, player);
        if (result != InteractionResult.PASS)
        {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        var target = SimplePatternProviderMirrorHelper.getTarget(stack);
        if (target == null)
        {
            tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.unbound").withStyle(ChatFormatting.GRAY));
            return;
        }

        var pos = target.pos().pos();
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.pos", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.side",
                target.side() == null ? Component.translatable("ae2cs.item.mirror_pattern_provider.target.side.block") : Component.literal(target.side().getName()))
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean applyStoredTarget(ItemStack stack, Player player, MirrorPatternProviderHost host)
    {
        MirroredPatternProviderTarget target = SimplePatternProviderMirrorHelper.getTarget(stack);
        if (!player.level().isClientSide())
        {
            host.getMirroringLogic().setMirrorTarget(target);
            player.displayClientMessage(Component.translatable(target == null
                            ? "ae2cs.msg.mirror_linker.cleared_provider"
                            : "ae2cs.msg.mirror_linker.applied")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return true;
    }

    public static int applyStoredTargetToCluster(ItemStack stack, Player player, Level level, BlockPos centerPos, Vec3 clickLocation)
    {
        Set<MirrorPatternProviderHost> hosts = new LinkedHashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> pending = new ArrayDeque<>();

        pending.add(centerPos);
        MirrorPatternProviderHost selected = PatternProviderBindingHelper.resolveMirrorProvider(level, centerPos, clickLocation);
        if (selected != null)
        {
            hosts.add(selected);
        }

        while (!pending.isEmpty())
        {
            BlockPos current = pending.poll();
            if (!visited.add(current))
            {
                continue;
            }

            List<MirrorPatternProviderHost> atCurrent = PatternProviderBindingHelper.getMirrorProvidersAt(level, current);
            if (atCurrent.isEmpty())
            {
                continue;
            }

            hosts.addAll(atCurrent);
            for (Direction direction : Direction.values())
            {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next))
                {
                    pending.add(next);
                }
            }
        }

        MirroredPatternProviderTarget target = SimplePatternProviderMirrorHelper.getTarget(stack);
        for (MirrorPatternProviderHost host : hosts)
        {
            host.getMirroringLogic().setMirrorTarget(target);
        }

        if (!player.level().isClientSide())
        {
            player.displayClientMessage(Component.translatable(target == null
                            ? "ae2cs.msg.mirror_linker.cleared_batch"
                            : "ae2cs.msg.mirror_linker.applied_batch",
                    hosts.size()).withStyle(ChatFormatting.GRAY), true);
        }

        return hosts.size();
    }
}
