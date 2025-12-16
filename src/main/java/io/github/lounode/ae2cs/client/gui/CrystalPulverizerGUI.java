package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.ProgressBar;
import appeng.menu.interfaces.IProgressProvider;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CrystalPulverizerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalPulverizerGUI extends UpgradeableScreen<CrystalPulverizerMenu>
{
    // 能量进度条
    private final ProgressBar energyRateBar;

    // 燃烧进度条
    private final ProgressBar workingProgressBar;

    public CrystalPulverizerGUI(CrystalPulverizerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_pulverizer_menu.json"));

        this.energyRateBar = new ProgressBar(new IProgressProvider()
        {
            @Override
            public int getCurrentProgress()
            {
                return (int) Math.ceil(getMenu().currentEnergy);
            }

            @Override
            public int getMaxProgress()
            {
                return (int) Math.ceil(getMenu().maxEnergy);
            }
        }, style.getImage("energyRateBar"), ProgressBar.Direction.VERTICAL, SimpleComponents.ENERGY_PROGRESS_BAR);
        widgets.add("energyRateBar", this.energyRateBar);

        this.workingProgressBar = new ProgressBar(new IProgressProvider()
        {
            @Override
            public int getCurrentProgress()
            {
                return getMenu().recipeProgress;
            }

            @Override
            public int getMaxProgress()
            {
                return getMenu().recipeNeedTicks;
            }
        }, style.getImage("workingProgressBar"), ProgressBar.Direction.HORIZONTAL, SimpleComponents.WORKING_PROGRESS_BAR);
        widgets.add("workingProgressBar", this.workingProgressBar);
    }
}
