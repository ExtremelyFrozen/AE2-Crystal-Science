package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.common.menu.CircuitEtcherMenu;

import appeng.client.gui.style.StyleManager;
import appeng.menu.SlotSemantics;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingCircuitEtcherFactoryGUI extends CircuitEtcherGUI {

    public ResonatingCircuitEtcherFactoryGUI(CircuitEtcherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/resonating_circuit_etcher_factory_menu.json"));
    }

    @Override
    protected void init() {
        super.init();
        // The supplied artwork has 19-pixel column spacing; AE2's standard grid uses 18.
        var inputSlots = getMenu().getSlots(SlotSemantics.MACHINE_INPUT);
        var origin = style.getSlots().get(SlotSemantics.MACHINE_INPUT.id());
        for (int index = 0; index < inputSlots.size(); index++) {
            var slot = inputSlots.get(index);
            slot.x = origin.getLeft() + index % 3 * 19;
            slot.y = origin.getTop() + index / 3 * 18;
        }
    }
}
