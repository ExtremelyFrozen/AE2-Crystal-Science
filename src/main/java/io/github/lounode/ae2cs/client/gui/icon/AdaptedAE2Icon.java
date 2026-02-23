package io.github.lounode.ae2cs.client.gui.icon;

import appeng.client.gui.Icon;

public class AdaptedAE2Icon
{
    private AdaptedAE2Icon()
    {
    }

    public static final AE2IconAdapter REDSTONE_IGNORE = new AE2IconAdapter(Icon.REDSTONE_IGNORE);
    public static final AE2IconAdapter REDSTONE_LOW = new AE2IconAdapter(Icon.REDSTONE_LOW);
    public static final AE2IconAdapter REDSTONE_HIGH = new AE2IconAdapter(Icon.REDSTONE_HIGH);
    public static final AE2IconAdapter REDSTONE_PULSE = new AE2IconAdapter(Icon.REDSTONE_PULSE);

    public static final AE2IconAdapter OVERLAY_ON = new AE2IconAdapter(Icon.OVERLAY_ON);
    public static final AE2IconAdapter OVERLAY_OFF = new AE2IconAdapter(Icon.OVERLAY_OFF);

    public static final AE2IconAdapter LOCKED = new AE2IconAdapter(Icon.LOCKED);
    public static final AE2IconAdapter UNLOCKED = new AE2IconAdapter(Icon.UNLOCKED);

    public static final AE2IconAdapter CLEAR = new AE2IconAdapter(Icon.CLEAR);

    // TODO 验证TOOLBAR_BUTTON_BACKGROUND与新版本AE的TOOLBAR_BUTTON_BACKGROUND_FOCUS、TOOLBAR_BUTTON_BACKGROUND_HOVER之间的关系
    public static final AE2IconAdapter TOOLBAR_BUTTON_BACKGROUND = new AE2IconAdapter(Icon.TOOLBAR_BUTTON_BACKGROUND);
    public static final AE2IconAdapter TOOLBAR_BUTTON_BACKGROUND_FOCUS = new AE2IconAdapter(Icon.TOOLBAR_BUTTON_BACKGROUND);
    public static final AE2IconAdapter TOOLBAR_BUTTON_BACKGROUND_HOVER = new AE2IconAdapter(Icon.TOOLBAR_BUTTON_BACKGROUND);

    public static final AE2IconAdapter ARROW_UP = new AE2IconAdapter(Icon.ARROW_UP);
    public static final AE2IconAdapter ARROW_DOWN = new AE2IconAdapter(Icon.ARROW_DOWN);
    public static final AE2IconAdapter ARROW_RIGHT = new AE2IconAdapter(Icon.ARROW_RIGHT);
    public static final AE2IconAdapter ARROW_LEFT = new AE2IconAdapter(Icon.ARROW_LEFT);

    public static final AE2IconAdapter SCHEDULING_RANDOM = new AE2IconAdapter(Icon.SCHEDULING_RANDOM);

}
