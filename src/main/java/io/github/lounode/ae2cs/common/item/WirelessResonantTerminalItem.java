package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.menuhost.WirelessResonantTerminalMenuHost;

import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

public class WirelessResonantTerminalItem extends WirelessTerminalItem {

    public WirelessResonantTerminalItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {
        return AECSMenus.RESONANT_TEMPLATE_CODING_TERM_MENU.get();
    }

    @Nullable
    @Override
    public WirelessResonantTerminalMenuHost getMenuHost(Player player, ItemMenuHostLocator locator,
                                                        @Nullable BlockHitResult hitResult) {
        return new WirelessResonantTerminalMenuHost(this, player, locator,
                (p, subMenu) -> openFromInventory(p, locator, true));
    }
}
