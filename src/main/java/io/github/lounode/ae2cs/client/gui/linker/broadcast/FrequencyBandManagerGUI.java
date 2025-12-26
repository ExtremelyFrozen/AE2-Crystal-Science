package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FrequencyBandManagerGUI extends AEBaseScreen<FrequencyBandManagerMenu>
{
    private Component bandName = Component.empty();
    private AETextField inputPassword;
    private AE2Button confirmChangePasswordButton;
    private AECheckbox changePublicBox;
    private AECheckbox changeAllowMemoryCardBox;
    private MultiLineTextWidget whiteListText;

    public FrequencyBandManagerGUI(FrequencyBandManagerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_manager_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);

        inputPassword = widgets.addTextField("input_password_field");
        inputPassword.setPlaceholder(Component.translatable("ae2cs.menu.frequency_manager_menu.input_password_field"));
        confirmChangePasswordButton = widgets.addButton(
                "confirm_password_change_button",
                Component.translatable("ae2cs.menu.frequency_manager_menu.confirm_password_button"),
                () -> menu.sendChangePasswordAction(inputPassword.getValue()));
        // 很tm操蛋，为什么tm的checkbox的runable是在自身已经切换完状态后才运行的？？？有点反直觉
        changePublicBox = widgets.addCheckbox(
                "change_public_box",
                Component.translatable("ae2cs.menu.frequency_manager_menu.change_public_box"),
                () -> menu.sendChangePublicAction(changePublicBox.isSelected()));
        changeAllowMemoryCardBox = widgets.addCheckbox(
                "change_allow_memory_card_box",
                Component.translatable("ae2cs.menu.frequency_manager_menu.change_allow_memory_card_box"),
                () -> menu.sendChangeAllowMemoryCardAction(changeAllowMemoryCardBox.isSelected()));
        whiteListText = new MultiLineTextWidget(Component.empty(), this.font);
        widgets.add("whitelist_text", whiteListText);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font, bandName, 10, 20, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        bandName = Component.translatable("ae2cs.menu.frequency_manager_menu.band_name", menu.bandDetailInfo.name());
        changePublicBox.setSelected(menu.bandDetailInfo.isPublic());
        changeAllowMemoryCardBox.setSelected(menu.bandDetailInfo.allowedMemoryCardCopy());

        Component whiteListComponent = Component.translatable("ae2cs.menu.frequency_manager_menu.white_list", String.join(", ", menu.bandDetailInfo.whiteList()));
        whiteListText.setMessage(whiteListComponent);
    }
}
