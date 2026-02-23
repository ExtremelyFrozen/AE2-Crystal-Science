package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
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
public abstract class AECSIconButton extends Button implements ITooltip
{

    private boolean halfSize = false;
    private boolean disableClickSound = false;
    private boolean disableBackground = false;

    public AECSIconButton(OnPress onPress)
    {
        super(0, 0, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    public void setVisibility(boolean vis)
    {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundHandler)
    {
        if (!disableClickSound)
        {
            super.playDownSound(soundHandler);
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial)
    {
        if (!this.visible)
        {
            return;
        }

        IButtonIcon icon = this.getIcon();
        Blitter blitter = icon != null ? icon.getBlitter() : null;
        if (blitter != null && !this.active)
        {
            blitter.opacity(0.5F);
        }

        if (this.halfSize)
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

        if (this.halfSize)
        {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(this.getX(), this.getY(), 0.0F);
            pose.scale(0.5F, 0.5F, 1.0F);

            if (!disableBackground)
            {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter()
                        .dest(0, 0)
                        .blit(guiGraphics);
            }

            Item item = this.getItemOverlay();
            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), 0, 0);
            }
            else if (blitter != null)
            {
                blitter.dest(0, 0).blit(guiGraphics);
            }

            pose.popPose();
        }
        else
        {
            if (!disableBackground)
            {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter()
                        .dest(getX(), getY())
                        .blit(guiGraphics);
            }

            Item item = this.getItemOverlay();
            if (item != null)
            {
                guiGraphics.renderItem(new ItemStack(item), getX(), getY());
            }
            else if (blitter != null)
            {
                blitter.dest(getX(), getY()).blit(guiGraphics);
            }
        }

        RenderSystem.enableDepthTest();
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
    protected Item getItemOverlay()
    {
        return null;
    }

    @Override
    public List<Component> getTooltipMessage()
    {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea()
    {
        return new Rect2i(
                getX(),
                getY(),
                this.halfSize ? 8 : 16,
                this.halfSize ? 8 : 16);
    }

    @Override
    public boolean isTooltipAreaVisible()
    {
        return this.visible;
    }

    public boolean isHalfSize()
    {
        return this.halfSize;
    }

    public void setHalfSize(boolean halfSize)
    {
        this.halfSize = halfSize;
    }

    public boolean isDisableClickSound()
    {
        return disableClickSound;
    }

    public void setDisableClickSound(boolean disableClickSound)
    {
        this.disableClickSound = disableClickSound;
    }

    public boolean isDisableBackground()
    {
        return disableBackground;
    }

    public void setDisableBackground(boolean disableBackground)
    {
        this.disableBackground = disableBackground;
    }
}
