package io.github.lounode.ae2cs.client.gui;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.ResonatingPatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ResonatingPatternProviderGUI extends PatternProviderScreen<ResonatingPatternProviderMenu>
{
    AECSServerSettingToggleButton<PullMode> changePullModeButton;

    public ResonatingPatternProviderGUI(ResonatingPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        try
        {
            this.widgets.add("upgrades", new UpgradesPanel(
                    menu.getSlots(SlotSemantics.UPGRADE),
                    this::getCompatibleUpgrades));
        }
        catch (IllegalStateException e)
        {
            // 可能有其他模组完成了添加，静默处理
        }
        if (menu.getToolbox().isPresent())
        {
            this.widgets.add("toolbox", new ToolboxPanel(style, menu.getToolbox().getName()));
        }

        changePullModeButton = new AECSServerSettingToggleButton<>(AECSSettings.PULL_MODE, PullMode.PULL_OFF);
        addToLeftToolbar(changePullModeButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.changePullModeButton.set(menu.pullMode);
    }

    private List<Component> getCompatibleUpgrades()
    {
        var list = new ArrayList<Component>();
        list.add(GuiText.CompatibleUpgrades.text());
        list.addAll(Upgrades.getTooltipLinesForMachine(menu.getUpgrades().getUpgradableItem()));
        return list;
    }
}
