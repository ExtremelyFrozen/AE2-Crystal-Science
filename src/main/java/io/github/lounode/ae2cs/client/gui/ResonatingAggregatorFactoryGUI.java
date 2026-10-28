package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.common.menu.ResonatingAggregatorFactoryMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingAggregatorFactoryGUI extends AEBaseScreen<ResonatingAggregatorFactoryMenu> {

    public ResonatingAggregatorFactoryGUI(ResonatingAggregatorFactoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/resonating_aggregator_factory_menu.json"));
    }
}
