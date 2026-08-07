package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CrystalInfuserMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalInfuserGUI extends UpgradeableScreen<CrystalInfuserMenu> {

    private final AdvancedProgressBar fluidBar;

    public CrystalInfuserGUI(CrystalInfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_infuser_menu.json"));

        AdvancedProgressBar energyBar = new AdvancedProgressBar(new IProgressProvider() {

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
        widgets.add("energyRateBar", energyBar);

        fluidBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().fluidAmount;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().fluidCapacity;
            }
        }, style.getImage("fluidTank"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP);
        widgets.add("fluidTank", fluidBar);

        AdvancedProgressBar operationBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeNeedTicks;
            }
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT,
                SimpleComponents.WORKING_PROGRESS_BAR);
        operationBar.onClick(() -> RecipeViewerNavigation.show(RecipeViewerNavigation.MachineCategory.CRYSTAL_INFUSER));
        widgets.add("workingProgressBar", operationBar);

        addToLeftToolbar(SideConfigGUI.iconButton());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        fluidBar.setFullMsg(Component.translatable("ae2cs.tooltip.fluid_amount",
                getMenu().fluidAmount, getMenu().fluidCapacity));
    }
}
