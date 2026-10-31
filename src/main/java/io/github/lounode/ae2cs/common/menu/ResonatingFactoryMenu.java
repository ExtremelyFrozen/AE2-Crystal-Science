package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.inventory.ResonatingFactoryUpgradeInventory;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.client.gui.Icon;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/** Shared dedicated card slots, with the standard AE2 slots retained for ordinary machines. */
public abstract class ResonatingFactoryMenu<T extends IUpgradeableObject> extends UpgradeableMenu<T> {

    protected ResonatingFactoryMenu(MenuType<?> type, int id, Inventory inventory, T host) {
        super(type, id, inventory, host);
    }

    @Override
    protected void setupUpgrades() {
        if (!(getUpgrades() instanceof ResonatingFactoryUpgradeInventory)) {
            super.setupUpgrades();
            return;
        }
        for (int slot = 0; slot < getUpgrades().size(); slot++) {
            var upgradeSlot = new AppEngSlot(getUpgrades(), slot);
            upgradeSlot.setIcon(Icon.BACKGROUND_UPGRADE);
            upgradeSlot.setNotDraggable();
            addSlot(upgradeSlot, SlotSemantics.UPGRADE);
        }
    }
}
