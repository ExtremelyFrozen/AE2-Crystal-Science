package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.common.menu.ResonatingFactoryMenu;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** All four factories use AE2's standard compatible-upgrades panel and tooltip. */
public class ResonatingFactoryScreen<T extends ResonatingFactoryMenu<?>> extends UpgradeableScreen<T> {

    protected ResonatingFactoryScreen(T menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }
}
