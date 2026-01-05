package io.github.lounode.ae2cs.client.gui.icon;

import appeng.client.gui.style.Blitter;

/**
 * 用于提供按钮图标信息，以描述按钮如何渲染
 */
@FunctionalInterface
public interface IButtonIcon
{
    Blitter getBlitter();
}
