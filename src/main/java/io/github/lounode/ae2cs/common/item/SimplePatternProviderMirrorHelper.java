package io.github.lounode.ae2cs.common.item;

import appeng.api.parts.IPartHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import io.github.lounode.ae2cs.common.me.logic.MirroredSimplePatternProviderHost;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class SimplePatternProviderMirrorHelper
{
    private static final String TAG_MIRROR = "mirror_target";

    private SimplePatternProviderMirrorHelper()
    {
    }

    public static @NotNull InteractionResult tryBind(ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return InteractionResult.PASS;

        PatternProviderLogicHost targetHost = resolveClickedTargetHost(context);
        if (targetHost == null)
        {
            return InteractionResult.PASS;
        }

        if (targetHost instanceof MirroredSimplePatternProviderHost mirrored && mirrored.getMirroringLogic().isMirroring())
        {
            if (!context.getLevel().isClientSide())
            {
                player.displayClientMessage(Component.translatable("ae2cs.msg.simple_pattern_provider.mirror.invalid_target").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.CONSUME;
        }

        MirroredPatternProviderTarget target;
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof IPartHost partHost)
        {
            var selected = partHost.selectPartWorld(context.getClickLocation());
            if (selected.part instanceof PatternProviderLogicHost)
            {
                target = new MirroredPatternProviderTarget(GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), selected.side);
            }
            else
            {
                target = new MirroredPatternProviderTarget(GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), null);
            }
        }
        else
        {
            target = new MirroredPatternProviderTarget(GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), null);
        }

        if (!context.getLevel().isClientSide())
        {
            var tag = stack.getOrCreateTag();
            MirroredPatternProviderTarget.write(target, tag);
            player.displayClientMessage(Component.translatable("ae2cs.msg.simple_pattern_provider.mirror.bound",
                    context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }

    public static @NotNull InteractionResult tryClear(ItemStack stack, Player player)
    {
        if (!player.isShiftKeyDown())
        {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide())
        {
            MirroredPatternProviderTarget.write(null, stack.getOrCreateTag());
            player.displayClientMessage(Component.translatable("ae2cs.msg.simple_pattern_provider.mirror.cleared").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    public static @Nullable MirroredPatternProviderTarget getTarget(ItemStack stack)
    {
        return MirroredPatternProviderTarget.read(stack.getOrCreateTag());
    }

    private static PatternProviderLogicHost resolveClickedTargetHost(UseOnContext context)
    {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof IPartHost partHost)
        {
            var selected = partHost.selectPartWorld(context.getClickLocation());
            if (selected.part instanceof PatternProviderLogicHost host)
            {
                return host;
            }
        }
        return be instanceof PatternProviderLogicHost host ? host : null;
    }
}
