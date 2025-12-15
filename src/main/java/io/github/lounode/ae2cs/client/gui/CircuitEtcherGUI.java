package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import io.github.lounode.ae2cs.common.menu.CircuitEtcherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CircuitEtcherGUI extends UpgradeableScreen<CircuitEtcherMenu>
{

    public CircuitEtcherGUI(CircuitEtcherMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/circuit_etcher_menu.json"));
    }
}
