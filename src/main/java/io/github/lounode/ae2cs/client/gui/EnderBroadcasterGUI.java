package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.menu.EnderBroadcasterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderBroadcasterGUI extends UpgradeableScreen<EnderBroadcasterMenu>
{
    private Component bandName = Component.empty();
    private Component connectStatus = Component.empty();
    private Component receiverExpectedChannels = Component.empty();

    private AE2Button addReceiver1ChannelsButton;
    private AE2Button addReceiver10ChannelsButton;
    private AE2Button reduceReceiver1ChannelsButton;
    private AE2Button reduceReceiver10ChannelsButton;

    public EnderBroadcasterGUI(EnderBroadcasterMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/ender_broadcaster_menu.json"));

        addReceiver1ChannelsButton = new AE2Button(Component.literal("+1"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(1 * mult);
        });
        widgets.add("add_receiver_expected_channels_button_1", addReceiver1ChannelsButton);

        addReceiver10ChannelsButton = new AE2Button(Component.literal("+10"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(10 * mult);
        });
        widgets.add("add_receiver_expected_channels_button_10", addReceiver10ChannelsButton);

        reduceReceiver1ChannelsButton = new AE2Button(Component.literal("-1"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(-1 * mult);
        });
        widgets.add("reduce_receiver_expected_channels_button_1", reduceReceiver1ChannelsButton);

        reduceReceiver10ChannelsButton = new AE2Button(Component.literal("-10"), button -> {
            int mult = hasShiftDown() ? 5 : 1;
            menu.sendChangeExpectedChannels(-10 * mult);
        });
        widgets.add("reduce_receiver_expected_channels_button_10", reduceReceiver10ChannelsButton);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font, this.bandName, 8, 16, 4210752, false);
        guiGraphics.drawString(this.font, this.connectStatus, 8, 32, 4210752, false);
        guiGraphics.drawString(this.font, this.receiverExpectedChannels, 8, 48, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        Component bandValue = menu.bandName.isEmpty()
                ? Component.translatable("ae2cs.menu.ender_broadcaster.none")
                : Component.literal(menu.bandName);
        this.bandName = Component.translatable(
                "ae2cs.menu.ender_broadcaster.current_band",
                bandValue
        );

        Component typeText = switch (menu.connectionType)
        {
            case AS_SENDER -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.as_sender");
            case AS_RECEIVER -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.as_receiver");
            case NO_CONNECTION -> Component.translatable("ae2cs.menu.ender_broadcaster.connection_type.no_connection");
        };
        this.connectStatus = Component.translatable("ae2cs.menu.ender_broadcaster.connect_status", typeText);

        if (menu.connectionType == EnderBroadcasterBlockEntity.ConnectionType.AS_RECEIVER)
        {
            this.receiverExpectedChannels = Component.translatable("ae2cs.menu.ender_broadcaster.receiver_expected_channels", menu.receiverExpectedChannels);
            this.addReceiver1ChannelsButton.active = true;
            this.addReceiver1ChannelsButton.visible = true;
            this.addReceiver10ChannelsButton.active = true;
            this.addReceiver10ChannelsButton.visible = true;
            this.reduceReceiver1ChannelsButton.active = true;
            this.reduceReceiver1ChannelsButton.visible = true;
            this.reduceReceiver10ChannelsButton.active = true;
            this.reduceReceiver10ChannelsButton.visible = true;
        }
        else
        {
            this.receiverExpectedChannels = Component.empty();
            this.addReceiver1ChannelsButton.active = false;
            this.addReceiver1ChannelsButton.visible = false;
            this.addReceiver10ChannelsButton.active = false;
            this.addReceiver10ChannelsButton.visible = false;
            this.reduceReceiver1ChannelsButton.active = false;
            this.reduceReceiver1ChannelsButton.visible = false;
            this.reduceReceiver10ChannelsButton.active = false;
            this.reduceReceiver10ChannelsButton.visible = false;
        }
    }
}
