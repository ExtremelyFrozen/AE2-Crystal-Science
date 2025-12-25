package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandMenu;
import io.github.lounode.ae2cs.util.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FrequencyBandInfoPanel extends AbstractWidget
{
    private static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/frequency_band_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 224, 140, 16);

    private final FrequencyBandMenu menu;

    private String bandId = "";
    private boolean isPublic = false;
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
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pt)
    {
        guiGraphics.blit(BG, getX(), getY(), TEXTURE_BOUND.getX(), TEXTURE_BOUND.getY(), TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), 256, 256);

        final var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, bandId, getX() + 6, getY() + 4, 0xFFFFFF, false);

        int rightX = getX() + getWidth() - 5;

        if (isEncrypted)
        {
            GuiRenderHelper.drawRightAlignedAtX(
                    guiGraphics, font,
                    Component.translatable("ae2cs.menu.widgets.frequency_info_panel.encrypted"),
                    rightX, getY() + 4, 0xFFFFFF, false
            );
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button)
    {
        super.onClick(mouseX, mouseY, button);

        if (bandId != null && !bandId.isEmpty())
        {
            menu.sendTryConnectBand(bandId);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
    {
    }
}
