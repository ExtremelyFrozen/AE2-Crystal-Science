package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.client.gui.widgets.FluidTankWidget;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CrystalInfuserMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalInfuserGUI extends UpgradeableScreen<CrystalInfuserMenu> {

    private final AdvancedProgressBar energyBar;
    private final AdvancedProgressBar operationBar;
    private final FluidTankWidget inputFluidTank;
    private final FluidTankWidget outputFluidTank;

    public CrystalInfuserGUI(CrystalInfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_infuser_menu.json"));

        energyBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return (int) Math.ceil(getMenu().currentEnergy);
            }

            @Override
            public int getMaxProgress() {
                return (int) Math.ceil(getMenu().maxEnergy);
            }
        }, AECSBlitter.crystalInfuserEnergy, AdvancedProgressBar.FillMode.BOTTOM_TO_TOP,
                SimpleComponents.ENERGY_PROGRESS_BAR);

        inputFluidTank = new FluidTankWidget(8, 20, 18, 60, () -> getMenu().inputFluid,
                () -> getMenu().sendFillFluidInputAction());
        outputFluidTank = new FluidTankWidget(150, 20, 18, 60, () -> getMenu().outputFluid,
                () -> getMenu().sendDrainFluidOutputAction());

        operationBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeNeedTicks;
            }
        }, AECSBlitter.crystalInfuserProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT,
                SimpleComponents.WORKING_PROGRESS_BAR);
        operationBar.onClick(() -> RecipeViewerNavigation.show(RecipeViewerNavigation.MachineCategory.CRYSTAL_INFUSER));

        addToLeftToolbar(SideConfigGUI.iconButton());
    }

    @Override
    protected void init() {
        super.init();
        energyBar.setX(leftPos + 135);
        energyBar.setY(topPos + 40);
        addRenderableWidget(energyBar);

        operationBar.setX(leftPos + 73);
        operationBar.setY(topPos + 36);
        addRenderableWidget(operationBar);

        inputFluidTank.setX(leftPos + 8);
        inputFluidTank.setY(topPos + 20);
        addRenderableWidget(inputFluidTank);

        outputFluidTank.setX(leftPos + 150);
        outputFluidTank.setY(topPos + 20);
        addRenderableWidget(outputFluidTank);
    }
}
