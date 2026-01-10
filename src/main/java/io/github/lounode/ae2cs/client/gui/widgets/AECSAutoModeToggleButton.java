package io.github.lounode.ae2cs.client.gui.widgets;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 仅用于 {@link io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI} 的自动输入、输出按钮
 */
public class AECSAutoModeToggleButton extends AECSBackgroundToggleButton<AECSAutoModeToggleButton.State>
{
    public enum State
    {
        DISABLED,
        ENABLED
    }

    private final Consumer<Boolean> onChangedBool;

    public AECSAutoModeToggleButton(boolean initialEnabled, Component title, @NotNull Consumer<Boolean> onChangedBool)
    {
        super(State.class, initialEnabled ? State.ENABLED : State.DISABLED);
        this.onChangedBool = Objects.requireNonNull(onChangedBool);

        mapTooltipLines(State.DISABLED, List.of(title, Component.translatable("ae2cs.auto_mode.disabled")));
        mapTooltipLines(State.ENABLED, List.of(title, Component.translatable("ae2cs.auto_mode.enabled")));

        mapBackground(State.DISABLED, new BackgroundSet(
                AECSIcon.BUTTON_ORIGINAL_DARK,
                AECSIcon.BUTTON_ORIGINAL_DARK,
                AECSIcon.BUTTON_ORIGINAL_DARK_HOVER
        ));
        mapBackground(State.ENABLED, new BackgroundSet(
                AECSIcon.BUTTON_ORIGINAL_LIGHT,
                AECSIcon.BUTTON_ORIGINAL_LIGHT,
                AECSIcon.BUTTON_ORIGINAL_LIGHT_HOVER
        ));

        setOnValueChanged(state -> this.onChangedBool.accept(state == State.ENABLED));
    }

    public boolean isEnabled()
    {
        return getValue() == State.ENABLED;
    }

    public void setBoolean(boolean value)
    {
        if (value)
        {
            setValue(State.ENABLED);
        }
        else
        {
            setValue(State.DISABLED);
        }
    }

}
