package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.networking.FluidTankState;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.EntropyFluidMode;
import io.github.lounode.ae2cs.common.block.entity.EntropyVariationReactionChamberBlockEntity;

import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.recipes.entropy.EntropyMode;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class EntropyVariationReactionChamberMenu extends UpgradeableMenu<EntropyVariationReactionChamberBlockEntity> {

    private static final String FILL_FLUID_INPUT_ACTION = "fill_fluid_input";
    private static final String DRAIN_FLUID_OUTPUT_ACTION = "drain_fluid_output";

    @GuiSync(10)
    public int recipeProgress;

    @GuiSync(11)
    public int recipeNeedTicks;

    @GuiSync(12)
    public double currentEnergy;

    @GuiSync(13)
    public double maxEnergy;

    @GuiSync(14)
    public EntropyMode entropyMode;

    @GuiSync(15)
    public FluidTankState inputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);

    @GuiSync(16)
    public FluidTankState outputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);

    @GuiSync(17)
    public EntropyFluidMode entropyFluidMode;

    public EntropyVariationReactionChamberMenu(MenuType<?> menuType, int id, Inventory ip, EntropyVariationReactionChamberBlockEntity host) {
        super(menuType, id, ip, host);
        registerClientAction(FILL_FLUID_INPUT_ACTION, this::fillFluidInput);
        registerClientAction(DRAIN_FLUID_OUTPUT_ACTION, this::drainFluidOutput);

        InternalInventory inputInv = getHost().getInputInv().createMenuWrapper();
        InternalInventory outputInv = getHost().getOutputInv().createMenuWrapper();
        for (int i = 0; i < inputInv.size(); i++) {
            AppEngSlot inputSlot = new AppEngSlot(inputInv, i);
            this.addSlot(inputSlot, SlotSemantics.MACHINE_INPUT);
        }
        for (int i = 0; i < outputInv.size(); i++) {
            AppEngSlot outputSlot = new AppEngSlot(outputInv, i) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            this.addSlot(outputSlot, SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.entropyMode = cm.getSetting(AECSSettings.ENTROPY_CHANGE_MODE);
        this.entropyFluidMode = cm.getSetting(AECSSettings.ENTROPY_FLUID_MODE);
    }

    public void sendFillFluidInputAction() {
        sendClientAction(FILL_FLUID_INPUT_ACTION);
    }

    public void sendDrainFluidOutputAction() {
        sendClientAction(DRAIN_FLUID_OUTPUT_ACTION);
    }

    private void fillFluidInput() {
        var result = FluidUtil.tryEmptyContainer(getCarried(),
                getHost().getFluidTanks().input(), 16_000, getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    private void drainFluidOutput() {
        var result = FluidUtil.tryFillContainer(getCarried(),
                getHost().getFluidTanks().output(), 16_000, getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    @Override
    public void broadcastChanges() {
        recipeNeedTicks = getHost().getActiveRecipeEnergyCost();
        recipeProgress = getHost().getRecipeProgress();
        maxEnergy = getHost().getAEMaxPower();
        currentEnergy = getHost().getAECurrentPower();
        entropyMode = getHost().getEntropyMode();
        entropyFluidMode = getHost().getEntropyFluidMode();
        inputFluid = new FluidTankState(getHost().getFluidTanks().input().getFluid(),
                getHost().getFluidTanks().input().getCapacity());
        outputFluid = new FluidTankState(getHost().getFluidTanks().output().getFluid(),
                getHost().getFluidTanks().output().getCapacity());

        super.broadcastChanges();
    }
}
