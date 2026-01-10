package io.github.lounode.ae2cs.client.gui.widgets;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 仅用于 {@link io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI} 的自动输入、输出按钮
 */
public class AECSAutoModeToggleButton extends AECSColorToggleButton<AECSAutoModeToggleButton.State>
{
    public enum State
    {
        DISABLED,
        ENABLED
    }

    private final Consumer<Boolean> onChangedBool;

    public AECSAutoModeToggleButton(boolean initialEnabled, @NotNull Consumer<Boolean> onChangedBool)
    {
        super(State.class, initialEnabled ? State.ENABLED : State.DISABLED);
        this.onChangedBool = Objects.requireNonNull(onChangedBool);

        mapTooltip(State.DISABLED, Component.translatable("ae2cs.auto_mode.disabled"));
        mapTooltip(State.ENABLED, Component.translatable("ae2cs.auto_mode.enabled"));

        setOnValueChanged(state -> this.onChangedBool.accept(state == State.ENABLED));
    }

    public boolean isEnabled()
    {
        return getValue() == State.ENABLED;
    }

    @Override
    protected boolean useHoverBackground()
    {
        return isEnabled();
    }
}
