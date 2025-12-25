package io.github.lounode.ae2cs.api.submenu;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import net.minecraft.world.entity.player.Player;

public interface CustomReturnableSubMenuHost extends ISubMenuHost
{
    @Override
    default void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        if (subMenu instanceof CustomReturnableSubMenu customReturnableSubMenu)
        {
            MenuOpener.returnTo(customReturnableSubMenu.getReturnToMenuType(), player, customReturnableSubMenu.getLocator());
        }
        else
        {
            throw new IllegalArgumentException("CustomReturnableSubMenuHost can only use with subMenu that implement CustomReturnableSubMenu");
        }
    }
}
