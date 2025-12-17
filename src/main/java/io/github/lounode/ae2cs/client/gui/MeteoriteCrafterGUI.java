package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import io.github.lounode.ae2cs.common.menu.MeteoriteCrafterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MeteoriteCrafterGUI extends UpgradeableScreen<MeteoriteCrafterMenu>
{
    // 将使用样式 JSON，背景由样式管理
    public MeteoriteCrafterGUI(MeteoriteCrafterMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/meteorite_crafter_menu.json"));

        widgets.addOpenPriorityButton();
    }

}
