package io.github.lounode.ae2cs.common.item;

import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.localization.GuiText;
import appeng.items.tools.MemoryCardItem;
import appeng.util.InteractionUtil;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
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
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonatingMemoryCardItem extends MemoryCardItem implements IScrollCycleItem
{
    public static final int SLOT_COUNT = 9;

    public ResonatingMemoryCardItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static int getSelectedSlot(ItemStack stack)
    {
        return storage(stack).selectedSlot();
    }

    public static boolean hasSelectedData(ItemStack stack)
    {
        return !storage(stack).selected().isEmpty();
    }

    public static ResonatingMemoryCardStorage storage(ItemStack stack)
    {
        return stack.getOrDefault(AECSDataComponents.RESONATING_MEMORY_CARD_STORAGE.get(), ResonatingMemoryCardStorage.EMPTY);
    }

    public static void store(ItemStack stack, ResonatingMemoryCardStorage storage)
    {
        stack.set(AECSDataComponents.RESONATING_MEMORY_CARD_STORAGE.get(), storage);
    }

    public void setMemoryCardContents(ItemStack stack, String settingsName, DataComponentMap data)
    {
        store(stack, storage(stack).withSelectedSlotData(settingsName, data));
    }

    public String getSettingsName(ItemStack stack)
    {
        String name = storage(stack).selected().settingsName();
        return name.isEmpty() ? GuiText.Blank.getTranslationKey() : name;
    }

    public static Component getSelectedSlotName(ItemStack stack)
    {
        if (!(stack.getItem() instanceof ResonatingMemoryCardItem card))
        {
            return Component.translatable(GuiText.Blank.getTranslationKey());
        }
        return Component.translatable(card.getSettingsName(stack));
    }

    public DataComponentMap getData(ItemStack stack)
    {
        return storage(stack).selected().data();
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        if (InteractionUtil.isInAlternateUseMode(context.getPlayer()))
        {
            Level level = context.getLevel();
            if (!level.isClientSide())
            {
                clearCurrentSlot(context.getPlayer(), context.getHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        if (InteractionUtil.isInAlternateUseMode(player) && !level.isClientSide)
        {
            clearCurrentSlot(player, hand);
        }
        return super.use(level, player, hand);
    }

    private void clearCurrentSlot(Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        store(stack, storage(stack).clearSelectedSlot());
        notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips)
    {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        lines.add(Component.translatable("ae2cs.item.resonating_memory_card.slot", getSelectedSlot(stack) + 1, SLOT_COUNT)
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("ae2cs.item.resonating_memory_card.target", getSelectedSlotName(stack))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void scrollSelection(Player player, ItemStack stack, boolean next)
    {
        int current = getSelectedSlot(stack);
        int target = Math.floorMod(current + (next ? 1 : -1), SLOT_COUNT);
        store(stack, storage(stack).withSelectedSlot(target));
        player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_memory_card.slot", target + 1, SLOT_COUNT, getSelectedSlotName(stack))
                .withStyle(ChatFormatting.GRAY), true);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, net.minecraft.core.BlockPos pos, Player player)
    {
        return true;
    }
}
