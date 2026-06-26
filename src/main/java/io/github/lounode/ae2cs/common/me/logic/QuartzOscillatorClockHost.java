package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

public interface QuartzOscillatorClockHost extends IUpgradeableObject {

    QuartzOscillatorClockLogic getLogic();

    BlockEntity getBlockEntity();

    EnumSet<Direction> getTargets();

    void saveChanges();

    default void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(AECSMenus.QUARTZ_OSCILLATOR_CLOCK_MENU.get(), player, locator);
    }

    default void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(AECSMenus.QUARTZ_OSCILLATOR_CLOCK_MENU.get(), player, subMenu.getLocator());
    }

    /**
     * 逻辑层通知：开始/结束脉冲
     */
    void setPulseActive(boolean active);

    /**
     * 给logic查询当前是否在输出
     */
    boolean isPulseActive();
}
