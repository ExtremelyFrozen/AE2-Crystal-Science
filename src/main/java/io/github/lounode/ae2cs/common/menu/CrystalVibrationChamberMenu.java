package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import io.github.lounode.ae2cs.common.block.entity.CrystalVibrationChamberBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CrystalVibrationChamberMenu extends UpgradeableMenu<CrystalVibrationChamberBlockEntity>
{
    @GuiSync(10)
    public int burnTime;

    @GuiSync(11)
    public int maxBurnTime;

    @GuiSync(12)
    public double energyPerTick;

    @GuiSync(13)
    public double storedAE;

    @GuiSync(14)
    public double maxStoredAE;


    public CrystalVibrationChamberMenu(int id, Inventory playerInv, @NotNull CrystalVibrationChamberBlockEntity host)
    {
        super(AECSMenus.CRYSTAL_VIBRATION_CHAMBER_MENU.get(), id, playerInv, host);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {

    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
        InternalInventory wrapInv = getHost().getInv().createMenuWrapper();
        this.addSlot(new AppEngSlot(wrapInv, 0), SlotSemantics.MACHINE_INPUT);
    }

    @Override
    public void broadcastChanges()
    {
        if(isServerSide())
        {
            this.burnTime = getHost().getRemainingBurnTime();
            this.maxBurnTime = getHost().getMaxBurnTime();
            this.energyPerTick = getHost().getEnergyPerTick();
            this.storedAE = getHost().getAECurrentPower();
            this.maxStoredAE = getHost().getAEMaxPower();
        }

        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().isRemoved();
    }
}