package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderBroadcasterMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterGUI extends UpgradeableScreen<EnderBroadcasterMenu>
{
    // 通用
    private final AECSIconButton openFrequencyViewMenuButton;
    private final AECSIconButton openFrequencyBandCreateMenuButton;
    private final AECSIconButton openFrequencyBandManagerMenuButton;
    private final AECSToggleButton toggleBandLinkButton;
    private final AECSIconButton cleanLinkerConnectionButton;
    // 接收端
    private final Button addReceiver1ChannelsButton;
    private final Button addReceiver10ChannelsButton;
    private final Button reduceReceiver1ChannelsButton;
    private final Button reduceReceiver10ChannelsButton;


    public EnderBroadcasterGUI(EnderBroadcasterMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/ender_broadcaster_menu.json"));

        openFrequencyViewMenuButton = new AECSIconButton(button -> menu.sendFrequencyBandMenuAction())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.BAND_VIEW;
            }
        };
        this.openFrequencyViewMenuButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.open_frequency_view_menu"));
        addToLeftToolbar(openFrequencyViewMenuButton);

        openFrequencyBandCreateMenuButton = new AECSIconButton(button -> menu.sendOpenFrequencyBandCreateMenuAction())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.BAND_CREATE;
            }
        };
        this.openFrequencyBandCreateMenuButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.open_frequency_create_menu"));
        addToLeftToolbar(openFrequencyBandCreateMenuButton);

        openFrequencyBandManagerMenuButton = new AECSIconButton(button -> menu.sendOpenFrequencyBandManagerMenuAction())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
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

        cleanLinkerConnectionButton = new AECSIconButton(button -> menu.sendCleanLinkerConnectionAction())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AdaptedAE2Icon.CLEAR;
            }
        };
        this.cleanLinkerConnectionButton.setMessage(Component.translatable("ae2cs.menu.ender_broadcaster.button.clean_linker_connection"));
        addToLeftToolbar(cleanLinkerConnectionButton);

        addReceiver1ChannelsButton = Button.builder(Component.literal("+1"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(1 * mult);
        }).build();
        widgets.add("add_receiver_expected_channels_button_1", addReceiver1ChannelsButton);

        addReceiver10ChannelsButton = Button.builder(Component.literal("+10"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(10 * mult);
        }).build();
        widgets.add("add_receiver_expected_channels_button_10", addReceiver10ChannelsButton);

        reduceReceiver1ChannelsButton = Button.builder(Component.literal("-1"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(-1 * mult);
        }).build();
        widgets.add("reduce_receiver_expected_channels_button_1", reduceReceiver1ChannelsButton);

        reduceReceiver10ChannelsButton = Button.builder(Component.literal("-10"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(-10 * mult);
        }).build();
        widgets.add("reduce_receiver_expected_channels_button_10", reduceReceiver10ChannelsButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        Component bandValue = menu.bandName.isEmpty()
                ? Component.translatable("ae2cs.menu.ender_broadcaster.none")
                : Component.literal(menu.bandName);

        Component typeText = switch (menu.connectionType)
        {
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

        setTextContent("receiver_expected_channels_count", Component.literal(String.valueOf(menu.receiverExpectedChannels)));
        setTextHidden("receiver_expected_channels_count", !isReceiver);

        setTextContent("sender_sent_channels", Component.translatable("ae2cs.menu.ender_broadcaster.sender_sent_channels", menu.senderSentChannels));
        setTextHidden("sender_sent_channels", !isSender);


        toggleBandLinkButton.setState(isSender);
        toggleBandLinkButton.setVisibility(isReceiver || isSender);

        openFrequencyBandManagerMenuButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);
        cleanLinkerConnectionButton.setVisibility(menu.connectionType != EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION);

        this.addReceiver1ChannelsButton.active = isReceiver;
        this.addReceiver1ChannelsButton.visible = isReceiver;
        this.addReceiver10ChannelsButton.active = isReceiver;
        this.addReceiver10ChannelsButton.visible = isReceiver;
        this.reduceReceiver1ChannelsButton.active = isReceiver;
        this.reduceReceiver1ChannelsButton.visible = isReceiver;
        this.reduceReceiver10ChannelsButton.active = isReceiver;
        this.reduceReceiver10ChannelsButton.visible = isReceiver;
    }
}
