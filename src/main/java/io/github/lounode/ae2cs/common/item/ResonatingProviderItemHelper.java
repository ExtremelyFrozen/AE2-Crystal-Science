package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;

import appeng.util.InteractionUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ResonatingProviderItemHelper {

    private ResonatingProviderItemHelper() {}

    public static @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int selected = ResonatingProviderDefaults.getSelectedInput(stack);
        var clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        var target = new EncodedResonatingPattern.Target(GlobalPos.of(level.dimension(), clickedPos), face);

        List<java.util.Optional<EncodedResonatingPattern.Target>> targets = ResonatingProviderDefaults.readTargets(stack);
        var current = targets.get(selected);
        if (current.isPresent() && current.get().pos().equals(target.pos()) && current.get().face() == target.face()) {
            targets.set(selected, java.util.Optional.empty());
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.unmarked",
                    selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS).withStyle(ChatFormatting.GRAY), true);
        } else {
            targets.set(selected, java.util.Optional.of(target));
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.marked",
                    selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS,
                    clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), face.getName()).withStyle(ChatFormatting.GREEN), true);
        }

        ResonatingProviderDefaults.writeTargets(stack, targets);
        return InteractionResult.CONSUME;
    }

    public static void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next) {
        int selected = ResonatingProviderDefaults.getSelectedInput(stack);
        int nextIdx = Math.floorMod(selected + (next ? 1 : -1), ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS);
        ResonatingProviderDefaults.setSelectedInput(stack, nextIdx);

        var targets = ResonatingProviderDefaults.readTargets(stack);
        var target = targets.get(nextIdx);
        if (target.isPresent()) {
            var pos = target.get().pos().pos();
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.selected_marked",
                    nextIdx + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS,
                    pos.getX(), pos.getY(), pos.getZ(), target.get().face().getName()).withStyle(ChatFormatting.GRAY), true);
        } else {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.selected_unmarked",
                    nextIdx + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS).withStyle(ChatFormatting.GRAY), true);
        }
    }
}
