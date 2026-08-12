package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.networking.FluidTankState;
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
import net.neoforged.neoforge.fluids.FluidUtil;

public class CrystalInfuserMenu extends UpgradeableMenu<CrystalInfuserBlockEntity> {

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
    public FluidTankState inputFluid = new FluidTankState(net.neoforged.neoforge.fluids.FluidStack.EMPTY, 4000);

    @GuiSync(15)
    public FluidTankState outputFluid = new FluidTankState(net.neoforged.neoforge.fluids.FluidStack.EMPTY, 4000);

    public CrystalInfuserMenu(int id, Inventory playerInventory, CrystalInfuserBlockEntity host) {
        super(AECSMenus.CRYSTAL_INFUSER_MENU.get(), id, playerInventory, host);
        registerClientAction(FILL_FLUID_INPUT_ACTION, this::fillFluidInput);
        registerClientAction(DRAIN_FLUID_OUTPUT_ACTION, this::drainFluidOutput);

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
        recipeNeedTicks = getHost().getActiveRecipeEnergyCost();
        recipeProgress = getHost().getRecipeProgress();
        maxEnergy = getHost().getAEMaxPower();
        currentEnergy = getHost().getAECurrentPower();
        inputFluid = new FluidTankState(getHost().getFluidTanks().input().getFluid(),
                getHost().getFluidTanks().input().getCapacity());
        outputFluid = new FluidTankState(getHost().getFluidTanks().output().getFluid(),
                getHost().getFluidTanks().output().getCapacity());
        super.broadcastChanges();
    }
}
