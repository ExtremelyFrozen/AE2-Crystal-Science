package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.slot.FakeSlot;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderInterfaceMenu extends InterfaceMenu
{
    private static final String changeBlackListMode = "change_black_list_mode";
    public static final String changeAbsorbRange = "change_absorb_range";

    @GuiSync(10)
    public boolean blackListMode = false;

    @GuiSync(11)
    public int absorbRange = 3;

    public EnderInterfaceMenu(MenuType<? extends InterfaceMenu> menuType, int id, Inventory ip, EnderInterfaceHost host)
    {
        super(menuType, id, ip, host);

        registerClientAction(changeBlackListMode, Boolean.class, this::onChangeBlackListMode);
        registerClientAction(changeAbsorbRange, Integer.class, this::onChangeAbsorbRange);
    }

    @Override
    public void broadcastChanges()
    {
        blackListMode = getEnderInterfaceHost().getEnderInterfaceLogic().isBlackListMode();
        absorbRange = getEnderInterfaceHost().getEnderInterfaceLogic().getRange();
        super.broadcastChanges();
    }

    public void sendChangeBlackListMode(boolean newMode)
    {
        sendClientAction(changeBlackListMode, newMode);
    }

    public void sendChangeAbsorbRange(int delta)
    {
        sendClientAction(changeAbsorbRange, delta);
    }

    private void onChangeBlackListMode(boolean newMode)
    {
        getEnderInterfaceHost().getEnderInterfaceLogic().setBlackListMode(newMode);
    }

    private void onChangeAbsorbRange(int delta)
    {
        int originalValue = getEnderInterfaceHost().getEnderInterfaceLogic().getRange();
        getEnderInterfaceHost().getEnderInterfaceLogic().setRange(originalValue + delta);
    }

    @Override
    protected void setupInventorySlots()
    {
        super.setupInventorySlots();

        InternalInventory absorbConfigWarp = getEnderInterfaceHost().getEnderInterfaceLogic().getAbsorbConfigInventory().createMenuWrapper();
        for (int i = 0; i < absorbConfigWarp.size(); i++)
        {
            addSlot(new FakeSlot(absorbConfigWarp, i), SlotSemantics.PROCESSING_INPUTS);
        }
    }

    private EnderInterfaceHost getEnderInterfaceHost()
    {
        return (EnderInterfaceHost) getHost();
    }
}
