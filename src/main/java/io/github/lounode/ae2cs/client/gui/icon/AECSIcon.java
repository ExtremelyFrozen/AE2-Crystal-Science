package io.github.lounode.ae2cs.client.gui.icon;

import appeng.client.gui.style.Blitter;
import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.resources.ResourceLocation;

public enum AECSIcon implements IButtonIcon
{
    PULL_MODE_ON(0, 0),
    PULL_MODE_OFF(16, 0),

    ENTROPY_INCREASE(0, 16),
    ENTROPY_DECREASE(16, 16);


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
