package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 枚举状态切换按钮（背景贴图替换版）：
 * - 基于{@link AECSIconButton}
 */
public class AECSBackgroundToggleButton<E extends Enum<E>> extends AECSIconButton
{
    /**
     * 背景贴图三态：normal / focus / hover
     */
    public record BackgroundSet(@NotNull IButtonIcon normal,
                                @NotNull IButtonIcon focus,
                                @NotNull IButtonIcon hover)
    {
        public BackgroundSet
        {
            Objects.requireNonNull(normal);
            Objects.requireNonNull(focus);
            Objects.requireNonNull(hover);
        }

        public static BackgroundSet toolbar()
        {
            return new BackgroundSet(
                    AdaptedAE2Icon.TOOLBAR_BUTTON_BACKGROUND,
                    AdaptedAE2Icon.TOOLBAR_BUTTON_BACKGROUND,
                    AdaptedAE2Icon.TOOLBAR_BUTTON_BACKGROUND
            );
        }
    }

    protected final Class<E> enumClass;
    protected final E[] values;

    protected E value;

    // state -> mapping
    protected final EnumMap<E, BackgroundSet> backgroundByState;
    protected final EnumMap<E, List<Component>> tooltipLinesByState;
    protected final EnumMap<E, @Nullable IButtonIcon> iconByState;

    protected Consumer<E> onValueChanged = e -> {
    };

    public AECSBackgroundToggleButton(@NotNull Class<E> enumClass, @NotNull E initial)
    {
        super(btn -> { /* onPress 由本类覆写 */ });

        this.enumClass = Objects.requireNonNull(enumClass);
        this.values = Objects.requireNonNull(enumClass.getEnumConstants());
        this.value = Objects.requireNonNull(initial);

        this.backgroundByState = new EnumMap<>(enumClass);
        this.tooltipLinesByState = new EnumMap<>(enumClass);
        this.iconByState = new EnumMap<>(enumClass);
    }

    @Override
    public void onPress()
    {
        cycleNext();
    }

    public @NotNull E getValue()
    {
        return value;
    }

    public void setValue(@NotNull E value)
    {
        this.value = Objects.requireNonNull(value);
    }

    public AECSBackgroundToggleButton<E> setOnValueChanged(@NotNull Consumer<E> onValueChanged)
    {
        this.onValueChanged = Objects.requireNonNull(onValueChanged);
        return this;
    }

    protected void cycleNext()
    {
        int step = 1;

        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen<?> ae && ae.isHandlingRightClick())
        {
            step = -1;
        }

        int len = values.length;
        int idx = (this.value.ordinal() + step) % len;
        if (idx < 0)
        {
            idx += len;
        }

        this.value = values[idx];
        this.onValueChanged.accept(this.value);
    }

    public AECSBackgroundToggleButton<E> mapBackground(@NotNull E state, @NotNull BackgroundSet set)
    {
        backgroundByState.put(Objects.requireNonNull(state), Objects.requireNonNull(set));
        return this;
    }

    public AECSBackgroundToggleButton<E> mapBackgroundAll(@NotNull BackgroundSet set)
    {
        Objects.requireNonNull(set);
        for (E v : values) backgroundByState.put(v, set);
        return this;
    }

    /**
     * 单行 tooltip
     */
    public AECSBackgroundToggleButton<E> mapTooltip(@NotNull E state, @NotNull Component tooltip)
    {
        return mapTooltipLines(state, Collections.singletonList(Objects.requireNonNull(tooltip)));
    }

    /**
     * 多行 tooltip
     */
    public AECSBackgroundToggleButton<E> mapTooltipLines(@NotNull E state, @NotNull List<Component> lines)
    {
        Objects.requireNonNull(lines);
        tooltipLinesByState.put(Objects.requireNonNull(state), List.copyOf(lines));
        return this;
    }

    public AECSBackgroundToggleButton<E> mapIcon(@NotNull E state, @Nullable IButtonIcon icon)
    {
        iconByState.put(Objects.requireNonNull(state), icon);
        return this;
    }

    @Override
    public List<Component> getTooltipMessage()
    {
        List<Component> lines = tooltipLinesByState.get(this.value);
        if (lines != null && !lines.isEmpty())
        {
            return lines;
        }
        return Collections.singletonList(getMessage());
    }

    @Override
    protected @Nullable IButtonIcon getIcon()
    {
        return iconByState.get(this.value);
    }

    protected @NotNull BackgroundSet getBackgroundSet()
    {
        BackgroundSet set = backgroundByState.get(this.value);
        return set != null ? set : BackgroundSet.toolbar();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial)
    {
        if (!this.visible)
        {
            return;
        }

        var icon = this.getIcon();
        var item = this.getItemOverlay();

        if (this.isHalfSize())
        {
            this.width = 8;
            this.height = 8;
        }

        final BackgroundSet bgSet = getBackgroundSet();

        if (this.isHalfSize())
        {
            if (!isDisableBackground())
            {
                bgSet.normal().getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }

            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), getX(), getY(), 0, 20);
            }
            else if (icon != null)
            {
                Blitter blitter = icon.getBlitter();
                if (!this.active)
                {
                    blitter.opacity(0.5f);
                }
                blitter.dest(getX(), getY()).blit(guiGraphics);
            }
        }
        else
        {
            if (!isDisableBackground())
            {
                IButtonIcon bgIcon = isHovered() ? bgSet.hover()
                        : isFocused() ? bgSet.focus()
                        : bgSet.normal();

                bgIcon.getBlitter()
                        .dest(getX(), getY(), this.width, this.height)
                        .blit(guiGraphics);
            }

            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), getX() + (this.width - 16) / 2, getY() + (this.height - 16) / 2, 0, 3);
            }
            else if (icon != null)
            {
                icon.getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }
        }
    }
}