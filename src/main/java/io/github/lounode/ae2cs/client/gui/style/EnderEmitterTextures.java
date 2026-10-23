package io.github.lounode.ae2cs.client.gui.style;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AE2IconAdapter;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

/** Artwork used by the emitter; the supplied background and range atlas remain unmodified. */
public final class EnderEmitterTextures {

    private static final ResourceLocation WIDGETS = AE2CrystalScience.makeId("textures/gui/ender_emitter/widgets.png");
    public static final ResourceLocation RANGE = AE2CrystalScience.makeId("textures/gui/ender_emitter/range.png");
    public static final Scrollbar.Style BAND_SCROLLBAR = Scrollbar.Style.create(
            AE2CrystalScience.makeId("ender_emitter/scrollbar"),
            AE2CrystalScience.makeId("ender_emitter/scrollbar_disabled"));

    private EnderEmitterTextures() {}

    @Nullable
    public static IButtonIcon icon(@Nullable IButtonIcon original) {
        if (original instanceof AECSIcon icon) {
            return () -> widget(icon.x, icon.y, icon.width, icon.height);
        }
        if (original instanceof AE2IconAdapter adapter) {
            var icon = adapter.icon();
            return () -> widget(256 + icon.x, icon.y, icon.width, icon.height);
        }
        return original;
    }

    public static Blitter widget(int x, int y, int width, int height) {
        return Blitter.texture(WIDGETS, 512, 512).src(x, y, width, height);
    }

    /** Preserve the bevel and clipped corner of NeoECOAE's 34-by-11 card component. */
    public static void drawCard(GuiGraphics graphics, int x, int y, int width, int height) {
        int[] sourceX = { 0, 2, 30 };
        int[] sourceY = { 256, 258, 263 };
        int[] sourceWidths = { 2, 28, 4 };
        int[] sourceHeights = { 2, 5, 4 };
        int[] destinationWidths = { 2, width - 6, 4 };
        int[] destinationHeights = { 2, height - 6, 4 };
        int destinationY = y;
        for (int row = 0; row < 3; row++) {
            int destinationX = x;
            for (int column = 0; column < 3; column++) {
                widget(sourceX[column], sourceY[row], sourceWidths[column], sourceHeights[row])
                        .dest(destinationX, destinationY, destinationWidths[column], destinationHeights[row]).blit(graphics);
                destinationX += destinationWidths[column];
            }
            destinationY += destinationHeights[row];
        }
    }

    /** Stretch only the centre of the four-pixel bar, retaining both one-pixel end caps. */
    public static void drawBar(GuiGraphics graphics, int x, int y, int width, boolean pink) {
        int sourceY = pink ? 264 : 256;
        widget(40, sourceY, 1, 4).dest(x, y).blit(graphics);
        widget(41, sourceY, 27, 4).dest(x + 1, y, width - 2, 4).blit(graphics);
        widget(68, sourceY, 1, 4).dest(x + width - 1, y).blit(graphics);
    }
}
