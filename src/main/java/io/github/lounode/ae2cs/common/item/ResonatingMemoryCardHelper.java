package io.github.lounode.ae2cs.common.item;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.SettingsFrom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class ResonatingMemoryCardHelper
{
    private ResonatingMemoryCardHelper()
    {
    }

    public static @Nullable ItemStack findActiveCard(Player player)
    {
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.getItem() instanceof ResonatingMemoryCardItem && ResonatingMemoryCardItem.hasSelectedData(stack))
            {
                return stack;
            }
        }
        return null;
    }

    public static void tryApplyToBlockEntity(Player player, BlockEntity blockEntity)
    {
        if (!(blockEntity instanceof AEBaseBlockEntity aeBe))
        {
            return;
        }

        ItemStack card = findActiveCard(player);
        if (card == null)
        {
            return;
        }

        CompoundTag data = ((ResonatingMemoryCardItem) card.getItem()).getData(card);
        if (data.isEmpty())
        {
            return;
        }

        aeBe.importSettings(SettingsFrom.MEMORY_CARD, data, player);
    }

    public static void tryApplyToPart(Player player, IPart part)
    {
        ItemStack card = findActiveCard(player);
        if (card == null)
        {
            return;
        }

        CompoundTag data = ((ResonatingMemoryCardItem) card.getItem()).getData(card);
        if (data.isEmpty())
        {
            return;
        }

        part.importSettings(SettingsFrom.MEMORY_CARD, data, player);
    }
}
