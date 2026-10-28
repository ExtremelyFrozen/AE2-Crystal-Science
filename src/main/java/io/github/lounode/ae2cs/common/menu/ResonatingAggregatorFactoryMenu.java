package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.ResonatingAggregatorFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ResonatingAggregatorFactoryMenu extends AEBaseMenu {

    public ResonatingAggregatorFactoryMenu(int id, Inventory playerInventory, ResonatingAggregatorFactoryBlockEntity host) {
        super(AECSMenus.RESONATING_AGGREGATOR_FACTORY_MENU.get(), id, playerInventory, host);

        for (int slot = 0; slot < host.getInputInv().size(); slot++) {
            addSlot(new AppEngSlot(host.getInputInv(), slot), SlotSemantics.MACHINE_INPUT);
        }
        for (int slot = 0; slot < host.getOutputInv().size(); slot++) {
            addSlot(new AppEngSlot(host.getOutputInv(), slot) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            }, SlotSemantics.MACHINE_OUTPUT);
        }
        createPlayerInventorySlots(playerInventory);
    }
}
