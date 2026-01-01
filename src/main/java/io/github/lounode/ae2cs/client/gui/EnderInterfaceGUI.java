package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import io.github.lounode.ae2cs.common.menu.EnderInterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderInterfaceGUI extends InterfaceScreen<EnderInterfaceMenu>
{
    private AECheckbox blackListModeBox;
    private AE2Button addRangeButton;
    private AE2Button reduceRangeButton;

    public EnderInterfaceGUI(EnderInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        blackListModeBox = widgets.addCheckbox(
                "block_list_mode_box",
                Component.translatable("ae2cs.menu.ender_interface.black_list_mode_box"),
                () -> menu.sendChangeBlackListMode(blackListModeBox.isSelected()));

        addRangeButton = widgets.addButton(
                "add_range_button",
                Component.translatable("ae2cs.menu.ender_interface.add_range"),
                () -> menu.sendChangeAbsorbRange(1));

        reduceRangeButton = widgets.addButton(
                "reduce_range_button",
                Component.translatable("ae2cs.menu.ender_interface.reduce_range"),
                () -> menu.sendChangeAbsorbRange(-1));
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font, String.valueOf(menu.absorbRange), 108, 24, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();
        blackListModeBox.setSelected(menu.blackListMode);
    }
}
