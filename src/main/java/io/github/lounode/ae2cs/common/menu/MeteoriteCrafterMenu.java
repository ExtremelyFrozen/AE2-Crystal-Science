package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.lounode.ae2cs.common.block.entity.MeteoriteCrafterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class MeteoriteCrafterMenu extends UpgradeableMenu<MeteoriteCrafterBlockEntity>
{

    public MeteoriteCrafterMenu(int id, Inventory ip, MeteoriteCrafterBlockEntity host)
    {
        super(AECSMenus.METEORITE_CRAFTER_MENU.get(), id, ip, host);
    }

    @Override
    protected void setupInventorySlots()
    {
        for (int i = 0; i < getHost().getTerminalPatternInventory().size(); ++i)
        {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, getHost().getTerminalPatternInventory(), i), SlotSemantics.ENCODED_PATTERN);
        }
        InternalInventory returnWrap = getHost().getReturnInventory().createMenuWrapper();
        for (int i = 0; i < getHost().getReturnInventory().size(); ++i)
        {
            this.addSlot(new AppEngSlot(returnWrap, i), SlotSemantics.STORAGE);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager configManager)
    {
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().getBlockEntity().isRemoved();
    }
}
