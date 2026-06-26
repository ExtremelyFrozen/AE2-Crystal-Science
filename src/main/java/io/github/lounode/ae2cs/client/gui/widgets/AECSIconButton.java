package io.github.lounode.ae2cs.client.gui.widgets;

import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * {@link appeng.client.gui.widgets.IconButton}的改写版：把 Icon 换成可扩展的 IButtonIcon
 */
public abstract class AECSIconButton extends Button implements ITooltip {

    private boolean halfSize = false;
    private boolean disableClickSound = false;
    private boolean disableBackground = false;

    public AECSIconButton(OnPress onPress) {
        super(0, 0, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    public void setVisibility(boolean vis) {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundHandler) {
        if (!disableClickSound) {
            super.playDownSound(soundHandler);
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) {
            return;
        }

        var icon = this.getIcon();
        var item = this.getItemOverlay();

        if (this.halfSize) {
            this.width = 8;
            this.height = 8;
        }

        if (this.halfSize) {
            if (!disableBackground) {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }

            if (item != null) {
                guiGraphics.renderItem(new ItemStack(item), getX(), getY(), 0, 20);
            } else if (icon != null) {
                Blitter blitter = icon.getBlitter();
                if (!this.active) {
                    blitter.opacity(0.5f);
                }
                blitter.dest(getX(), getY()).blit(guiGraphics);
            }
        } else {
            if (!disableBackground) {
                Icon bgIcon = Icon.TOOLBAR_BUTTON_BACKGROUND;

                bgIcon.getBlitter()
                        .dest(getX(), getY(), this.width, this.height)
                        .blit(guiGraphics);
            }

            if (item != null) {
                guiGraphics.renderItem(new ItemStack(item), getX() + (this.width - 16) / 2, getY() + (this.height - 16) / 2, 0, 3);
            } else if (icon != null) {
                icon.getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }
        }
    }

    /**
     * 返回要绘制的图标。允许返回 null（则只画背景/物品 overlay）。
     */
    @Nullable
    protected abstract IButtonIcon getIcon();

    /**
     * 若不为 null，则优先绘制物品覆盖层。
     */
    @Nullable
    protected Item getItemOverlay() {
        return null;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                getX(),
                getY(),
                this.halfSize ? 8 : 16,
                this.halfSize ? 8 : 16);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public boolean isHalfSize() {
        return this.halfSize;
    }

    public void setHalfSize(boolean halfSize) {
        this.halfSize = halfSize;
    }

    public boolean isDisableClickSound() {
        return disableClickSound;
    }

    public void setDisableClickSound(boolean disableClickSound) {
        this.disableClickSound = disableClickSound;
    }

    public boolean isDisableBackground() {
        return disableBackground;
    }

    public void setDisableBackground(boolean disableBackground) {
        this.disableBackground = disableBackground;
    }
}
