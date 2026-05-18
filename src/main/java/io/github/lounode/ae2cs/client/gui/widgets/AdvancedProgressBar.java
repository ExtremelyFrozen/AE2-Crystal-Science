package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.GuiText;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基于AE的ProgressBar上更改而来，支持更多不同的进度条填充模式
 */
public class AdvancedProgressBar extends AbstractWidget implements ITooltip
{
    private final IProgressProvider source;
    private final Blitter blitter;
    private final FillMode layout;
    private final Rect2i sourceRect;
    private final Component titleName;
    private Component fullMsg;

    public AdvancedProgressBar(IProgressProvider source, Blitter blitter, FillMode dir)
    {
        this(source, blitter, dir, null);
    }

    public AdvancedProgressBar(IProgressProvider source, Blitter blitter, FillMode dir, Component title)
    {
        super(0, 0, blitter.getSrcWidth(), blitter.getSrcHeight(), Component.empty());
        this.source = source;
        this.blitter = blitter.copy();
        this.layout = dir;
        this.titleName = title;
        this.sourceRect = new Rect2i(
                blitter.getSrcX(),
                blitter.getSrcY(),
                blitter.getSrcWidth(),
                blitter.getSrcHeight());
    }

    @Override
    public void renderWidget(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible) return;

        int max = this.source.getMaxProgress();
        int current = this.source.getCurrentProgress();

        if (max <= 0) return;
        if (current < 0) current = 0;
        if (current > max) current = max;

        // 源贴图区域（UV）
        int baseSrcX = sourceRect.getX();
        int baseSrcY = sourceRect.getY();
        int baseSrcW = sourceRect.getWidth();
        int baseSrcH = sourceRect.getHeight();

        // 目标位置
        int baseDestX = getX();
        int baseDestY = getY();

        // 进度比例（用于“填充”类模式）
        int shownW = baseSrcW * current / max;
        int shownH = baseSrcH * current / max;

