package io.github.lounode.ae2cs.api.submenu;

import appeng.menu.ISubMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * 用来指定这个SubMenu应该返回到哪一个菜单中，必须要与相关的CustomReturnableSubMenuHost配合使用
 */
public interface CustomReturnableSubMenu extends ISubMenu
{
    MenuType<?> getReturnToMenuType();

    void setReturnToMenuType(MenuType<?> menuType);
}
