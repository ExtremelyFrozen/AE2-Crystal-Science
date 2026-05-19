package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class FrequencyBandInfoPanel extends AbstractWidget
{
    private static final Identifier BG = AE2CrystalScience.makeId("textures/gui/frequency_band_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 219, 158, 17);
    private static final Rect2i TEXTURE_HIGHLIGHT_BOUND = new Rect2i(0, 236, 158, 17);

    private final FrequencyBandMenu menu;

    private String bandId = "";
    private boolean isPublic = false; // 虽然我感觉没什么用，但是也许可以留着
    private boolean isEncrypted = false;

    public FrequencyBandInfoPanel(int x, int y, FrequencyBandMenu menu)
    {
        super(x, y, TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), Component.empty());
        this.menu = menu;
        this.active = false;
        this.visible = false;
    }

    public void setInfo(String bandId, boolean isPublic, boolean isEncrypted)
    {
        this.bandId = bandId == null ? "" : bandId;
        this.isPublic = isPublic;
        this.isEncrypted = isEncrypted;
    }

    public String getBandId()
    {
        return bandId;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        // 背景
        Rect2i bounds;
        if (isHovered())
            bounds = TEXTURE_HIGHLIGHT_BOUND;
        else
            bounds = TEXTURE_BOUND;
        guiGraphicsExtractor.blit(BG, getX(), getY(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 256, 256);

        // 频段名
        final var font = Minecraft.getInstance().font;
        guiGraphicsExtractor.text(font, bandId, getX() + 6, getY() + 4, getTextColor(), false);

        // 加密图标
        if (isEncrypted)
        {
            int rightX = getX() + getWidth() - 18;
            AdaptedAE2Icon.LOCKED.getBlitter().dest(rightX, getY() - 1).blit(guiGraphicsExtractor);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);

        if (bandId != null && !bandId.isEmpty())
        {
            menu.sendTryConnectBand(bandId);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
    {
    }

    private int getTextColor()
    {
        if (!this.active) return 0x413f54 | Mth.ceil(this.alpha * 255.0F) << 24;
        else if (this.isHovered()) return 0x517497 | Mth.ceil(this.alpha * 255.0F) << 24;
        else return 0x878797 | Mth.ceil(this.alpha * 255.0F) << 24;
    }
}
