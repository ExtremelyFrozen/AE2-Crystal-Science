package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.interfaces.IProgressProvider;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CrystalAggregatorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalAggregatorGUI extends UpgradeableScreen<CrystalAggregatorMenu>
{
    // 能量进度条
    private final AdvancedProgressBar energyRateBar;

    // 工作进度条
    private final AdvancedProgressBar workingProgressBar;

    public CrystalAggregatorGUI(CrystalAggregatorMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_aggregator_menu.json"));

        this.energyRateBar = new AdvancedProgressBar(new IProgressProvider()
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
        }, style.getImage("energyRateBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP, SimpleComponents.ENERGY_PROGRESS_BAR);
        widgets.add("energyRateBar", this.energyRateBar);

        this.workingProgressBar = new AdvancedProgressBar(new IProgressProvider()
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
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT, SimpleComponents.WORKING_PROGRESS_BAR);
        widgets.add("workingProgressBar", this.workingProgressBar);
    }
}
