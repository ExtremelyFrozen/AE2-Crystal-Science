package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import io.github.lounode.ae2cs.common.menu.EnderEmitterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderEmitterGUI extends UpgradeableScreen<EnderEmitterMenu>
{
    private AE2Button addDistanceButton;
    private AE2Button reduceDistanceButton;
    private AECheckbox autoModeBox;
    private AECheckbox allowAutoLinkCableLikeBox;

    public EnderEmitterGUI(EnderEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        this.addDistanceButton = widgets.addButton(
                "add_distance_button", Component.translatable("ae2cs.menu.ender_emitter.add_distance"),
                () -> {
                    int mult = hasShiftDown() ? 5 : 1;
                    menu.sendChangeDistance(1 * mult);
                });
        this.reduceDistanceButton = widgets.addButton(
                "reduce_distance_button", Component.translatable("ae2cs.menu.ender_emitter.reduce_distance"),
                () -> {
                    int mult = hasShiftDown() ? 5 : 1;
                    menu.sendChangeDistance(-1 * mult);
                });
        this.autoModeBox = widgets.addCheckbox("auto_mode_box",
                Component.translatable("ae2cs.menu.ender_emitter.auto_mode_box"),
                () -> menu.sendChangeAutoMode(autoModeBox.isSelected()));
        this.allowAutoLinkCableLikeBox = widgets.addCheckbox("auto_link_cable_box",
                Component.translatable("ae2cs.menu.ender_emitter.auto_link_cable_box"),
                () -> menu.sendAllowAutoLinkCable(allowAutoLinkCableLikeBox.isSelected()));
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font,
                Component.translatable("ae2cs.menu.ender_emitter.max_distance", menu.maxLinkDistance),
                10, 20, 4210752, false);

        guiGraphics.drawString(this.font,
                Component.translatable("ae2cs.menu.ender_emitter.distance", menu.linkDistance),
                10, 31, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        this.autoModeBox.setSelected(menu.autoMode);
        this.allowAutoLinkCableLikeBox.setSelected(menu.allowAutoLinkCable);
        super.updateBeforeRender();
    }
}
