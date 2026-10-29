package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.common.menu.ResonatingGrowthFactoryMenu;

import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingGrowthFactoryGUI extends ResonatingFactoryScreen<ResonatingGrowthFactoryMenu> {

    public ResonatingGrowthFactoryGUI(ResonatingGrowthFactoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/resonating_growth_factory_menu.json"));
        addToLeftToolbar(SideConfigGUI.iconButton());
    }
}
