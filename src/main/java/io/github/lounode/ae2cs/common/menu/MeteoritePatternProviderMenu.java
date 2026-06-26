package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;

import appeng.api.upgrades.IUpgradeInventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class MeteoritePatternProviderMenu extends UpgradeablePatternProviderMenu {

    private final MeteoritePatternProviderHost host;

    public MeteoritePatternProviderMenu(MenuType<? extends UpgradeablePatternProviderMenu> menuType, int id, Inventory playerInventory, MeteoritePatternProviderHost host) {
        super(menuType, id, playerInventory, host);
        this.host = host;
    }

    @Override
    public final IUpgradeInventory getUpgrades() {
        return host.getUpgrades();
    }
}
