package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.interfaces.IProgressProvider;
import appeng.recipes.entropy.EntropyMode;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.EntropyVariationReactionChamberMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EntropyVariationReactionChamberGUI extends UpgradeableScreen<EntropyVariationReactionChamberMenu>
{
    // 能量进度条
    private final AdvancedProgressBar energyRateBar;

    // 工作进度条
    private final AdvancedProgressBar workingProgressBar;

    // 侧边按钮切换熵变模式
    private final AECSServerSettingToggleButton<EntropyMode> entropyModeButton;

    public EntropyVariationReactionChamberGUI(EntropyVariationReactionChamberMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

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

        entropyModeButton = new AECSServerSettingToggleButton<>(AECSSettings.ENTROPY_CHANGE_MODE, EntropyMode.HEAT);
        addToLeftToolbar(entropyModeButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.entropyModeButton.set(getMenu().entropyMode);
    }
}