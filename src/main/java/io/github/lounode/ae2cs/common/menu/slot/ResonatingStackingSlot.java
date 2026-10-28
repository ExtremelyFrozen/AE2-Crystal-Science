package io.github.lounode.ae2cs.common.menu.slot;

import io.github.lounode.ae2cs.common.inventory.ResonatingStackingInventory;

import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.item.ItemStack;

/** Exposes the machine's per-item capacity to menu insertion as well as automation. */
public class ResonatingStackingSlot extends AppEngSlot {

    private final AppEngInternalInventory inventory;

    public ResonatingStackingSlot(AppEngInternalInventory inventory, int slot) {
        super(inventory, slot);
        this.inventory = inventory;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return inventory instanceof ResonatingStackingInventory stacking ? stacking.getItemLimit(stack) : super.getMaxStackSize(stack);
    }
}
