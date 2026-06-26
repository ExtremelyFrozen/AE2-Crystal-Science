package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandCreateInfo;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandCreateMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public class FrequencyBandCreateGUI extends AEBaseScreen<FrequencyBandCreateMenu> {

    private AETextField bandNameTextField;
    private AETextField passwordTextField;
    private AECheckbox isPublicBox;
    private AECheckbox allowedMemoryCardCopyBox;
    private Button confirmButton;

    public FrequencyBandCreateGUI(FrequencyBandCreateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_create_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);

        bandNameTextField = widgets.addTextField("band_name_field");
        bandNameTextField.setPlaceholder(Component.translatable("ae2cs.menu.frequency_band_create_menu.input_band_name"));
        passwordTextField = widgets.addTextField("password_field");
        passwordTextField.setPlaceholder(Component.translatable("ae2cs.menu.frequency_band_create_menu.input_password"));
        isPublicBox = widgets.addCheckbox("is_public_box",
                Component.translatable("ae2cs.menu.frequency_band_create_menu.is_public"),
                () -> {});
        allowedMemoryCardCopyBox = widgets.addCheckbox("allow_memory_card_copy",
                Component.translatable("ae2cs.menu.frequency_band_create_menu.allow_memory_card_copy"),
                () -> {});
        confirmButton = widgets.addButton("confirm_button",
                Component.translatable("ae2cs.menu.frequency_band_create_menu.confirm"),
                this::sendConfirm);
    }

    private void sendConfirm() {
        String bandName = bandNameTextField.getValue();
        String password = passwordTextField.getValue();
        UUID ownerId = getMenu().getPlayer().getUUID();
        boolean isPublic = isPublicBox.isSelected();
        boolean allowedMemoryCardCopy = allowedMemoryCardCopyBox.isSelected();

        if (bandName.isEmpty()) return; // TODO 添加提示信息

        FrequencyBandCreateInfo createInfo = new FrequencyBandCreateInfo(bandName, password, ownerId, isPublic, allowedMemoryCardCopy);
        getMenu().sendCreateBand(createInfo);
    }
}
