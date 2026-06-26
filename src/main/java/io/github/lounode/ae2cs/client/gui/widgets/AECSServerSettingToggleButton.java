package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.api.config.Setting;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;

public class AECSServerSettingToggleButton<T extends Enum<T>> extends AECSSettingToggleButton<T> {

    public AECSServerSettingToggleButton(Setting<T> setting, T val) {
        super(setting, val, AECSServerSettingToggleButton::sendToServer);
    }

    private static <T extends Enum<T>> void sendToServer(AECSSettingToggleButton<T> button, boolean backwards) {
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(button.getSetting(), backwards));
    }
}
