package io.github.lounode.ae2cs.common.menu;

import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.common.me.logic.QuartzOscillatorClockHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class QuartzOscillatorClockMenu extends UpgradeableMenu<QuartzOscillatorClockHost>
{
    private static final String changePulseCountHold = "change_pulse_count_hold";

    @GuiSync(10)
    public int pulseCountHold = 10;

    public QuartzOscillatorClockMenu(MenuType<?> menuType, int id, Inventory ip, QuartzOscillatorClockHost host)
    {
        super(menuType, id, ip, host);

        registerClientAction(changePulseCountHold, Integer.class, this::onChangePulseCountHold);
    }

    public void sendChangePulseCountHold(int newValue)
    {
        sendClientAction(changePulseCountHold, newValue);
    }

    private void onChangePulseCountHold(int newValue)
    {
        getHost().getLogic().setCurrentHold(newValue);
    }

    @Override
    public void broadcastChanges()
    {
        pulseCountHold = getHost().getLogic().getCurrentHold();
        super.broadcastChanges();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
        this.setRedStoneMode(cm.getSetting(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE));
    }
}
