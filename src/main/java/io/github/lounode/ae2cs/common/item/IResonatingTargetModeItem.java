package io.github.lounode.ae2cs.common.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IResonatingTargetModeItem
{
    void scrollSelectedInputAndToast(Player player, ItemStack stack, boolean next);
}
