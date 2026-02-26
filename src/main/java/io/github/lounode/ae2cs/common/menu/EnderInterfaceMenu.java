package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.slot.FakeSlot;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.BlackListMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderInterfaceMenu extends InterfaceMenu
{
    private static final String changeAbsorbRange = "change_absorb_range";

    public final boolean extended;

    @GuiSync(10)
    public BlackListMode blackListMode;

    @GuiSync(11)
    public int absorbRange = 3;

    @GuiSync(12)
    public ShowRangeMode showRange;

    public EnderInterfaceMenu(MenuType<? extends InterfaceMenu> menuType, int id, Inventory ip, EnderInterfaceHost host)
    {
        super(menuType, id, ip, host);

        InternalInventory absorbConfigWarp = getEnderInterfaceHost().getEnderInterfaceLogic().getAbsorbConfigInventory().createMenuWrapper();
        for (int i = 0; i < absorbConfigWarp.size(); i++)
        {
            addSlot(new FakeSlot(absorbConfigWarp, i), SlotSemantics.PROCESSING_INPUTS);
        }

        extended = host.isExtended();

        registerClientAction(changeAbsorbRange, Integer.class, this::onChangeAbsorbRange);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
        super.loadSettingsFromHost(cm);
        showRange = cm.getSetting(AECSSettings.SHOW_RANGE_MODE);
        blackListMode = cm.getSetting(AECSSettings.BLACK_LIST_MODE);
    }

    @Override
    public void broadcastChanges()
    {
        absorbRange = getEnderInterfaceHost().getEnderInterfaceLogic().getRange();
        super.broadcastChanges();
    }

    public void sendChangeAbsorbRange(int delta)
    {
        sendClientAction(changeAbsorbRange, delta);
    }

    private void onChangeAbsorbRange(int delta)
    {
        int originalValue = getEnderInterfaceHost().getEnderInterfaceLogic().getRange();
        getEnderInterfaceHost().getEnderInterfaceLogic().setRange(originalValue + delta);
    }

    private EnderInterfaceHost getEnderInterfaceHost()
    {
        return (EnderInterfaceHost) getHost();
    }
}
