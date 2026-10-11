package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

public class CrystalGrowthAcceleratorMenu extends AEBaseMenu {

    private final CrystalGrowthChamberBlockEntity host;

    public CrystalGrowthAcceleratorMenu(int id, Inventory playerInv, @NotNull CrystalGrowthChamberBlockEntity host) {
        super(AECSMenus.CRYSTAL_GROWTH_ACCELERATOR_MENU.get(), id, playerInv, host);
        this.host = host;

        addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
        createPlayerInventorySlots(playerInv);
    }

    public CrystalGrowthChamberBlockEntity getBlockEntity() {
        return host;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !host.isRemoved();
    }
}
