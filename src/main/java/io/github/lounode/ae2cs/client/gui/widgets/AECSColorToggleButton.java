package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 枚举状态切换按钮：
 * - 内部维护：状态 -> 背景 tint / tooltip / icon 的映射表
 * - hover 时不显示 hover 背景（不高亮），但仍正常下陷
 * - 背景变色仅作用于背景，icon 不变色
 */
public class AECSColorToggleButton<E extends Enum<E>> extends AECSIconButton
{
    protected final Class<E> enumClass;
    protected final E[] values;

    protected E value;

    // 映射表
    protected final EnumMap<E, Integer> backgroundTintArgb;
    protected final EnumMap<E, Component> tooltipByState;
    protected final EnumMap<E, @Nullable IButtonIcon> iconByState;

    protected Consumer<E> onValueChanged = e -> {
    };

    public AECSColorToggleButton(@NotNull Class<E> enumClass, @NotNull E initial)
    {
        super(btn -> { /* onPress 由本类覆写 */ });

        this.enumClass = Objects.requireNonNull(enumClass);
        this.values = Objects.requireNonNull(enumClass.getEnumConstants());
        this.value = Objects.requireNonNull(initial);

        this.backgroundTintArgb = new EnumMap<>(enumClass);
        this.tooltipByState = new EnumMap<>(enumClass);
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

    public AECSColorToggleButton<E> setOnValueChanged(@NotNull Consumer<E> onValueChanged)
    {
        this.onValueChanged = Objects.requireNonNull(onValueChanged);
        return this;
    }

    /**
     * 为某个状态映射背景 tint（ARGB，alpha=0 表示不 tint）
     */
    public AECSColorToggleButton<E> mapBackgroundTint(@NotNull E state, int argb)
    {
        backgroundTintArgb.put(Objects.requireNonNull(state), argb);
        return this;
    }

    /**
     * 批量映射：同色
     */
    public AECSColorToggleButton<E> mapBackgroundTintAll(int argb)
    {
        for (E v : values) backgroundTintArgb.put(v, argb);
        return this;
    }

    /**
     * 为某个状态映射 tooltip
     */
    public AECSColorToggleButton<E> mapTooltip(@NotNull E state, @NotNull Component tooltip)
    {
        tooltipByState.put(Objects.requireNonNull(state), Objects.requireNonNull(tooltip));
        return this;
    }

    /**
     * 为某个状态映射 icon（可为 null）
     */
    public AECSColorToggleButton<E> mapIcon(@NotNull E state, @Nullable IButtonIcon icon)
    {
        iconByState.put(Objects.requireNonNull(state), icon);
        return this;
    }

    /**
     * 默认：hover 不使用高亮背景。子类可覆写。
     */
    protected boolean useHoverBackground()
    {
        return false;
    }

    protected int getYOffset()
    {
        return isHovered() ? 1 : 0;
    }

    protected void cycleNext()
    {
        int idx = this.value.ordinal() + 1;
        if (idx >= values.length) idx = 0;

        this.value = values[idx];
        this.onValueChanged.accept(this.value);
    }

    @Override
    public List<Component> getTooltipMessage()
    {
        Component tip = tooltipByState.get(this.value);
        if (tip != null) return Collections.singletonList(tip);
        return Collections.singletonList(getMessage());
    }

    @Override
    protected @Nullable IButtonIcon getIcon()
    {
        return iconByState.get(this.value);
    }

    protected int getBackgroundTint()
    {
        return backgroundTintArgb.getOrDefault(this.value, 0x00000000);
    }

    @Override
    public void setHalfSize(boolean halfSize)
    {
        super.setHalfSize(halfSize);
        this.width = halfSize ? 8 : 16;
        this.height = halfSize ? 8 : 16;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial)
    {
        if (!this.visible)
        {
            return;
        }

        final var icon = this.getIcon();
        final var item = this.getItemOverlay();
        final int yOffset = getYOffset();
        final int tint = getBackgroundTint();

        if (this.isHalfSize())
        {
            if (!isDisableBackground())
            {
                Blitter bg = Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().copy();
                if ((tint >>> 24) != 0) bg.colorArgb(tint);
                bg.dest(getX(), getY()).zOffset(10).blit(guiGraphics);
            }

            if (item != null)
            {
                guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(item), getX(), getY(), 0, 20);
                return;
            }

            if (icon != null)
            {
                Blitter blitter = icon.getBlitter().copy();
                if (!this.active) blitter.opacity(0.5f);
                blitter.dest(getX(), getY()).zOffset(20).blit(guiGraphics);
            }
        }
        else
        {
            if (!isDisableBackground())
            {
                Icon bgIcon =
                        (useHoverBackground() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                                : (isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND));

                Blitter bg = bgIcon.getBlitter().copy();
                if ((tint >>> 24) != 0) bg.colorArgb(tint);

                bg.dest(getX() - 1, getY() + yOffset, 18, 20).zOffset(2).blit(guiGraphics);
            }

            if (item != null)
            {
                guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(item), getX(), getY() + 1 + yOffset, 0, 3);
                return;
            }

            if (icon != null)
            {
                Blitter blitter = icon.getBlitter().copy();
                if (!this.active) blitter.opacity(0.5f);
                blitter.dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
            }
        }
    }
}