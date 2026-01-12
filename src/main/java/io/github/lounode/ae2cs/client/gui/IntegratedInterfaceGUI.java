package io.github.lounode.ae2cs.client.gui;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.menu.SlotSemantics;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.common.menu.IntegratedInterfaceMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IntegratedInterfaceGUI extends UpgradeableScreen<IntegratedInterfaceMenu>
{
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;
    private final ToggleButton showInPatternAccessTerminalButton;
    private final PatternProviderLockReason lockReason;

    private final List<Button> amountButtons = new ArrayList<>();

    private final AECSIconButton nextPageButton;
    private final AECSIconButton prevPageButton;

    public IntegratedInterfaceGUI(IntegratedInterfaceMenu menu, Inventory inv, Component title, ScreenStyle style)
    {
        super(menu, inv, title, style);

        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);
        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);
        lockCraftingModeButton = new ServerSettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(lockCraftingModeButton);
        this.showInPatternAccessTerminalButton = new ToggleButton(Icon.PATTERN_ACCESS_SHOW,
                Icon.PATTERN_ACCESS_HIDE,
                GuiText.PatternAccessTerminal.text(), GuiText.PatternAccessTerminalHint.text(),
                btn -> selectNextPatternProviderMode());
        this.addToLeftToolbar(this.showInPatternAccessTerminalButton);

        nextPageButton = new AECSIconButton(button -> menu.onTogglePageButton(1))
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AdaptedAE2Icon.ARROW_RIGHT;
            }
        };
        nextPageButton.setMessage(Component.translatable("ae2cs.menu.button.next_page"));
        this.addToLeftToolbar(nextPageButton);

        prevPageButton = new AECSIconButton(button -> menu.onTogglePageButton(-1))
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AdaptedAE2Icon.ARROW_LEFT;
            }
        };
        prevPageButton.setMessage(Component.translatable("ae2cs.menu.button.prev_page"));
        this.addToLeftToolbar(prevPageButton);

        this.lockReason = new PatternProviderLockReason(this);
        widgets.add("lockReason", this.lockReason);

        widgets.addOpenPriorityButton();

        List<Slot> configSlots = menu.getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < configSlots.size(); i++)
        {
            SetAmountButton button = new SetAmountButton(btn -> {
                int idx = amountButtons.indexOf(btn);
                Slot configSlot = configSlots.get(idx);
                menu.openSetAmountMenu(configSlot.getSlotIndex());
            });
            button.setDisableBackground(true);
            button.setMessage(ButtonToolTips.InterfaceSetStockAmount.text());
            widgets.add("amtButton" + (1 + i), button);
            amountButtons.add(button);
        }
    }

    private void selectNextPatternProviderMode()
    {
        final boolean backwards = isHandlingRightClick();
        ServerboundPacket message = new ConfigButtonPacket(Settings.PATTERN_ACCESS_TERMINAL, backwards);
        PacketDistributor.sendToServer(message);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.hasUpgrade(AEItems.FUZZY_CARD));
        this.lockReason.setVisible(menu.getLockCraftingMode() != LockCraftingMode.NONE);
        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.lockCraftingModeButton.set(this.menu.getLockCraftingMode());
        this.showInPatternAccessTerminalButton.setState(this.menu.getShowInAccessTerminal() == YesNo.YES);

        this.prevPageButton.setVisibility(menu.pageSize > 1 && menu.pageIndex != 0);
        this.nextPageButton.setVisibility(menu.pageSize > 1 && menu.pageIndex != menu.pageSize - 1);

        int activeStart = menu.pageIndex * 9;
        int activeEnd = activeStart + 9;
        for (int i = 0; i < amountButtons.size(); i++)
        {
            var button = amountButtons.get(i);
            button.visible = i >= activeStart && i < activeEnd;
        }
        var configSlots = menu.getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < amountButtons.size(); i++)
        {
            var button = amountButtons.get(i);
            var item = configSlots.get(i).getItem();
            button.visible = !item.isEmpty();
        }
    }

    private static class PatternProviderLockReason implements ICompositeWidget
    {
        protected boolean visible = false;
        protected int x;
        protected int y;

        private final IntegratedInterfaceGUI screen;

        public PatternProviderLockReason(IntegratedInterfaceGUI screen)
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
        public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse)
        {
            var menu = screen.getMenu();

            Icon icon;
            Component lockStatusText;
            if (menu.getCraftingLockedReason() == LockCraftingMode.NONE)
            {
                icon = Icon.UNLOCKED;
                lockStatusText = GuiText.CraftingLockIsUnlocked.text()
                        .setStyle(Style.EMPTY.withColor(Mth.color(125 / 255f, 169 / 255f, 210 / 255f)));
            }
            else
            {
                icon = Icon.LOCKED;
                lockStatusText = GuiText.CraftingLockIsLocked.text()
                        .setStyle(Style.EMPTY.withColor(Mth.color(193 / 255f, 66 / 255f, 75 / 255f)));
            }

            icon.getBlitter().dest(x, y).blit(guiGraphics);
            guiGraphics.drawString(Minecraft.getInstance().font, lockStatusText, x + 15, y + 5, -1, false);
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
                        stackName = AEKeyRendering.getDisplayName(stack.what());
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

    private static class SetAmountButton extends IconButton
    {
        public SetAmountButton(OnPress onPress)
        {
            super(onPress);
        }

        @Override
        protected Icon getIcon()
        {
            return isHoveredOrFocused() ? Icon.COG : Icon.COG_DISABLED;
        }
    }
}