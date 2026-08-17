package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.EntropyFluidMode;
import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.client.gui.widgets.FluidTankWidget;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.EntropyVariationReactionChamberMenu;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.interfaces.IProgressProvider;
import appeng.recipes.entropy.EntropyMode;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class EntropyVariationReactionChamberGUI extends UpgradeableScreen<EntropyVariationReactionChamberMenu> {

    // 能量进度条
    private final AdvancedProgressBar energyRateBar;

    // 工作进度条
    private final AdvancedProgressBar workingProgressBar;

    // 侧边按钮切换熵变模式
    private final AECSServerSettingToggleButton<EntropyMode> entropyModeButton;

    private final AECSServerSettingToggleButton<EntropyFluidMode> entropyFluidModeButton;

    public EntropyVariationReactionChamberGUI(EntropyVariationReactionChamberMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
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
        }, style.getImage("workingProgressBar"), AdvancedProgressBar.FillMode.LEFT_TO_RIGHT, SimpleComponents.WORKING_PROGRESS_BAR);
        this.workingProgressBar.onClick(() -> RecipeViewerNavigation.show(RecipeViewerNavigation.MachineCategory.ENTROPY_REACTION));
        widgets.add("workingProgressBar", this.workingProgressBar);

        widgets.add("fluidInput", new FluidTankWidget(8, 20, () -> getMenu().inputFluid,
                () -> getMenu().sendFillFluidInputAction(),
                () -> getMenu().entropyFluidMode == EntropyFluidMode.FLOWING ? List.of(Component.translatable("ae2cs.tooltip.fluid_flowing")) : List.of()));
        widgets.add("fluidOutput", new FluidTankWidget(150, 20, () -> getMenu().outputFluid,
                () -> getMenu().sendDrainFluidOutputAction()));

        entropyModeButton = new AECSServerSettingToggleButton<>(AECSSettings.ENTROPY_CHANGE_MODE, EntropyMode.HEAT);
        addToLeftToolbar(entropyModeButton);
        entropyFluidModeButton = new AECSServerSettingToggleButton<>(AECSSettings.ENTROPY_FLUID_MODE, EntropyFluidMode.STILL);
        addToLeftToolbar(entropyFluidModeButton);
        addToLeftToolbar(SideConfigGUI.iconButton());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.entropyModeButton.set(getMenu().entropyMode);
        this.entropyFluidModeButton.set(getMenu().entropyFluidMode);
    }
}
