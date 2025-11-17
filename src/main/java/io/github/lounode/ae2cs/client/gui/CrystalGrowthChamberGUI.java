package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import io.github.lounode.ae2cs.common.menu.CrystalGrowthChamberMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalGrowthChamberGUI extends UpgradeableScreen<CrystalGrowthChamberMenu>
{
    // 将使用样式 JSON，背景由样式管理
    public CrystalGrowthChamberGUI(CrystalGrowthChamberMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/crystal_growth_chamber_menu.json"));
    }
}