package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.common.menu.CrystalGrowthAcceleratorMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalGrowthAcceleratorGUI extends AEBaseScreen<CrystalGrowthAcceleratorMenu> {

    public CrystalGrowthAcceleratorGUI(CrystalGrowthAcceleratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_growth_accelerator_menu.json"));
    }
}
