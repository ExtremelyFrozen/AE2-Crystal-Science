package io.github.lounode.ae2cs.common.item;

import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.localization.GuiText;
import appeng.items.tools.MemoryCardItem;
import appeng.util.InteractionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
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

    private static final String TAG_SELECTED_SLOT = "SelectedSlot";
    private static final String TAG_SLOTS = "Slots";
    private static final String TAG_CONFIG = "Config";
    private static final String TAG_DATA = "Data";

    public ResonatingMemoryCardItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static int getSelectedSlot(ItemStack stack)
    {
        int slot = stack.getOrCreateTag().getInt(TAG_SELECTED_SLOT);
        return Math.max(0, Math.min(SLOT_COUNT - 1, slot));
    }

    public static boolean hasSelectedData(ItemStack stack)
    {
        CompoundTag slotTag = getSelectedSlotTag(stack, false);
        return slotTag != null && !slotTag.getCompound(TAG_DATA).isEmpty();
    }

    private static CompoundTag getSlotsTag(ItemStack stack, boolean create)
    {
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(TAG_SLOTS, Tag.TAG_COMPOUND) && create)
        {
            root.put(TAG_SLOTS, new CompoundTag());
        }
        return root.getCompound(TAG_SLOTS);
    }

    private static @Nullable CompoundTag getSelectedSlotTag(ItemStack stack, boolean create)
    {
        CompoundTag slots = getSlotsTag(stack, create);
        String key = String.valueOf(getSelectedSlot(stack));
        if (!slots.contains(key, Tag.TAG_COMPOUND))
        {
            if (!create)
            {
                return null;
            }
            slots.put(key, new CompoundTag());
        }
        return slots.getCompound(key);
    }

    @Override
    public void setMemoryCardContents(ItemStack is, String settingsName, CompoundTag data)
    {
        CompoundTag slot = getSelectedSlotTag(is, true);
        slot.putString(TAG_CONFIG, settingsName);
        slot.put(TAG_DATA, data.copy());
    }

    @Override
    public String getSettingsName(ItemStack is)
    {
        CompoundTag slot = getSelectedSlotTag(is, false);
        if (slot == null)
        {
            return GuiText.Blank.getTranslationKey();
        }
        String name = slot.getString(TAG_CONFIG);
        return name.isEmpty() ? GuiText.Blank.getTranslationKey() : name;
    }

    @Override
    public CompoundTag getData(ItemStack is)
    {
        CompoundTag slot = getSelectedSlotTag(is, false);
        return slot == null ? new CompoundTag() : slot.getCompound(TAG_DATA).copy();
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
        CompoundTag slots = getSlotsTag(stack, false);
        slots.remove(String.valueOf(getSelectedSlot(stack)));
        notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag advancedTooltips)
    {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        lines.add(Component.translatable("ae2cs.item.resonating_memory_card.slot", getSelectedSlot(stack) + 1, SLOT_COUNT)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void scrollSelection(Player player, ItemStack stack, boolean next)
    {
        int current = getSelectedSlot(stack);
        int target = Math.floorMod(current + (next ? 1 : -1), SLOT_COUNT);
        stack.getOrCreateTag().putInt(TAG_SELECTED_SLOT, target);
        player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_memory_card.slot", target + 1, SLOT_COUNT)
                .withStyle(ChatFormatting.GRAY), true);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, net.minecraft.core.BlockPos pos, Player player)
    {
        return true;
    }
}
