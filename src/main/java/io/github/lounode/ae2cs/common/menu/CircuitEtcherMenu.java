package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.CircuitEtcherBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.slot.ResonatingStackingSlot;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class CircuitEtcherMenu extends ResonatingFactoryMenu<CircuitEtcherBlockEntity> {

    @GuiSync(10)
    public int recipeProgress;

    @GuiSync(11)
    public int recipeNeedTicks;

    @GuiSync(12)
    public double currentEnergy;

    @GuiSync(13)
    public double maxEnergy;

    public CircuitEtcherMenu(int id, Inventory ip, CircuitEtcherBlockEntity host) {
        this(AECSMenus.CIRCUIT_ETCHER_MENU.get(), id, ip, host);
    }

    public CircuitEtcherMenu(MenuType<?> menuType, int id, Inventory ip, CircuitEtcherBlockEntity host) {
        super(menuType, id, ip, host);
    }

    @Override
    protected void setupInventorySlots() {
        AppEngInternalInventory inputInv = getHost().getInputInv();
        AppEngInternalInventory outputInv = getHost().getOutputInv();
        for (int i = 0; i < inputInv.size(); i++) {
            ResonatingStackingSlot inputSlot = new ResonatingStackingSlot(inputInv, i);
            this.addSlot(inputSlot, SlotSemantics.MACHINE_INPUT);
        }
        for (int i = 0; i < outputInv.size(); i++) {
            ResonatingStackingSlot outputSlot = new ResonatingStackingSlot(outputInv, i) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            this.addSlot(outputSlot, SlotSemantics.MACHINE_OUTPUT);
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

        super.broadcastChanges();
    }
}
