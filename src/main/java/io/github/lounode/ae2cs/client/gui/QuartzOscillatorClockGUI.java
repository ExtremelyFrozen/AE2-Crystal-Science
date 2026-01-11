package io.github.lounode.ae2cs.client.gui;

import appeng.api.config.RedstoneMode;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.SoundMode;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.QuartzOscillatorClockMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuartzOscillatorClockGUI extends UpgradeableScreen<QuartzOscillatorClockMenu>
{
    private final AECSServerSettingToggleButton<SoundMode> soundModeButton;
    private final AECSServerSettingToggleButton<RedstoneMode> redstoneMode;
    private final NumberEntryWidget levelCountHold;
    private final NumberEntryWidget levelPulseWidth;

    public QuartzOscillatorClockGUI(QuartzOscillatorClockMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);
        this.soundModeButton = new AECSServerSettingToggleButton<>(AECSSettings.SOUND_MODE, SoundMode.UNMUTE);
        this.addToLeftToolbar(soundModeButton);
        this.redstoneMode = new AECSServerSettingToggleButton<>(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.HIGH_SIGNAL);
        this.addToLeftToolbar(this.redstoneMode);

        this.levelCountHold = widgets.addNumberEntryWidget("level_count_hold", NumberEntryType.UNITLESS);
        this.levelCountHold.setTextFieldStyle(style.getWidget("level_count_hold_input"));
        this.levelCountHold.setOnChange(this::onCountDownChange);
        this.levelCountHold.setOnConfirm(this::onClose);

        this.levelPulseWidth = widgets.addNumberEntryWidget("level_pulse_width", NumberEntryType.UNITLESS);
        this.levelPulseWidth.setTextFieldStyle(style.getWidget("level_pulse_width_input"));
        this.levelPulseWidth.setOnChange(this::onPulseWidthChange);
        this.levelPulseWidth.setOnConfirm(this::onClose);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.soundModeButton.set(menu.soundMode);

        this.redstoneMode.active = menu.hasUpgrade(AEItems.REDSTONE_CARD);
        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(this.redstoneMode.active);

        this.levelCountHold.setLongValue(this.menu.pulseCountHold);
        this.levelPulseWidth.setLongValue(this.menu.pulseWidthTicks);

    }

    private void onCountDownChange()
    {
        this.levelCountHold.getIntValue().ifPresent(value -> {
            if (value != menu.pulseCountHold)
                menu.sendChangePulseCountHold(value);
        });
    }

    private void onPulseWidthChange()
    {
        this.levelPulseWidth.getIntValue().ifPresent(value -> {
            if (value != menu.pulseWidthTicks)
                menu.sendChangePulseWidthTicks(value);
        });
    }
}
