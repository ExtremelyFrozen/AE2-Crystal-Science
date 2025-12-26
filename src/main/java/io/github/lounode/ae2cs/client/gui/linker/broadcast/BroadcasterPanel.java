package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 记录广播器类型和其位置
 */
public class BroadcasterPanel extends AbstractWidget
{
    private static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/frequency_band_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 224, 140, 16);
    private static final Component senderComponent = Component.translatable("ae2cs.widgets.broadcaster.sender");
    private static final Component receiverComponent = Component.translatable("ae2cs.widgets.broadcaster.receiver");

    private final FrequencyBandManagerMenu menu;

    private GlobalPos globalPos = null;
    private boolean isSender = false;

    public BroadcasterPanel(int x, int y, FrequencyBandManagerMenu menu)
    {
        super(x, y, TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), Component.empty());
        this.menu = menu;
        this.active = false;
        this.visible = false;
    }

    public void setInfo(GlobalPos globalPos, boolean isSender)
    {
        this.globalPos = globalPos;
        this.isSender = isSender;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pt)
    {
        guiGraphics.blit(BG, getX(), getY(), TEXTURE_BOUND.getX(), TEXTURE_BOUND.getY(), TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), 256, 256);

        final var font = Minecraft.getInstance().font;

        if (isSender)
            guiGraphics.drawString(font, senderComponent, getX() + 2, getY() + 4, 0xFFFFFF, false);
        else
            guiGraphics.drawString(font, receiverComponent, getX() + 2, getY() + 4, 0xFFFFFF, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button)
    {
        super.onClick(mouseX, mouseY, button);

        if (globalPos != null)
            menu.sendTapToBroadcasterAction(globalPos);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
    {
    }
}
