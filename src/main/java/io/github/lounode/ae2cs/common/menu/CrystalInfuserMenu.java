package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.CrystalInfuserBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CrystalInfuserMenu extends UpgradeableMenu<CrystalInfuserBlockEntity> {

    @GuiSync(10)
    public int recipeProgress;

    @GuiSync(11)
    public int recipeNeedTicks;

    @GuiSync(12)
    public double currentEnergy;

    @GuiSync(13)
    public double maxEnergy;

    @GuiSync(14)
    public int fluidAmount;

    @GuiSync(15)
    public int fluidCapacity;

    public CrystalInfuserMenu(int id, Inventory playerInventory, CrystalInfuserBlockEntity host) {
        super(AECSMenus.CRYSTAL_INFUSER_MENU.get(), id, playerInventory, host);

        AppEngInternalInventory input = host.getInputInv();
        AppEngInternalInventory output = host.getOutputInv();
        for (int i = 0; i < input.size(); i++) {
            addSlot(new AppEngSlot(input, i), SlotSemantics.MACHINE_INPUT);
        }
        for (int i = 0; i < output.size(); i++) {
            addSlot(new AppEngSlot(output, i) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            }, SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {}

    @Override
    public void broadcastChanges() {
        recipeNeedTicks = getHost().getActiveRecipeEnergyCost();
        recipeProgress = getHost().getRecipeProgress();
        maxEnergy = getHost().getAEMaxPower();
        currentEnergy = getHost().getAECurrentPower();
        fluidAmount = getHost().getFluidTank().getFluidAmount();
        fluidCapacity = getHost().getFluidTank().getCapacity();
        super.broadcastChanges();
    }
}
