package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderEmitterFrequencyBandMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class EnderEmitterFrequencyBandInfoPanel extends AbstractWidget
{
    private static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/frequency_band_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 219, 158, 17);
    private static final Rect2i TEXTURE_HIGHLIGHT_BOUND = new Rect2i(0, 236, 158, 17);

    private final EnderEmitterFrequencyBandMenu menu;

    private String bandId = "";
    private boolean isEncrypted = false;

    public EnderEmitterFrequencyBandInfoPanel(int x, int y, EnderEmitterFrequencyBandMenu menu)
    {
        super(x, y, TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), Component.empty());
        this.menu = menu;
        this.active = false;
        this.visible = false;
    }

    public void setInfo(String bandId, boolean isEncrypted)
    {
        this.bandId = bandId == null ? "" : bandId;
        this.isEncrypted = isEncrypted;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pt)
    {
        Rect2i bounds = isHovered() ? TEXTURE_HIGHLIGHT_BOUND : TEXTURE_BOUND;
        guiGraphics.blit(BG, getX(), getY(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 256, 256);

        final var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, bandId, getX() + 6, getY() + 4, getTextColor(), false);

        if (isEncrypted)
        {
            int rightX = getX() + getWidth() - 18;
            AdaptedAE2Icon.LOCKED.getBlitter().dest(rightX, getY() - 1).blit(guiGraphics);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        if (!bandId.isEmpty())
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
