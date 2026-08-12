package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.client.gui.widgets.FluidTankWidget;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.PulseCentrifugeMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PulseCentrifugeGUI extends UpgradeableScreen<PulseCentrifugeMenu> {

    private final AdvancedProgressBar energyBar;
    private final AdvancedProgressBar workingProgressBar;
    private final FluidTankWidget inputFluidTank;
    private final FluidTankWidget outputFluidTank;

    public PulseCentrifugeGUI(PulseCentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/pulse_centrifuge_menu.json"));

        energyBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return (int) Math.ceil(getMenu().currentEnergy);
            }

            @Override
            public int getMaxProgress() {
                return (int) Math.ceil(getMenu().maxEnergy);
            }
        }, style.getImage("energyRateBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP,
                SimpleComponents.ENERGY_PROGRESS_BAR);
        workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeEnergyCost;
            }
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT,
                SimpleComponents.WORKING_PROGRESS_BAR);
        workingProgressBar.onClick(() -> RecipeViewerNavigation.show(
                RecipeViewerNavigation.MachineCategory.PULSE_CENTRIFUGE));

        inputFluidTank = new FluidTankWidget(8, 20, 18, 60, () -> getMenu().inputFluid,
                () -> getMenu().sendFillFluidInputAction());
        outputFluidTank = new FluidTankWidget(150, 20, 18, 60, () -> getMenu().outputFluid,
                () -> getMenu().sendDrainFluidOutputAction());

        addToLeftToolbar(SideConfigGUI.iconButton());
    }

    @Override
    protected void init() {
        super.init();
        energyBar.setX(leftPos + 135);
        energyBar.setY(topPos + 40);
        addRenderableWidget(energyBar);

        workingProgressBar.setX(leftPos + 73);
        workingProgressBar.setY(topPos + 36);
        addRenderableWidget(workingProgressBar);

        inputFluidTank.setX(leftPos + 8);
        inputFluidTank.setY(topPos + 20);
        addRenderableWidget(inputFluidTank);

        outputFluidTank.setX(leftPos + 150);
        outputFluidTank.setY(topPos + 20);
        addRenderableWidget(outputFluidTank);
    }
}
