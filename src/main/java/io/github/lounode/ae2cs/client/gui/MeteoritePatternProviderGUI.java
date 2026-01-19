package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.common.menu.MeteoritePatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MeteoritePatternProviderGUI extends UpgradeablePatternProviderGUI<MeteoritePatternProviderMenu>
{
    public MeteoritePatternProviderGUI(MeteoritePatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);
    }
}
