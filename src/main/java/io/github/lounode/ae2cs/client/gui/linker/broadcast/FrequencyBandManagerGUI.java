package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FrequencyBandManagerGUI extends AEBaseScreen<FrequencyBandManagerMenu>
{

    public FrequencyBandManagerGUI(FrequencyBandManagerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_manager_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);
    }
}
