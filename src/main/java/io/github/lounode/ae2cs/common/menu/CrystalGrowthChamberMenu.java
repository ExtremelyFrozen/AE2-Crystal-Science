package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalGrowthChamberMenu extends UpgradeableMenu<CrystalGrowthChamberBlockEntity> {

    public CrystalGrowthChamberMenu(int id, Inventory playerInv, @NotNull CrystalGrowthChamberBlockEntity host) {
        super(AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get(), id, playerInv, host);

        for (int i = 0; i < getHost().getInternalInventory().size(); ++i) {
            this.addSlot(new AppEngSlot(getHost().getInternalInventory(), i), SlotSemantics.MACHINE_INPUT);
        }
    }

    public @Nullable CrystalGrowthChamberBlockEntity getBlockEntity() {
        return getHost();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !getHost().isRemoved();
    }
}
