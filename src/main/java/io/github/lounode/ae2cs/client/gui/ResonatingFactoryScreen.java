package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.common.menu.ResonatingFactoryMenu;
import io.github.lounode.ae2cs.common.menu.slot.ResonatingStackingSlot;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.util.ReadableNumberConverter;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Factory screens with compact counts for enlarged machine stacks. */
public class ResonatingFactoryScreen<T extends ResonatingFactoryMenu<?>> extends UpgradeableScreen<T> {

    protected ResonatingFactoryScreen(T menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, String countString) {
        if (!(slot instanceof ResonatingStackingSlot) || countString != null || stack.getCount() < 100) {
            super.renderSlotContents(graphics, stack, slot, countString);
            return;
        }

        String label = ReadableNumberConverter.format(stack.getCount(), 3).replace('K', 'k');
        super.renderSlotContents(graphics, stack, slot, "");
        int width = font.width(label);
        float scale = Math.min(1f, 15f / (width + 1));
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(slot.x + 16 - width * scale, slot.y + 16 - font.lineHeight * scale, 200);
        pose.scale(scale, scale, 1);
        graphics.drawString(font, label, 0, 0, 0xFFFFFF, true);
        pose.popPose();
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var tooltip = super.getTooltipFromContainerItem(stack);
        if (!(hoveredSlot instanceof ResonatingStackingSlot) || stack.getCount() < 100) return tooltip;
        var lines = new ArrayList<>(tooltip);
        lines.add(Component.translatable("ae2cs.gui.resonating_factory.stack_count", stack.getCount())
                .withStyle(ChatFormatting.GRAY));
        return lines;
    }
}
