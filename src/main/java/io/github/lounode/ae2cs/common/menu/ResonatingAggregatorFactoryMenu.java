package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.networking.FluidTankState;
import io.github.lounode.ae2cs.common.block.entity.ResonatingAggregatorFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.slot.ResonatingStackingSlot;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class ResonatingAggregatorFactoryMenu extends ResonatingFactoryMenu<ResonatingAggregatorFactoryBlockEntity> {

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
    public FluidTankState inputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);

    @GuiSync(15)
    public FluidTankState outputFluid = new FluidTankState(FluidStack.EMPTY, 16_000);

    public ResonatingAggregatorFactoryMenu(int id, Inventory playerInventory, ResonatingAggregatorFactoryBlockEntity host) {
        super(AECSMenus.RESONATING_AGGREGATOR_FACTORY_MENU.get(), id, playerInventory, host);
        registerClientAction(FILL_FLUID_INPUT_ACTION, this::fillFluidInput);
        registerClientAction(DRAIN_FLUID_OUTPUT_ACTION, this::drainFluidOutput);
    }

    @Override
    protected void setupInventorySlots() {
        for (int slot = 0; slot < getHost().getInputInv().size(); slot++) {
            addSlot(new ResonatingStackingSlot(getHost().getInputInv(), slot), SlotSemantics.MACHINE_INPUT);
        }
        for (int slot = 0; slot < getHost().getOutputInv().size(); slot++) {
            addSlot(new ResonatingStackingSlot(getHost().getOutputInv(), slot) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            }, SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {}

    public void sendFillFluidInputAction() {
        sendClientAction(FILL_FLUID_INPUT_ACTION);
    }

    public void sendDrainFluidOutputAction() {
        sendClientAction(DRAIN_FLUID_OUTPUT_ACTION);
    }

    private void fillFluidInput() {
        var result = FluidUtil.tryEmptyContainer(getCarried(), getHost().getFluidTanks().input(), Integer.MAX_VALUE, getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    private void drainFluidOutput() {
        var result = FluidUtil.tryFillContainer(getCarried(), getHost().getFluidTanks().output(), Integer.MAX_VALUE, getPlayer(), true);
        if (result.isSuccess()) setCarried(result.getResult());
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            recipeNeedTicks = getHost().getActiveRecipeEnergyCost();
            recipeProgress = getHost().getRecipeProgress();
            maxEnergy = getHost().getAEMaxPower();
            currentEnergy = getHost().getAECurrentPower();
            inputFluid = new FluidTankState(getHost().getFluidTanks().input().getFluid(), getHost().getFluidTanks().input().getCapacity());
            outputFluid = new FluidTankState(getHost().getFluidTanks().output().getFluid(), getHost().getFluidTanks().output().getCapacity());
        }
        super.broadcastChanges();
    }
}
