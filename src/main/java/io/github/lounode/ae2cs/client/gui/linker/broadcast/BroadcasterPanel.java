package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

/**
 * 记录广播器类型和其位置
 */
public class BroadcasterPanel extends AbstractWidget {

    private static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/frequency_band_manager_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 226, 158, 17);
    private static final Rect2i TEXTURE_HIGHLIGHT_BOUND = new Rect2i(0, 243, 158, 17);
    private static final Rect2i DELETE_AREA_BOUND = new Rect2i(142, 0, 16, 17);
    private static final Component senderComponent = Component.translatable("ae2cs.widgets.broadcaster.sender");
    private static final Component receiverComponent = Component.translatable("ae2cs.widgets.broadcaster.receiver");

    private final FrequencyBandManagerMenu menu;

    private GlobalPos globalPos = null;
    private boolean isSender = false;

    public BroadcasterPanel(int x, int y, FrequencyBandManagerMenu menu) {
        super(x, y, TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), Component.empty());
        this.menu = menu;
        this.active = false;
        this.visible = false;
    }

    public void setInfo(GlobalPos globalPos, boolean isSender) {
        this.globalPos = globalPos;
        this.isSender = isSender;
        if (globalPos != null)
            this.setTooltip(
                    Tooltip.create(Component.translatable("ae2cs.widgets.broadcaster.pos",
                            Component.translatable(globalPos.dimension().location().toLanguageKey("dimension")), globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ())));
        else
            this.setTooltip(Tooltip.create(Component.empty()));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pt) {
        Rect2i bounds;
        if (isHovered())
            bounds = TEXTURE_HIGHLIGHT_BOUND;
        else
            bounds = TEXTURE_BOUND;
        guiGraphics.blit(BG, getX(), getY(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 512, 512);

        AdaptedAE2Icon.CLEAR.getBlitter().dest(getX() + DELETE_AREA_BOUND.getX(), getY() + DELETE_AREA_BOUND.getY()).blit(guiGraphics);

        final var font = Minecraft.getInstance().font;

        if (isSender)
            guiGraphics.drawString(font, senderComponent, getX() + 2, getY() + 4, getTextColor(), false);
        else
            guiGraphics.drawString(font, receiverComponent, getX() + 2, getY() + 4, getTextColor(), false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);

        if (globalPos != null) {
            if (DELETE_AREA_BOUND.contains((int) mouseX - getX(), (int) mouseY - getY()))
                menu.sendDisconnectBroadcasterAction(globalPos);
            else
                menu.sendTapToBroadcasterAction(globalPos);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    private int getTextColor() {
        if (!this.active) return 0x413f54 | Mth.ceil(this.alpha * 255.0F) << 24;
        else if (this.isHovered()) return 0x517497 | Mth.ceil(this.alpha * 255.0F) << 24;
        else return 0x878797 | Mth.ceil(this.alpha * 255.0F) << 24;
    }
}
