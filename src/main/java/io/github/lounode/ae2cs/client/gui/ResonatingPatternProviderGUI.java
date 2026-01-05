package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.common.menu.ResonatingPatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingPatternProviderGUI extends PatternProviderScreen<ResonatingPatternProviderMenu>
{
    AECSToggleButton changePullModeButton;

    public ResonatingPatternProviderGUI(ResonatingPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        changePullModeButton = new AECSToggleButton(AECSIcon.PULL_MODE_ON, AECSIcon.PULL_MODE_OFF,
                Component.translatable("ae2cs.menu.resonating_provider.pull_mode_title"),
                Component.translatable("ae2cs.menu.resonating_provider.pull_mode_desc"),
                btn -> menu.sendChangePullMode(!menu.enableChangePullMode));
        addToLeftToolbar(changePullModeButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.changePullModeButton.setState(menu.enableChangePullMode);
    }
}
