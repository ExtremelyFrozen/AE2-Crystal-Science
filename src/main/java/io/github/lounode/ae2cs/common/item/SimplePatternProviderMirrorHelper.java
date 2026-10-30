package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class SimplePatternProviderMirrorHelper {

    private SimplePatternProviderMirrorHelper() {}

    public static @NotNull InteractionResult tryBind(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return InteractionResult.PASS;

        if (PatternProviderBindingHelper.resolveClickedMirrorProvider(context) != null) {
            if (!context.getLevel().isClientSide()) {
                player.displayClientMessage(Component.translatable("ae2cs.msg.mirror_pattern_provider.invalid_target").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.CONSUME;
        }

        MirroredPatternProviderTarget target = PatternProviderBindingHelper.referenceClickedMirrorSource(context);
        if (target == null) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide()) {
            stack.set(AECSDataComponents.MIRROR_PATTERN_PROVIDER_TARGET.get(), target);
            player.displayClientMessage(Component.translatable("ae2cs.msg.mirror_pattern_provider.bound",
                    context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    public static @NotNull InteractionResult tryClear(ItemStack stack, Player player) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            stack.remove(AECSDataComponents.MIRROR_PATTERN_PROVIDER_TARGET.get());
            player.displayClientMessage(Component.translatable("ae2cs.msg.mirror_pattern_provider.cleared").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    public static @Nullable MirroredPatternProviderTarget getTarget(ItemStack stack) {
        return stack.get(AECSDataComponents.MIRROR_PATTERN_PROVIDER_TARGET.get());
    }
}
