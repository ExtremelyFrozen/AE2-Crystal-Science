package io.github.lounode.ae2cs.client.gui;

import appeng.api.config.RedstoneMode;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.QuartzOscillatorClockMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuartzOscillatorClockGUI extends UpgradeableScreen<QuartzOscillatorClockMenu>
{
    private final AECSServerSettingToggleButton<RedstoneMode> redstoneMode;
    private final NumberEntryWidget level;

    public QuartzOscillatorClockGUI(QuartzOscillatorClockMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);
        this.redstoneMode = new AECSServerSettingToggleButton<>(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.HIGH_SIGNAL);
        this.addToLeftToolbar(this.redstoneMode);

        this.level = widgets.addNumberEntryWidget("level", NumberEntryType.UNITLESS);
        this.level.setTextFieldStyle(style.getWidget("level_input"));
        this.level.setOnChange(this::onValueChange);
        this.level.setOnConfirm(this::onClose);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.redstoneMode.active = menu.hasUpgrade(AEItems.REDSTONE_CARD);
        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(this.redstoneMode.active);

        this.level.setLongValue(this.menu.pulseCountHold);

    }

    private void onValueChange()
    {
        this.level.getIntValue().ifPresent(value -> {
            if (value != menu.pulseCountHold)
                menu.sendChangePulseCountHold(value);
        });
    }
}
