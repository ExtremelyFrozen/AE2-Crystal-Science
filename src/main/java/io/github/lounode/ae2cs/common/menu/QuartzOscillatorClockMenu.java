package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.SoundMode;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;

import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class QuartzOscillatorClockMenu extends UpgradeableMenu<QuartzOscillatorClockHost> {

    private static final String changePulseCountHold = "change_pulse_count_hold";
    private static final String changePulseWidthTicks = "change_pulse_width_ticks";

    @GuiSync(10)
    public int pulseCountHold = 10;

    @GuiSync(11)
    public int pulseWidthTicks = 1;

    @GuiSync(12)
    public SoundMode soundMode = SoundMode.UNMUTE;

    public QuartzOscillatorClockMenu(MenuType<?> menuType, int id, Inventory ip, QuartzOscillatorClockHost host) {
        super(menuType, id, ip, host);

        registerClientAction(changePulseCountHold, Integer.class, this::onChangePulseCountHold);
        registerClientAction(changePulseWidthTicks, Integer.class, this::onChangePulseWidthTicks);
    }

    public void sendChangePulseCountHold(int newValue) {
        sendClientAction(changePulseCountHold, newValue);
    }

    public void sendChangePulseWidthTicks(int newValue) {
        sendClientAction(changePulseWidthTicks, newValue);
    }

    private void onChangePulseCountHold(int newValue) {
        getHost().getLogic().setCurrentHold(newValue);
    }

    private void onChangePulseWidthTicks(int newValue) {
        getHost().getLogic().setPulseWidthTicks(newValue);
    }

    @Override
    public void broadcastChanges() {
        pulseCountHold = getHost().getLogic().getCurrentHold();
        pulseWidthTicks = getHost().getLogic().getPulseWidthTicks();
        super.broadcastChanges();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setRedStoneMode(cm.getSetting(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE));
        soundMode = getHost().getLogic().getConfigManager().getSetting(AECSSettings.SOUND_MODE);
    }
}
