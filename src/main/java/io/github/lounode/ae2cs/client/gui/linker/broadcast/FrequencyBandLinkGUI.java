package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandLinkInfo;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandLinkMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FrequencyBandLinkGUI extends AEBaseScreen<FrequencyBandLinkMenu> {

    private AETextField passwordInput;
    private AECheckbox linkAsSenderBox;
    private AECheckbox linkAsReceiverBox;
    private Button confirmButton;

    public FrequencyBandLinkGUI(FrequencyBandLinkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_link_menu.json"));
        AESubScreen.addBackButton(menu, "back_button", widgets);

        passwordInput = widgets.addTextField("password_input");
        passwordInput.setPlaceholder(Component.translatable("ae2cs.menu.frequency_band_menu.input_password"));
        linkAsSenderBox = widgets.addCheckbox("as_sender_box", Component.translatable("ae2cs.menu.frequency_band_menu.as_sender"), this::asSender);
        linkAsSenderBox.setRadio(true);
        linkAsReceiverBox = widgets.addCheckbox("as_receiver_box", Component.translatable("ae2cs.menu.frequency_band_menu.as_receiver"), this::asReceiver);
        linkAsReceiverBox.setRadio(true);
        confirmButton = widgets.addButton("confirm_button", Component.translatable("ae2cs.menu.frequency_band_menu.confirm"), this::confirm);

        asSender();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        Component linkState = Component.translatable("ae2cs.menu.frequency_band_menu.not_connected");
        if (menu.connected) {
            linkState = Component.translatable("ae2cs.menu.frequency_band_menu.connected");
        }

        setTextContent("link_state", linkState);
    }

    private void confirm() {
        boolean asSender = linkAsSenderBox.isSelected();
        menu.sendLinkToBand(new FrequencyBandLinkInfo(menu.selectedBand, asSender));
    }

    private void asSender() {
        linkAsSenderBox.setSelected(true);
        linkAsReceiverBox.setSelected(false);
    }

    private void asReceiver() {
        linkAsReceiverBox.setSelected(true);
        linkAsSenderBox.setSelected(false);
    }
}
