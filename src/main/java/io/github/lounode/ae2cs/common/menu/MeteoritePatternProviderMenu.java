package io.github.lounode.ae2cs.common.menu;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.ToolboxMenu;
import appeng.menu.implementations.PatternProviderMenu;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class MeteoritePatternProviderMenu extends PatternProviderMenu
{
    private final MeteoritePatternProviderHost host;
    private final ToolboxMenu toolbox;

    public MeteoritePatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, MeteoritePatternProviderHost host)
    {
        super(menuType, id, playerInventory, host);
        this.host = host;
        this.toolbox = new ToolboxMenu(this);

        setupUpgrades(host.getUpgrades());
    }

    public ToolboxMenu getToolbox()
    {
        return toolbox;
    }

    public final IUpgradeInventory getUpgrades()
    {
        return host.getUpgrades();
    }

    @Override
    public void broadcastChanges()
    {
        toolbox.tick();
        super.broadcastChanges();
    }
}
