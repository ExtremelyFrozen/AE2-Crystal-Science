package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastReceiverHost;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.client.gui.widgets.AdjustmentButtons;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderBroadcasterMenu;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.util.ReadableNumberConverter;

import net.minecraft.client.gui.components.Tooltip;
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
    // 接收端
    private final AE2Button addReceiver1ChannelsButton;
    private final AE2Button addReceiver10ChannelsButton;
    private final AE2Button reduceReceiver1ChannelsButton;
    private final AE2Button reduceReceiver10ChannelsButton;
    private final AE2Button channelLimitButton;

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

        addReceiver1ChannelsButton = channelStepButton("add_receiver_expected_channels_button_1", 1);
        addReceiver10ChannelsButton = channelStepButton("add_receiver_expected_channels_button_10", 10);
        reduceReceiver1ChannelsButton = channelStepButton("reduce_receiver_expected_channels_button_1", -1);
        reduceReceiver10ChannelsButton = channelStepButton("reduce_receiver_expected_channels_button_10", -10);
        channelLimitButton = new AE2Button(Component.empty(), button -> menu.sendToggleUnlimitedChannels());
        channelLimitButton.setTooltip(Tooltip.create(Component.translatable("ae2cs.menu.toggle_unlimited_channels")));
        widgets.add("channel_limit", channelLimitButton);
    }

    private AE2Button channelStepButton(String id, int delta) {
        AE2Button button = new AE2Button(Component.literal(delta > 0 ? "+" + delta : Integer.toString(delta)),
                pressed -> menu.sendChangeExpectedChannels(AdjustmentButtons.step(delta, menu.receiverExpectedChannels)));
        button.setTooltip(AdjustmentButtons.tooltip());
        widgets.add(id, button);
        return button;
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

        setTextContent("receiver_actual_channels", Component.translatable("ae2cs.menu.ender_broadcaster.receiver_actual_channels", menu.receiverActualChannels));
        setTextHidden("receiver_actual_channels", !isReceiver);

        setTextContent("receiver_expected_channels_title", Component.translatable("ae2cs.menu.ender_broadcaster.receiver_expected_channels_title"));
        setTextHidden("receiver_expected_channels_title", !isReceiver);

        setTextContent("sender_sent_channels", Component.translatable("ae2cs.menu.ender_broadcaster.sender_sent_channels", menu.senderSentChannels));
        setTextHidden("sender_sent_channels", !isSender);

        toggleBandLinkButton.setState(isSender);
        toggleBandLinkButton.setVisibility(isReceiver || isSender);

        openFrequencyBandManagerMenuButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);
        cleanLinkerConnectionButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);

        boolean unlimited = menu.receiverExpectedChannels == BroadcastReceiverHost.UNLIMITED_CHANNELS;
        String exact = Integer.toString(menu.receiverExpectedChannels);
        String display = font.width(exact) <= 30 ? exact : ReadableNumberConverter.format(menu.receiverExpectedChannels, 5);
        channelLimitButton.visible = isReceiver;
        channelLimitButton.active = isReceiver;
        channelLimitButton.setMessage(Component.literal(unlimited ? "∞" : display));
        boolean canIncrease = isReceiver && !unlimited && menu.receiverExpectedChannels < Integer.MAX_VALUE;
        boolean canDecrease = isReceiver && !unlimited && menu.receiverExpectedChannels > 0;
        this.addReceiver1ChannelsButton.active = canIncrease;
        this.addReceiver1ChannelsButton.visible = isReceiver;
        this.addReceiver10ChannelsButton.active = canIncrease;
        this.addReceiver10ChannelsButton.visible = isReceiver;
        this.reduceReceiver1ChannelsButton.active = canDecrease;
        this.reduceReceiver1ChannelsButton.visible = isReceiver;
        this.reduceReceiver10ChannelsButton.active = canDecrease;
        this.reduceReceiver10ChannelsButton.visible = isReceiver;
    }
}
