package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IPriorityHost;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.util.ConfigInventory;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public interface IntegratedInterfaceHost extends IConfigurableObject, IPriorityHost, IUpgradeableObject, PatternContainer
{
    IntegratedInterfaceLogic getLogic();

    boolean isExtended();

    /**
     * @return 此持有者在游戏中的方块实体
     */
    BlockEntity getBlockEntity();

    /**
     * 样板供应的目标面
     */
    EnumSet<Direction> getTargets();

    /**
     * 获取配置槽
     */
    default ConfigInventory getConfigInv()
    {
        return getLogic().getConfigInv();
    }

    /**
     * 获取存储槽
     */
    default ConfigInventory getStorageInv()
    {
        return getLogic().getStorageInv();
    }

    /**
     * 标脏
     */
    void saveChanges();

    /**
     * 打开集成接口界面
     */
    default void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), player, locator);
    }

    /**
     * 用于次级界面返回
     */
    @Override
    default void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), player, subMenu.getLocator());
    }

    // IConfigurableObject

    /**
     * 配置管理者
     */
    @Override
    default IConfigManager getConfigManager()
    {
        return getLogic().getConfigManager();
    }

    // IUpgradeableObject

    /**
     * 获取升级槽
     */
    @Override
    default IUpgradeInventory getUpgrades()
    {
        return getLogic().getUpgrades();
    }

    // PatternContainer

    /**
     * 获取当前连接的网络
     */
    @Override
    default @Nullable IGrid getGrid()
    {
        return getLogic().getGrid();
    }

    /**
     * 在样板管理器中显示的icon
     */
    AEItemKey getTerminalIcon();

    /**
     * 是否显示在样板管理器
     */
    @Override
    default boolean isVisibleInTerminal()
    {
        return getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
    }

    /**
     * 获取样板槽
     */
    @Override
    default InternalInventory getTerminalPatternInventory()
    {
        return getLogic().getPatternInventory();
    }

    /**
     * 获取样板在终端中的显示顺序
     */
    @Override
    default long getTerminalSortOrder()
    {
        BlockPos blockPos = getBlockEntity().getBlockPos();
        return (long) blockPos.getZ() << 24 ^ (long) blockPos.getX() << 8 ^ blockPos.getY();
    }

    /**
     * 获取样板在终端中分类的显示组
     */
    @Override
    default PatternContainerGroup getTerminalGroup()
    {
        return getLogic().getTerminalGroup();
    }
}
