package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.api.config.Setting;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class AECSServerSettingToggleButton<T extends Enum<T>> extends AECSSettingToggleButton<T>
{
    public AECSServerSettingToggleButton(Setting<T> setting, T val)
    {
        super(setting, val, AECSServerSettingToggleButton::sendToServer);
    }

    private static <T extends Enum<T>> void sendToServer(AECSSettingToggleButton<T> button, boolean backwards)
    {
        ServerboundPacket message = new ConfigButtonPacket(button.getSetting(), backwards);
        PacketDistributor.sendToServer(message);
    }
}