package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegratedInterfaceMenu extends UpgradeableMenu<IntegratedInterfaceHost>
{
    public IntegratedInterfaceMenu(MenuType<?> menuType, int id, Inventory ip, IntegratedInterfaceHost host)
    {
        super(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), id, ip, host);
    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
        InternalInventory configWrap = getHost().getConfigInv().createMenuWrapper();
        for (int i = 0; i < getHost().getConfigInv().size(); ++i)
        {
            this.addSlot(new FakeSlot(configWrap, i), SlotSemantics.CONFIG);
        }
        InternalInventory storageWrap = getHost().getStorageInv().createMenuWrapper();
        for (int i = 0; i < getHost().getStorageInv().size(); ++i)
        {
            this.addSlot(new AppEngSlot(storageWrap, i), SlotSemantics.STORAGE);
        }
        for (int i = 0; i < getHost().getTerminalPatternInventory().size(); ++i)
        {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN,getHost().getTerminalPatternInventory(), i), SlotSemantics.ENCODED_PATTERN);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().getBlockEntity().isRemoved();
    }
}
