package io.github.lounode.ae2cs.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiRenderHelper
{
    /**
     * 在水平区间 [xA, xB] 的中央绘制一段文本。
     *
     * @param guiGraphics 渲染上下文
     * @param font        字体
     * @param text        要绘制的文字
     * @param xA          区域端点 A（左/右都可，内部会自动取 min/max）
     * @param xB          区域端点 B（左/右都可）
     * @param y           文本左上角的 y 坐标
     * @param color       颜色（0xAARRGGBB）
     * @param dropShadow  是否绘制阴影
     */
    public static void drawCenteredInRegion(GuiGraphics guiGraphics,
                                            Font font,
                                            Component text,
                                            int xA, int xB,
                                            int y,
                                            int color,
                                            boolean dropShadow)
    {
        // 1) 规范化左右边界
        int left = Math.min(xA, xB);
        int right = Math.max(xA, xB);

        // 2) 计算文本宽与区域宽
        int textWidth = font.width(text);
        int regionWidth = right - left;

        // 3) 居中起点
        int xStart = left + (regionWidth - textWidth) / 2;

        // 4) 绘制
        guiGraphics.drawString(font, text, xStart, y, color, dropShadow);
    }

    public static void drawRightAlignedAtX(GuiGraphics guiGraphics,
                                           Font font,
                                           Component text,
                                           int xRight,
                                           int y,
                                           int color,
                                           boolean dropShadow)
    {
        int xStart = xRight - font.width(text);
        guiGraphics.drawString(font, text, xStart, y, color, dropShadow);
    }
}
