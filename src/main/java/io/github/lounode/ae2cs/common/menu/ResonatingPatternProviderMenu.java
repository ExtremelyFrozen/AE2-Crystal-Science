package io.github.lounode.ae2cs.common.menu;

import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ResonatingPatternProviderMenu extends PatternProviderMenu
{
    private static final String changePullMode = "change_pull_mode";

    private final ResonatingPatternProviderLogic logic;

    @GuiSync(10)
    public boolean enableChangePullMode = false;

    public ResonatingPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, ResonatingPatternProviderHost host)
    {
        super(menuType, id, playerInventory, host);
        this.logic = host.getResonatingLogic();

        registerClientAction(changePullMode, Boolean.class, this::onChangePullMode);
    }

    @Override
    public void broadcastChanges()
    {
        enableChangePullMode = this.logic.isEnablePull();
        super.broadcastChanges();
    }

    public void sendChangePullMode(boolean newValue)
    {
        sendClientAction(changePullMode, newValue);
    }

    private void onChangePullMode(boolean newValue)
    {
        this.logic.setEnablePull(newValue);
    }
}
