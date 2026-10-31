package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

public class CrystalGrowthAcceleratorMenu extends UpgradeableMenu<CrystalGrowthChamberBlockEntity> {

    private static final String CYCLE_GROWTH_STAGE_ACTION = "cycle_growth_stage";

    @GuiSync(10)
    public int growthStage;

    public CrystalGrowthAcceleratorMenu(int id, Inventory playerInv, @NotNull CrystalGrowthChamberBlockEntity host) {
        super(AECSMenus.CRYSTAL_GROWTH_ACCELERATOR_MENU.get(), id, playerInv, host);
        registerClientAction(CYCLE_GROWTH_STAGE_ACTION, this::cycleGrowthStage);
        addSlot(new AppEngSlot(host.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            growthStage = getHost().getAcceleratorTargetStage();
        }
        super.broadcastChanges();
    }

    public CrystalGrowthChamberBlockEntity getBlockEntity() {
        return getHost();
    }

    public void sendCycleGrowthStage() {
        sendClientAction(CYCLE_GROWTH_STAGE_ACTION);
    }

    private void cycleGrowthStage() {
        getHost().setAcceleratorTargetStage((getHost().getAcceleratorTargetStage() + 1) % 4);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !getHost().isRemoved();
    }
}
