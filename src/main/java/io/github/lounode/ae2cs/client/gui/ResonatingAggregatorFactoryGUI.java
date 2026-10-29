package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.client.gui.widgets.FluidTankWidget;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.ResonatingAggregatorFactoryMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingAggregatorFactoryGUI extends ResonatingFactoryScreen<ResonatingAggregatorFactoryMenu> {

    public ResonatingAggregatorFactoryGUI(ResonatingAggregatorFactoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/resonating_aggregator_factory_menu.json"));

        widgets.add("energyRateBar", new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return (int) Math.ceil(getMenu().currentEnergy);
            }

            @Override
            public int getMaxProgress() {
                return (int) Math.ceil(getMenu().maxEnergy);
            }
        }, style.getImage("energyRateBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP, SimpleComponents.ENERGY_PROGRESS_BAR));

        var workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeNeedTicks;
            }
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT, SimpleComponents.WORKING_PROGRESS_BAR);
        workingProgressBar.onClick(() -> RecipeViewerNavigation.show(RecipeViewerNavigation.MachineCategory.CRYSTAL_AGGREGATOR));
        widgets.add("workingProgressBar", workingProgressBar);

        widgets.add("fluidInput", new FluidTankWidget(8, 20, () -> getMenu().inputFluid,
                () -> getMenu().sendFillFluidInputAction()));
        widgets.add("fluidOutput", new FluidTankWidget(150, 20, () -> getMenu().outputFluid,
                () -> getMenu().sendDrainFluidOutputAction()));

        addToLeftToolbar(SideConfigGUI.iconButton());
    }
}
