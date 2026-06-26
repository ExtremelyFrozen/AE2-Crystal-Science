package io.github.lounode.ae2cs.client.gui.icon;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;

/**
 * 使我们能在IButtonIcon接口下快速复用AE2原生Icon
 */
public record AE2IconAdapter(Icon icon) implements IButtonIcon {

    @Override
    public Blitter getBlitter() {
        return icon.getBlitter();
    }
}
