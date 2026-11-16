package io.github.lounode.ae2cs.client.gui.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AdjustmentButtons {

    private AdjustmentButtons() {}

    public static int step(int baseStep, int current) {
        if (Screen.hasControlDown()) {
            return baseStep > 0 ? Math.max(1, current) : -Math.max(1, current / 2 + current % 2);
        }
        return Screen.hasShiftDown() ? baseStep * 5 : baseStep;
    }

    public static Tooltip tooltip() {
        return Tooltip.create(Component.translatable("ae2cs.menu.adjustment_shortcuts"));
    }
}
