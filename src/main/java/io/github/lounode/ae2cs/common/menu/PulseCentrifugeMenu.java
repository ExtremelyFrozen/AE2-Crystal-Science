package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.networking.FluidTankState;
import io.github.lounode.ae2cs.common.block.entity.PulseCentrifugeBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class PulseCentrifugeMenu extends UpgradeableMenu<PulseCentrifugeBlockEntity> {

    private static final String FILL_FLUID_INPUT_ACTION = "fill_fluid_input";
    private static final String DRAIN_FLUID_OUTPUT_ACTION = "drain_fluid_output";

    @GuiSync(10)
    public int recipeProgress;
    @GuiSync(11)
    public int recipeEnergyCost;
    @GuiSync(12)
    public double currentEnergy;
    @GuiSync(13)
    public double maxEnergy;
    @GuiSync(14)
    public boolean processing;
    @GuiSync(15)
    public FluidTankState inputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);
    @GuiSync(16)
    public FluidTankState outputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);

    public PulseCentrifugeMenu(int id, Inventory playerInventory, PulseCentrifugeBlockEntity host) {
        super(AECSMenus.PULSE_CENTRIFUGE_MENU.get(), id, playerInventory, host);
        registerClientAction(FILL_FLUID_INPUT_ACTION, this::fillFluidInput);
        registerClientAction(DRAIN_FLUID_OUTPUT_ACTION, this::drainFluidOutput);

        addSlot(new AppEngSlot(host.getInputInv(), 0), SlotSemantics.MACHINE_INPUT);
        for (int slot = 0; slot < host.getOutputInv().size(); slot++) {
            addSlot(new AppEngSlot(host.getOutputInv(), slot) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            }, SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager configManager) {}

    public void sendFillFluidInputAction() {
        sendClientAction(FILL_FLUID_INPUT_ACTION);
    }

    public void sendDrainFluidOutputAction() {
        sendClientAction(DRAIN_FLUID_OUTPUT_ACTION);
    }

    private void fillFluidInput() {
        var result = FluidUtil.tryEmptyContainer(getCarried(), getHost().getFluidTanks().input(), Integer.MAX_VALUE,
                getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    private void drainFluidOutput() {
        var result = FluidUtil.tryFillContainer(getCarried(), getHost().getFluidTanks().output(), Integer.MAX_VALUE,
                getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    @Override
    public void broadcastChanges() {
        recipeProgress = getHost().getRecipeProgress();
        recipeEnergyCost = getHost().getActiveRecipeEnergyCost();
        currentEnergy = getHost().getAECurrentPower();
        maxEnergy = getHost().getAEMaxPower();
        processing = getHost().isProcessing();
        inputFluid = new FluidTankState(getHost().getFluidTanks().input().getFluid(),
                getHost().getFluidTanks().input().getCapacity());
        outputFluid = new FluidTankState(getHost().getFluidTanks().output().getFluid(),
                getHost().getFluidTanks().output().getCapacity());
        super.broadcastChanges();
    }
}
