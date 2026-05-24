package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.crafting.ResonatingProviderDefaults;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class ResonatingLinkerItem extends Item implements IResonatingTargetModeItem
{
    public ResonatingLinkerItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ResonatingPatternProviderHost host = PatternProviderBindingHelper.resolveClickedResonatingProvider(context);
        if (player.isShiftKeyDown() && host != null)
        {
            if (context.getLevel().isClientSide())
            {
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
    public void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next)
    {
        ResonatingProviderItemHelper.scrollSelectedInputAndToast(player, stack, next);
    }

    public static boolean hasStoredTargets(ItemStack stack)
    {
        return ResonatingProviderDefaults.hasAnyTarget(ResonatingProviderDefaults.readTargets(stack));
    }
}
