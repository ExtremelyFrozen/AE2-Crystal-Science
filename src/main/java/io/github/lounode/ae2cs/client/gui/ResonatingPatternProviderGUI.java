package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.ResonatingPatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingPatternProviderGUI extends PatternProviderScreen<ResonatingPatternProviderMenu>
{
    AECSServerSettingToggleButton<PullMode> changePullModeButton;

    public ResonatingPatternProviderGUI(ResonatingPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        changePullModeButton = new AECSServerSettingToggleButton<>(AECSSettings.PULL_MODE, PullMode.PULL_OFF);
        addToLeftToolbar(changePullModeButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.changePullModeButton.set(menu.pullMode);
    }
}
