package io.github.lounode.ae2cs.common.menu;

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

    @GuiSync(10)
    public PullMode pullMode;

    public ResonatingPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, ResonatingPatternProviderHost host)
    {
        super(menuType, id, playerInventory, host);
        this.logic = host.getResonatingLogic();
    }

    @Override
    public void broadcastChanges()
    {
        pullMode = this.logic.getConfigManager().getSetting(AECSSettings.PULL_MODE);
        super.broadcastChanges();
    }
}
