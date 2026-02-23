package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
                    AdaptedAE2Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS,
                    AdaptedAE2Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
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

        IButtonIcon icon = this.getIcon();
        Blitter iconBlitter = icon != null ? icon.getBlitter() : null;
        if (iconBlitter != null && !this.active)
        {
            iconBlitter.opacity(0.5F);
        }

        if (this.isHalfSize())
        {
            this.width = 8;
            this.height = 8;
        }
        else
        {
            this.width = 16;
            this.height = 16;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        if (this.isFocused())
        {
            guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), -1);
            guiGraphics.fill(this.getX() - 1, this.getY(), this.getX(), this.getY() + this.height, -1);
            guiGraphics.fill(this.getX() + this.width, this.getY(), this.getX() + this.width + 1, this.getY() + this.height, -1);
            guiGraphics.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, -1);
        }

        final BackgroundSet bgSet = getBackgroundSet();

        if (this.isHalfSize())
        {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(this.getX(), this.getY(), 0.0F);
            pose.scale(0.5F, 0.5F, 1.0F);

            if (!isDisableBackground())
            {
                bgSet.normal().getBlitter()
                        .dest(0, 0)
                        .blit(guiGraphics);
            }

            var item = this.getItemOverlay();
            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), 0, 0);
            }
            else if (iconBlitter != null)
            {
                iconBlitter.dest(0, 0).blit(guiGraphics);
            }

            pose.popPose();
        }
        else
        {
            if (!isDisableBackground())
            {
                IButtonIcon bgIcon = isHovered() ? bgSet.hover()
                        : isFocused() ? bgSet.focus()
                        : bgSet.normal();

                bgIcon.getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }

            var item = this.getItemOverlay();
            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), getX(), getY());
            }
            else if (iconBlitter != null)
            {
                iconBlitter.dest(getX(), getY()).blit(guiGraphics);
            }
        }

        RenderSystem.enableDepthTest();
    }
}
