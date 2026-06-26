package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.QuartzGrindstoneMenu;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuartzGrindstoneGUI extends UpgradeableScreen<QuartzGrindstoneMenu> {

    // 燃烧进度条
    private final AdvancedProgressBar workingProgressBar;

    public QuartzGrindstoneGUI(QuartzGrindstoneMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/quartz_grindstone_menu.json"));

        this.workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress() {
                return getMenu().recipeNeedTicks;
            }
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT, SimpleComponents.WORKING_PROGRESS_BAR);
        widgets.add("workingProgressBar", this.workingProgressBar);
        addToLeftToolbar(SideConfigGUI.iconButton());
    }
}
