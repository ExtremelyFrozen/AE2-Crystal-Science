package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.api.config.LockCraftingMode;
import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.api.AEKeyRenderer;
import appeng.client.api.AEKeyRendering;
import appeng.client.gui.ICompositeWidget;
import appeng.util.Icon;
import appeng.client.gui.Tooltip;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import io.github.lounode.ae2cs.client.gui.UpgradeablePatternProviderGUI;
import io.github.lounode.ae2cs.client.gui.icon.AE2IconAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class UpgradeablePatternProviderLockReason implements ICompositeWidget
{
    protected boolean visible = false;
    protected int x;
    protected int y;

    private final UpgradeablePatternProviderGUI<?> screen;

    public UpgradeablePatternProviderLockReason(UpgradeablePatternProviderGUI<?> screen)
    {
        this.screen = screen;
    }

    @Override
    public void setPosition(Point position)
    {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height)
    {
    }

    @Override
    public Rect2i getBounds()
    {
        return new Rect2i(x, y, 126, 16);
    }

    @Override
    public final boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor guiGraphics, Rect2i bounds, Point mouse)
    {
        var menu = screen.getMenu();

        AE2IconAdapter icon;
        Component lockStatusText;
        if (menu.getCraftingLockedReason() == LockCraftingMode.NONE)
        {
            icon = new AE2IconAdapter (Icon.UNLOCKED);
            lockStatusText = GuiText.CraftingLockIsUnlocked.text()
                    .setStyle(Style.EMPTY.withColor(ARGB.color(255,125 , 169, 210)));
        }
        else
        {
            icon = new AE2IconAdapter (Icon.LOCKED);
            lockStatusText = GuiText.CraftingLockIsLocked.text()
                    .setStyle(Style.EMPTY.withColor(ARGB.color(255,193 , 66, 75)));
        }

        icon.getBlitter().dest(x, y).blit(guiGraphics);
        guiGraphics.text(Minecraft.getInstance().font, lockStatusText, x + 15, y + 5, -1, false);
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY)
    {
        var menu = screen.getMenu();
        var tooltip = switch (menu.getCraftingLockedReason())
        {
            case NONE -> null;
            case LOCK_UNTIL_PULSE -> InGameTooltip.CraftingLockedUntilPulse.text();
            case LOCK_WHILE_HIGH -> InGameTooltip.CraftingLockedByRedstoneSignal.text();
            case LOCK_WHILE_LOW -> InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
            case LOCK_UNTIL_RESULT ->
            {
                var stack = menu.getUnlockStack();
                Component stackName;
                Component stackAmount;
                if (stack != null)
                {
                    stackName = stack.what().getDisplayName();
                    stackAmount = Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                }
                else
                {
                    stackName = Component.literal("ERROR");
                    stackAmount = Component.literal("ERROR");

                }
                yield InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        };

        return tooltip != null ? new Tooltip(tooltip) : null;
    }
}
