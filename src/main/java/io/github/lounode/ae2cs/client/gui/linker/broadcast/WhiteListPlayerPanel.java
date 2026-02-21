package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.BandWhiteListManagerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 显示玩家信息，并用于切换白名单模式
 */
public class WhiteListPlayerPanel extends AbstractWidget
{
    private static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/band_white_list_manager_menu.png");
    private static final Rect2i TEXTURE_BOUND = new Rect2i(0, 219, 158, 17);
    private static final Rect2i TEXTURE_HIGHLIGHT_BOUND = new Rect2i(0, 236, 158, 17);

    private final BandWhiteListManagerMenu menu;

    private UUID playerId = UUID.randomUUID();
    private String playerName = "";

    public WhiteListPlayerPanel(int x, int y, BandWhiteListManagerMenu menu)
    {
        super(x, y, TEXTURE_BOUND.getWidth(), TEXTURE_BOUND.getHeight(), Component.empty());
        this.menu = menu;
        this.active = false;
        this.visible = false;
    }

    public void setInfo(UUID playerId, String playerName)
    {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pt)
    {
        Rect2i bounds;
        if (isHovered())
            bounds = TEXTURE_HIGHLIGHT_BOUND;
        else
            bounds = TEXTURE_BOUND;
        guiGraphics.blit(BG, getX(), getY(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 512, 512);

        final var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, playerName, getX() + 2, getY() + 4, getTextColor(), false);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button)
    {
        super.onClick(mouseX, mouseY, button);
        menu.sendChangeWhiteListStateAction(playerId);
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
