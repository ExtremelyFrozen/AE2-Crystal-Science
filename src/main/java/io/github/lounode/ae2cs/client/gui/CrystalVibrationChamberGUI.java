package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.CommonButtons;
import appeng.menu.interfaces.IProgressProvider;
import appeng.util.Platform;
import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.location.SimpleComponents;
import io.github.lounode.ae2cs.common.menu.CrystalVibrationChamberMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalVibrationChamberGUI extends UpgradeableScreen<CrystalVibrationChamberMenu>
{
    // 能量进度条
    private final AdvancedProgressBar generationRateBar;

    // 燃烧进度条
    private final AdvancedProgressBar burnProgressBar;

    // 将使用样式 JSON，背景由样式管理
    public CrystalVibrationChamberGUI(CrystalVibrationChamberMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/crystal_vibration_chamber_menu.json"));

        this.generationRateBar = new AdvancedProgressBar(new EnergyProgress(this.getMenu()),
                style.getImage("generationRateBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP);
        widgets.add("generationRateBar", this.generationRateBar);

        this.burnProgressBar = new AdvancedProgressBar(new BurnProgress(this.getMenu()),
                style.getImage("burnProgressBar"), AdvancedProgressBar.FillMode.BOTTOM_TO_TOP, SimpleComponents.BURNING_PROGRESS_BAR);
        widgets.add("burnProgressBar", this.burnProgressBar);

        addToLeftToolbar(CommonButtons.togglePowerUnit());
        addToLeftToolbar(SideConfigGUI.iconButton());
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        var powerPerTick = this.menu.energyPerTick;
        this.generationRateBar.setFullMsg(Component.literal(Platform.formatPower(powerPerTick, true)
                + "\n" + Platform.formatPower(menu.storedAE, false) + "/"
                + Platform.formatPower(menu.maxStoredAE, false)));
    }

    public static class EnergyProgress implements IProgressProvider
    {
        private final CrystalVibrationChamberMenu menu;

        public EnergyProgress(CrystalVibrationChamberMenu menu)
        {
            this.menu = menu;
        }

        @Override
        public int getCurrentProgress()
        {
            return (int) Math.ceil(menu.storedAE);
        }

        @Override
        public int getMaxProgress()
        {
            return (int) Math.ceil(menu.maxStoredAE);
        }
    }

    public static class BurnProgress implements IProgressProvider
    {
        private final CrystalVibrationChamberMenu menu;

        public BurnProgress(CrystalVibrationChamberMenu menu)
        {
            this.menu = menu;
        }

        @Override
        public int getCurrentProgress()
        {
            return menu.burnTime; // 一个自然递减量，会使燃烧呈现出火力慢慢降低的效果
        }

        @Override
        public int getMaxProgress()
        {
            return menu.maxBurnTime;
        }
    }
}