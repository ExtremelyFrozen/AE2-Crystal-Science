package io.github.lounode.ae2cs.integration.dataenergistics.production;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;

import net.minecraft.world.item.ItemStack;

import com.fish_dan_.data_energistics.api.upgrade.production.MultiplicationCardOutputs;
import org.jspecify.annotations.NullMarked;

/** Inserts a complete boosted etcher result across its native output slots on the server thread. */
@NullMarked
public final class CrystalScienceItemOutput {

    private CrystalScienceItemOutput() {}

    /** Simulation and failed insertion leave every live slot unchanged. */
    public static ItemStack insert(InternalInventory inventory, ItemStack output, boolean simulate, IUpgradeInventory upgrades) {
        int remaining = output.getCount();
        int[] additions = new int[inventory.size()];
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack current = inventory.getStackInSlot(slot);
            if (!inventory.isItemValid(slot, output) ||
                    !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, output)) {
                continue;
            }
            int capacity = MultiplicationCardOutputs.capacity(inventory, slot, output, upgrades);
            additions[slot] = Math.min(remaining, Math.max(0, capacity - current.getCount()));
            remaining -= additions[slot];
        }
        if (remaining > 0) {
            return output;
        }
        if (!simulate) {
            for (int slot = 0; slot < additions.length; slot++) {
                if (additions[slot] > 0) {
                    inventory.setItemDirect(slot, output.copyWithCount(inventory.getStackInSlot(slot).getCount() + additions[slot]));
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
