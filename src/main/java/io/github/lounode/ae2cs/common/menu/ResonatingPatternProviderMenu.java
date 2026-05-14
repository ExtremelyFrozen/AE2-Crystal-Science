package io.github.lounode.ae2cs.common.menu;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.guisync.GuiSync;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ResonatingPatternProviderMenu extends UpgradeablePatternProviderMenu
{
    private final ResonatingPatternProviderLogic logic;

    public final boolean extended;

    @GuiSync(10)
    public PullMode pullMode;

    public ResonatingPatternProviderMenu(MenuType<? extends UpgradeablePatternProviderMenu> menuType, int id, Inventory playerInventory, ResonatingPatternProviderHost host)
    {
        super(menuType, id, playerInventory, host);
        this.logic = host.getResonatingLogic();
        this.extended = host.isExtended();
    }

    @Override
    public void broadcastChanges()
    {
        pullMode = this.logic.getConfigManager().getSetting(AECSSettings.PULL_MODE);
        super.broadcastChanges();
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return logic.getUpgrades();
    }

    public ResonatingPatternProviderHost getProviderHost()
    {
        return (ResonatingPatternProviderHost) getTarget();
    }
}
