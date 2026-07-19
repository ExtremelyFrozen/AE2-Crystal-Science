package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderReference;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ResonatingLinkerItem extends Item implements IResonatingTargetModeItem {

    public ResonatingLinkerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ResonatingPatternProviderReference reference = PatternProviderBindingHelper.referenceClickedResonatingProvider(context);
        ResonatingPatternProviderHost clickedProvider = PatternProviderBindingHelper.resolveClickedResonatingProvider(context);
        if (reference != null && clickedProvider != null) {
            if (context.getLevel().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            stack.set(AECSDataComponents.RESONATING_LINKER_PROVIDER.get(), reference);
            updateRenderData(stack, clickedProvider);
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.bound_provider")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ResonatingPatternProviderReference providerReference = stack.get(AECSDataComponents.RESONATING_LINKER_PROVIDER.get());
        if (providerReference == null) {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.no_provider")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        ResonatingPatternProviderHost host = providerReference.resolve(context.getLevel());
        if (host == null) {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.provider_unavailable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        int selected = host.getDefaultSelectedInput();
        EncodedResonatingPattern.Target target = new EncodedResonatingPattern.Target(
                GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()), context.getClickedFace());
        Optional<EncodedResonatingPattern.Target> current = host.getDefaultInputTargets().get(selected);
        if (current.isPresent() && current.get().equals(target)) {
            host.setDefaultInputTarget(selected, Optional.empty());
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.unmarked",
                    selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS).withStyle(ChatFormatting.GRAY), true);
        } else {
            host.setDefaultInputTarget(selected, Optional.of(target));
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.marked",
                    selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS,
                    context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(),
                    context.getClickedFace().getName()).withStyle(ChatFormatting.GREEN), true);
        }
        host.markForLogicClientUpdate();
        updateRenderData(stack, host);
        return InteractionResult.CONSUME;
    }

    @Override
    public void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next) {
        ResonatingPatternProviderReference reference = stack.get(AECSDataComponents.RESONATING_LINKER_PROVIDER.get());
        if (reference == null) {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.no_provider")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        ResonatingPatternProviderHost host = reference.resolve(player.level());
        if (host == null) {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_linker.provider_unavailable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int selected = Math.floorMod(host.getDefaultSelectedInput() + (next ? 1 : -1), ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS);
        host.setDefaultSelectedInput(selected);
        host.markForLogicClientUpdate();
        updateRenderData(stack, host);
        Optional<EncodedResonatingPattern.Target> target = host.getDefaultInputTargets().get(selected);
        if (target.isPresent()) {
            var pos = target.get().pos().pos();
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.selected_marked",
                    selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS,
                    pos.getX(), pos.getY(), pos.getZ(), target.get().face().getName()).withStyle(ChatFormatting.GRAY), true);
            return;
        }
        player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_provider.selected_unmarked",
                selected + 1, ResonatingProviderDefaults.DEFAULT_INPUT_SLOTS).withStyle(ChatFormatting.GRAY), true);
    }

    public static boolean hasBoundProvider(ItemStack stack) {
        return stack.has(AECSDataComponents.RESONATING_LINKER_PROVIDER.get());
    }

    /**
     * 将已绑定供应器的当前目标和选中槽位同步为绑定器的客户端渲染快照。
     */
    private static void updateRenderData(ItemStack stack, ResonatingPatternProviderHost host) {
        stack.set(AECSDataComponents.RESONATING_LINKER_RENDER_DATA.get(),
                new ResonatingProviderDefaults.Defaults(host.getDefaultSelectedInput(), host.getDefaultInputTargets()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("ae2cs.item.resonating_linker.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
        ResonatingPatternProviderReference reference = stack.get(AECSDataComponents.RESONATING_LINKER_PROVIDER.get());
        if (reference == null) {
            tooltipComponents.add(Component.translatable("ae2cs.item.resonating_linker.provider.unbound")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        var pos = reference.pos().pos();
        tooltipComponents.add(Component.translatable("ae2cs.item.resonating_linker.provider", pos.getX(), pos.getY(), pos.getZ())
                .withStyle(ChatFormatting.GRAY));
    }
}
