package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.part.MirrorPatternProviderPart;

import appeng.items.parts.PartItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MirrorPatternProviderPartItem extends PartItem<MirrorPatternProviderPart> {

    public MirrorPatternProviderPartItem(Properties properties) {
        super(properties, MirrorPatternProviderPart.class, MirrorPatternProviderPart::new);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return SimplePatternProviderMirrorHelper.tryBind(stack, context);
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        var target = SimplePatternProviderMirrorHelper.getTarget(stack);
        if (target == null) {
            tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.unbound").withStyle(net.minecraft.ChatFormatting.GRAY));
            return;
        }

        var pos = target.pos().pos();
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.pos", pos.getX(), pos.getY(), pos.getZ()).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("ae2cs.item.mirror_pattern_provider.target.side",
                target.side() == null ? Component.translatable("ae2cs.item.mirror_pattern_provider.target.side.block") : Component.literal(target.side().getName()))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
