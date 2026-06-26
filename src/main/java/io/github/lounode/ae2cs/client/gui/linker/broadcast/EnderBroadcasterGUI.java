package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderBroadcasterMenu;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterGUI extends UpgradeableScreen<EnderBroadcasterMenu> {

    // 通用
    private final AECSIconButton openFrequencyViewMenuButton;
    private final AECSIconButton openFrequencyBandCreateMenuButton;
    private final AECSIconButton openFrequencyBandManagerMenuButton;
    private final AECSToggleButton toggleBandLinkButton;
    private final AECSIconButton cleanLinkerConnectionButton;

    public EnderBroadcasterGUI(EnderBroadcasterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/ender_broadcaster_menu.json"));

        openFrequencyViewMenuButton = new AECSIconButton(button -> menu.sendFrequencyBandMenuAction()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AECSIcon.BAND_VIEW;
            }
        };
        this.openFrequencyViewMenuButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.open_frequency_view_menu"));
        addToLeftToolbar(openFrequencyViewMenuButton);

        openFrequencyBandCreateMenuButton = new AECSIconButton(button -> menu.sendOpenFrequencyBandCreateMenuAction()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AECSIcon.BAND_CREATE;
            }
        };
        this.openFrequencyBandCreateMenuButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.open_frequency_create_menu"));
        addToLeftToolbar(openFrequencyBandCreateMenuButton);

        openFrequencyBandManagerMenuButton = new AECSIconButton(button -> menu.sendOpenFrequencyBandManagerMenuAction()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AECSIcon.BAND_MANAGER;
            }
        };
        this.openFrequencyBandManagerMenuButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.open_frequency_manager_menu"));
        addToLeftToolbar(openFrequencyBandManagerMenuButton);

        toggleBandLinkButton = new AECSToggleButton(
                AECSIcon.SENDER_STATE, AECSIcon.RECEIVER_STATE,
                Component.translatable("ae2cs.menu.ender_broadcaster.button.toggle_band_side.title"),
                Component.translatable("ae2cs.menu.ender_broadcaster.button.toggle_band_side.desc"),
                state -> menu.sendToggleLinkerSideAction());
        addToLeftToolbar(toggleBandLinkButton);

        cleanLinkerConnectionButton = new AECSIconButton(button -> menu.sendCleanLinkerConnectionAction()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AdaptedAE2Icon.CLEAR;
            }
        };
        this.cleanLinkerConnectionButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.clean_linker_connection"));
        addToLeftToolbar(cleanLinkerConnectionButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        Component bandValue = menu.bandName.isEmpty() ? Component.translatable("ae2cs.menu.ender_broadcaster.none") : Component.literal(menu.bandName);

        Component typeText = switch (menu.connectionType) {
            case AS_SENDER -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.as_sender");
            case AS_RECEIVER -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.as_receiver");
            case NO_CONNECTION -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.no_connection");
        };

        boolean isReceiver = menu.connectionType == EnderBroadcasterBlockEntity.ConnectionType.AS_RECEIVER;
        boolean isSender = menu.connectionType == EnderBroadcasterBlockEntity.ConnectionType.AS_SENDER;

        setTextContent("band_name", Component.translatable("ae2cs.menu.ender_broadcaster.current_band", bandValue));
        setTextContent("connect_status", Component.translatable("ae2cs.menu.ender_broadcaster.connect_status", typeText));
        setTextContent("band_channels", Component.translatable("ae2cs.menu.ender_broadcaster.band_channels", menu.bandUsedChannels, menu.bandTotalChannels));
        setTextHidden("band_channels", !isReceiver && !isSender);

        setTextContent("mode_details_value", isReceiver ? Component.translatable("ae2cs.menu.ender_broadcaster.receiver_actual_channels", menu.receiverUsedChannels) : Component.translatable("ae2cs.menu.ender_broadcaster.sender_sent_channels", menu.senderAvailableChannels));
        setTextHidden("mode_details_value", !isReceiver && !isSender);

        toggleBandLinkButton.setState(isSender);
        toggleBandLinkButton.setVisibility(isReceiver || isSender);

        openFrequencyBandManagerMenuButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);
        cleanLinkerConnectionButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);
    }
}
