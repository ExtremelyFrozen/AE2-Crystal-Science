package io.github.lounode.ae2cs.common.menu;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ResonatingPatternProviderMenu extends PatternProviderMenu
{
    private final ResonatingPatternProviderLogic logic;
    private final ToolboxMenu toolbox;

    @GuiSync(10)
    public PullMode pullMode;

    public ResonatingPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, ResonatingPatternProviderHost host)
    {
        super(menuType, id, playerInventory, host);
        this.logic = host.getResonatingLogic();
        this.toolbox = new ToolboxMenu(this);

        setupUpgrades(host.getUpgrades());
    }

    @Override
    public void broadcastChanges()
    {
        pullMode = this.logic.getConfigManager().getSetting(AECSSettings.PULL_MODE);
        super.broadcastChanges();
    }

    public ToolboxMenu getToolbox()
    {
        return toolbox;
    }

    public IUpgradeInventory getUpgrades()
    {
        return logic.getUpgrades();
    }
}
