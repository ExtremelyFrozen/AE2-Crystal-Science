package io.github.lounode.ae2_crystal_seeds.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CrystalGrowthChamberMenu extends ChestMenu {

    public CrystalGrowthChamberMenu(int containerId, Inventory playerInventory, Container container) {
        super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
    }
}
