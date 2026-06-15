package io.github.lounode.ae2cs.common.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IScrollCycleItem {

    void scrollSelection(Player player, ItemStack stack, boolean next);
}
