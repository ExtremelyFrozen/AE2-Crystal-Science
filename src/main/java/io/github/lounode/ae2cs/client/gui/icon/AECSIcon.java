package io.github.lounode.ae2cs.client.gui.icon;

import appeng.client.gui.style.Blitter;
import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.resources.ResourceLocation;

public enum AECSIcon implements IButtonIcon
{
    PULL_MODE_ON(0, 0),
    PULL_MODE_OFF(16, 0),

    ENTROPY_INCREASE(0, 16),
    ENTROPY_DECREASE(16, 16),

    WHITE_LIST_MODE(0, 32),
    BLACK_LIST_MODE(16, 32),

    SUBTRACTION_SIGN(0, 48),
    ADDITION_SIGN(16, 48),

    BREAK_ALL_LINKS(0, 64),
    LINK_TO_ALL(16, 64),

    AUTO_LINK_CABLE_DISABLE(0, 80),
    AUTO_LINK_CABLE_ENABLE(16, 80),

    AUTO_LINK_DISABLE(0, 96),
    AUTO_LINK_ENABLE(16, 96),

    RECEIVER_STATE(0, 112),
    SENDER_STATE(16, 112),

    BAND_VIEW(0, 128),
    BAND_CREATE(16, 128),
    BAND_MANAGER(0, 144),

    ALLOW_MEMORY_CARD(0, 160),
    DENY_MEMORY_CARD(16, 160),

    SIDE_CONFIG(0, 176),

    BUTTON_PURPLE_LIGHT(16, 196, 18, 20),
    BUTTON_PURPLE_DARK(16, 216, 18, 20),
    BUTTON_PURPLE_COMMON(16, 236, 18, 20),
    BUTTON_PURPLE_LIGHT_HOVER(88, 196, 18, 20),
    BUTTON_PURPLE_DARK_HOVER(88, 216, 18, 20),
    BUTTON_PURPLE_COMMON_HOVER(88, 236, 18, 20),

    BUTTON_BLUE_LIGHT(34, 196, 18, 20),
    BUTTON_BLUE_DARK(34, 216, 18, 20),
    BUTTON_BLUE_COMMON(34, 236, 18, 20),
    BUTTON_BLUE_LIGHT_HOVER(106, 196, 18, 20),
    BUTTON_BLUE_DARK_HOVER(106, 216, 18, 20),
    BUTTON_BLUE_COMMON_HOVER(106, 236, 18, 20),

    BUTTON_RED_LIGHT(52, 196, 18, 20),
    BUTTON_RED_DARK(52, 216, 18, 20),
    BUTTON_RED_COMMON(52, 236, 18, 20),
    BUTTON_RED_LIGHT_HOVER(124, 196, 18, 20),
    BUTTON_RED_DARK_HOVER(124, 216, 18, 20),
    BUTTON_RED_COMMON_HOVER(124, 236, 18, 20),

    BUTTON_ORIGINAL_LIGHT(70, 196, 18, 20),
    BUTTON_ORIGINAL_DARK(70, 216, 18, 20),
    BUTTON_ORIGINAL_COMMON(70, 236, 18, 20),
    BUTTON_ORIGINAL_LIGHT_HOVER(142, 196, 18, 20),
    BUTTON_ORIGINAL_DARK_HOVER(142, 216, 18, 20),
    BUTTON_ORIGINAL_COMMON_HOVER(142, 236, 18, 20);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    AECSIcon(int x, int y)
    {
        this(x, y, 16, 16);
    }

    AECSIcon(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static final ResourceLocation TEXTURE = AE2CrystalScience.makeId("textures/gui/icons.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    @Override
    public Blitter getBlitter()
    {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
                .src(x, y, width, height);
    }
}