        switch (this.layout)
        {
            // 线性填充
            case LEFT_TO_RIGHT ->
            {
                if (shownW <= 0) return;
                blitSub(GuiGraphicsExtractor,
                        baseSrcX, baseSrcY, shownW, baseSrcH,
                        baseDestX, baseDestY);
            }
            case RIGHT_TO_LEFT ->
            {
                if (shownW <= 0) return;
                int diff = baseSrcW - shownW;
                blitSub(GuiGraphicsExtractor,
                        baseSrcX + diff, baseSrcY, shownW, baseSrcH,
                        baseDestX, baseDestY);
            }
            case TOP_TO_BOTTOM ->
            {
                if (shownH <= 0) return;
                blitSub(GuiGraphicsExtractor,
                        baseSrcX, baseSrcY, baseSrcW, shownH,
                        baseDestX, baseDestY);
            }
            case BOTTOM_TO_TOP ->
            {
                if (shownH <= 0) return;
                int diff = baseSrcH - shownH;
                blitSub(GuiGraphicsExtractor,
                        baseSrcX, baseSrcY + diff, baseSrcW, shownH,
                        baseDestX, baseDestY + diff);
            }

            // 向中间闭合式（两端同时向中间增长）
            case TOP_BOTTOM_TO_CENTER ->
            {
                if (shownH <= 0) return;

                int topH = shownH / 2;
                int bottomH = shownH - topH;

                // 顶部：从最上面取 topH
                if (topH > 0)
                {
                    blitSub(GuiGraphicsExtractor,
                            baseSrcX, baseSrcY, baseSrcW, topH,
                            baseDestX, baseDestY);
                }

                // 底部：从最下面取 bottomH，贴在底部
                if (bottomH > 0)
                {
                    int diff = baseSrcH - bottomH;
                    blitSub(GuiGraphicsExtractor,
                            baseSrcX, baseSrcY + diff, baseSrcW, bottomH,
                            baseDestX, baseDestY + diff);
                }
            }
            case LEFT_RIGHT_TO_CENTER ->
            {
                if (shownW <= 0) return;

                int leftW = shownW / 2;
                int rightW = shownW - leftW;

                // 左侧：从最左取 leftW
                if (leftW > 0)
                {
                    blitSub(GuiGraphicsExtractor,
                            baseSrcX, baseSrcY, leftW, baseSrcH,
                            baseDestX, baseDestY);
                }

                // 右侧：从最右取 rightW，贴在右侧
                if (rightW > 0)
                {
                    int diff = baseSrcW - rightW;
                    blitSub(GuiGraphicsExtractor,
                            baseSrcX + diff, baseSrcY, rightW, baseSrcH,
                            baseDestX + diff, baseDestY);
                }
            }

            // 顺/逆时针闭合（current/max 表示闭合程度，越大越闭合）
            case CLOCKWISE_CLOSE, COUNTERCLOCKWISE_CLOSE ->
            {
                double close = (double) current / (double) max; // 0=open, 1=closed
                if (close <= 0.0)
                {
                    blitSub(GuiGraphicsExtractor, baseSrcX, baseSrcY, baseSrcW, baseSrcH, baseDestX, baseDestY);
                    return;
                }
                if (close >= 1.0)
                {
                    return; // 完全闭合
                }

                // 每一边最大覆盖的距离：到一半时就会在中心闭合
                int maxTop = baseSrcH / 2;
                int maxBottom = baseSrcH - maxTop;
                int maxLeft = baseSrcW / 2;
                int maxRight = baseSrcW - maxLeft;

                // 把 close 分成 4 段，每段负责一条边（顺时针/逆时针顺序不同）
                int top = 0, right = 0, bottom = 0, left = 0;

                if (this.layout == FillMode.CLOCKWISE_CLOSE)
                {
                    // 上 -> 右 -> 下 -> 左
                    top = portion(close, 0.00, 0.25, maxTop);
                    right = portion(close, 0.25, 0.50, maxRight);
                    bottom = portion(close, 0.50, 0.75, maxBottom);
                    left = portion(close, 0.75, 1.00, maxLeft);
                }
                else
                {
                    // 逆时针：上 -> 左 -> 下 -> 右
                    top = portion(close, 0.00, 0.25, maxTop);
                    left = portion(close, 0.25, 0.50, maxLeft);
                    bottom = portion(close, 0.50, 0.75, maxBottom);
                    right = portion(close, 0.75, 1.00, maxRight);
                }

                int srcX = baseSrcX + left;
                int srcY = baseSrcY + top;
                int srcW = baseSrcW - left - right;
                int srcH = baseSrcH - top - bottom;

                if (srcW <= 0 || srcH <= 0) return;

                int destX = baseDestX + left;
                int destY = baseDestY + top;

                blitSub(GuiGraphicsExtractor, srcX, srcY, srcW, srcH, destX, destY);
            }
        }
    }

    /**
     * 把 [start,end] 段的 close 映射到 [0,maxVal] 的整数值。
     */
    private static int portion(double close, double start, double end, int maxVal)
    {
        if (close <= start) return 0;
        if (close >= end) return maxVal;
        double t = (close - start) / (end - start);
        return (int) Math.round(maxVal * t);
    }

    private void blitSub(GuiGraphicsExtractor g,
                         int srcX, int srcY, int srcW, int srcH,
                         int destX, int destY)
    {
        if (srcW <= 0 || srcH <= 0) return;
        blitter.src(srcX, srcY, srcW, srcH).dest(destX, destY).blit(g);
    }

    public void setFullMsg(Component msg)
    {
        this.fullMsg = msg;
    }

    @Override
    public List<Component> getTooltipMessage()
    {
        if (this.fullMsg != null)
        {
            return Collections.singletonList(this.fullMsg);
        }

        Component result = this.titleName != null ? this.titleName : Component.empty();
        return Arrays.asList(
                result,
                Component.literal(this.source.getCurrentProgress() + " ")
                        .append(GuiText.Of.text().copy().append(" " + this.source.getMaxProgress())));
    }

    @Override
    public Rect2i getTooltipArea()
    {
        return new Rect2i(getX() - 2, getY() - 2, width + 4, height + 4);
    }

    @Override
    public boolean isTooltipAreaVisible()
    {
        return true;
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output)
    {
    }

    public enum FillMode
    {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,

        TOP_BOTTOM_TO_CENTER,
        LEFT_RIGHT_TO_CENTER,

        CLOCKWISE_CLOSE,
        COUNTERCLOCKWISE_CLOSE
    }
}