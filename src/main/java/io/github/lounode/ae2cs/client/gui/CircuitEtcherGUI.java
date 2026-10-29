package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CircuitEtcherMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CircuitEtcherGUI extends ResonatingFactoryScreen<CircuitEtcherMenu> {

    // 能量进度条
    private final AdvancedProgressBar energyRateBar;

    // 工作进度条
    private final AdvancedProgressBar workingProgressBar;

    public CircuitEtcherGUI(CircuitEtcherMenu menu, Inventory playerInventory, Component title) {
        this(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/circuit_etcher_menu.json"));
    }

    public CircuitEtcherGUI(CircuitEtcherMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.energyRateBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return (int) Math.ceil(getMenu().currentEnergy);
            }

            @Override
            public int getMaxProgress() {
                return (int) Math.ceil(getMenu().maxEnergy);
            }
        }, style.getImage("energyRateBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP, SimpleComponents.ENERGY_PROGRESS_BAR);
        widgets.add("energyRateBar", this.energyRateBar);

        this.workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeNeedTicks;
            }
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.TOP_BOTTOM_TO_CENTER, SimpleComponents.WORKING_PROGRESS_BAR);
        this.workingProgressBar.onClick(() -> RecipeViewerNavigation.show(RecipeViewerNavigation.MachineCategory.CIRCUIT_ETCHER));
        widgets.add("workingProgressBar", this.workingProgressBar);

        addToLeftToolbar(SideConfigGUI.iconButton());
    }
}
