package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.style.EnderEmitterTextures;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.EnderEmitterMenu;

import appeng.api.config.Setting;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ReadableNumberConverter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnderEmitterGUI extends AEBaseScreen<EnderEmitterMenu> {

    private static final int VISIBLE_BANDS = 6;
    private static final int TEXT_X = 100;
    private static final int TEXT_WIDTH = 141;
    private static final int LINE_SPACING = 17;
    private static final int SUMMARY_Y = 22;
    private static final int METRIC_VALUE_X = 162;
    private static final int METRIC_VALUE_WIDTH = 69;
    private static final int CHANNELS_Y = 77;
    private static final int CHANNEL_ROW_SPACING = 14;
    private static final int CHANNEL_BUTTON_Y = 101;
    private static final int CHANNEL_BUTTON_HEIGHT = 17;
    private static final int CHANNEL_VALUE_X = 154;
    private static final int CHANNEL_VALUE_WIDTH = 33;
    private static final int MAP_X = 198;
    private static final int MAP_Y = 142;
    private static final int MAP_SIZE = 43;
    private static final int TEXT_COLOR = 0x413f54;
    private static final int BAND_VALUE_COLOR = 0x285ead;
    private static final int LINKED_VALUE_COLOR = 0x9a4382;
    private static final int TOTAL_VALUE_COLOR = 0x29775d;
    private static final int SEPARATOR_COLOR = 0x6b697f;

    private final Scrollbar bandScrollbar;
    private final List<Button> bandRows = new ArrayList<>();
    private final List<Button> channelButtons = new ArrayList<>();
    private final AECSIconButton addDistanceButton;
    private final AECSIconButton reduceDistanceButton;
    private final AECSServerSettingToggleButton<AutoLinkMode> autoModeButton;
    private final AECSServerSettingToggleButton<AutoLinkCableMode> autoLinkCableButton;
    private final AECSServerSettingToggleButton<ShowRangeMode> showRangeModeButton;
    private final AECSIconButton cleanBandConnectionButton;

    public EnderEmitterGUI(EnderEmitterMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
        if (menu.getToolbox().isPresent()) {
            widgets.add("toolbox", new ToolboxPanel(style, menu.getToolbox().getName()));
        }
        for (var slot : menu.getSlots(SlotSemantics.UPGRADE)) {
            if (slot instanceof AppEngSlot upgradeSlot) {
                // The supplied background already contains the empty-slot artwork.
                upgradeSlot.setIcon(null);
            }
        }

        bandScrollbar = widgets.addScrollBar("band_scrollbar", EnderEmitterTextures.BAND_SCROLLBAR);
        bandScrollbar.setCaptureMouseWheel(false);
        bandScrollbar.setRange(0, 0, 1);
        for (int row = 0; row < VISIBLE_BANDS; row++) {
            final int visibleRow = row;
            Button button = new AE2Button(Component.empty(), pressed -> {
                BroadcastBandsField.Entry entry = getBandAt(visibleRow);
                if (entry != null) menu.sendSelectBand(entry.name());
            }) {

                @Override
                protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    BroadcastBandsField.Entry entry = getBandAt(visibleRow);
                    if (entry == null) return;
                    boolean selected = entry.name().equals(menu.bandName);
                    int textureY = selected || isHovered() || isFocused() ? 240 : 223;
                    Blitter.texture(EnderEmitterTextures.RANGE).src(0, textureY, 69, 16).dest(getX(), getY()).blit(graphics);
                    int padding = entry.isEncrypted() ? 20 : 8;
                    String name = fitText(Component.literal(entry.name()), getWidth() - padding);
                    graphics.drawString(font, name, getX() + 4, getY() + (getHeight() - font.lineHeight) / 2,
                            selected ? TEXT_COLOR : 0x626174, false);
                    if (entry.isEncrypted()) {
                        EnderEmitterTextures.icon(AdaptedAE2Icon.LOCKED).getBlitter()
                                .dest(getX() + getWidth() - 14, getY() + (getHeight() - 10) / 2, 10, 10).blit(graphics);
                    }
                }
            };
            widgets.add("band_row_" + row, button);
            bandRows.add(button);
        }

        addDistanceButton = iconButton(AECSIcon.ADDITION_SIGN, "add_distance", pressed -> menu.sendChangeDistance(1));
        reduceDistanceButton = iconButton(AECSIcon.SUBTRACTION_SIGN, "reduce_distance", pressed -> menu.sendChangeDistance(-1));
        widgets.add("add_distance_button", addDistanceButton);
        widgets.add("reduce_distance_button", reduceDistanceButton);

        showRangeModeButton = settingButton(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE);
        autoModeButton = settingButton(AECSSettings.AUTO_LINK_MODE, AutoLinkMode.ENABLE);
        autoLinkCableButton = settingButton(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.ENABLE);
        addToLeftToolbar(showRangeModeButton);
        addToLeftToolbar(autoModeButton);
        addToLeftToolbar(autoLinkCableButton);
        addToLeftToolbar(iconButton(AECSIcon.LINK_TO_ALL, "try_sacn_all", pressed -> menu.sendSacnAll()));
        addToLeftToolbar(iconButton(AECSIcon.BREAK_ALL_LINKS, "destroy_all", pressed -> menu.sendDestroyAll()));
        addToLeftToolbar(iconButton(AECSIcon.BAND_VIEW, "open_frequency_view_menu", pressed -> menu.sendOpenBandMenu()));
        cleanBandConnectionButton = iconButton(AdaptedAE2Icon.CLEAR, "clean_band_connection", pressed -> menu.sendCleanBandConnection());
        addToLeftToolbar(cleanBandConnectionButton);

        for (int delta : new int[] { -10, -1, 1, 10 }) {
            Button button = new AE2Button(Component.literal(delta > 0 ? "+" + delta : String.valueOf(delta)),
                    pressed -> menu.sendChangeExpectedChannels(delta));
            widgets.add("channels_" + delta, button);
            channelButtons.add(button);
        }
    }

    private AECSIconButton iconButton(IButtonIcon icon, String tooltip, Button.OnPress onPress) {
        AECSIconButton button = new AECSIconButton(onPress) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return EnderEmitterTextures.icon(icon);
            }
        };
        button.setMessage(Component.translatable("ae2cs.menu.ender_emitter.button." + tooltip));
        return button;
    }

    private <T extends Enum<T>> AECSServerSettingToggleButton<T> settingButton(Setting<T> setting, T value) {
        return new AECSServerSettingToggleButton<>(setting, value) {

            @Override
            protected IButtonIcon getIcon() {
                return EnderEmitterTextures.icon(super.getIcon());
            }
        };
    }

    private BroadcastBandsField.Entry getBandAt(int row) {
        int index = bandScrollbar.getCurrentScroll() + row;
        List<BroadcastBandsField.Entry> bands = menu.bandsInfo.bands();
        return index >= 0 && index < bands.size() ? bands.get(index) : null;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        autoModeButton.set(menu.autoMode);
        autoLinkCableButton.set(menu.autoLinkCableMode);
        showRangeModeButton.set(menu.showRangeMode);
        cleanBandConnectionButton.setVisibility(!menu.bandName.isEmpty());
        addDistanceButton.active = menu.linkDistance < menu.maxLinkDistance;
        reduceDistanceButton.active = menu.linkDistance > 0;
        bandScrollbar.setRange(0, Math.max(0, menu.bandsInfo.bands().size() - VISIBLE_BANDS), 1);
        for (int row = 0; row < bandRows.size(); row++) {
            BroadcastBandsField.Entry entry = getBandAt(row);
            Button button = bandRows.get(row);
            button.visible = entry != null;
            button.active = entry != null;
            Component name = entry == null ? Component.empty() : Component.literal(entry.name());
            if (!name.equals(button.getMessage())) {
                button.setMessage(name);
                button.setTooltip(entry == null ? null : Tooltip.create(name));
            }
        }
        for (int i = 0; i < channelButtons.size(); i++) {
            boolean canAdjust = !menu.bandName.isEmpty() && (i < 2 ? menu.expectedChannels > 0 : menu.expectedChannels < Integer.MAX_VALUE);
            channelButtons.get(i).active = canAdjust;
        }
    }

    @Override
    public void drawBG(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        super.drawBG(graphics, x, y, mouseX, mouseY, partialTick);
        int tier = Math.max(0, Math.min(2, menu.maxLinkDistance - 1));
        // Each 44-pixel atlas cell has a transparent top/left margin around its 43-pixel map.
        Blitter.texture(EnderEmitterTextures.RANGE).src(tier * 44 + 1, 1, MAP_SIZE, MAP_SIZE)
                .dest(x + MAP_X, y + MAP_Y).blit(graphics);
        int radius = Math.max(0, Math.min(3, menu.linkDistance));
        int[] sourceX = { 19, 57, 95, 133 };
        int[] sourceY = { 63, 57, 51, 45 };
        int size = 7 + radius * 12;
        int inset = (MAP_SIZE - size) / 2;
        Blitter.texture(EnderEmitterTextures.RANGE).src(sourceX[radius], sourceY[radius], size, size + 1)
                .dest(x + MAP_X + inset, y + MAP_Y + inset).blit(graphics);
        // These are widgets placed inside the original panels; none of the background is redrawn.
        EnderEmitterTextures.drawCard(graphics, x + TEXT_X, y + 36, TEXT_WIDTH, 14);
        EnderEmitterTextures.drawCard(graphics, x + TEXT_X, y + 53, TEXT_WIDTH, 14);
        EnderEmitterTextures.drawBar(graphics, x + 104, y + 41, 5, false);
        EnderEmitterTextures.drawBar(graphics, x + 104, y + 58, 5, true);
        EnderEmitterTextures.drawBar(graphics, x + TEXT_X, y + CHANNELS_Y + 3, 7, false);
        EnderEmitterTextures.drawCard(graphics, x + CHANNEL_VALUE_X - 1, y + CHANNEL_BUTTON_Y,
                CHANNEL_VALUE_WIDTH + 2, CHANNEL_BUTTON_HEIGHT);
        EnderEmitterTextures.icon(AECSIcon.BAND_VIEW).getBlitter().dest(x + TEXT_X, y + 19).blit(graphics);
    }

    @Override
    public void drawFG(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(graphics, offsetX, offsetY, mouseX, mouseY);
        Component[] summary = summaryLines();
        drawLine(graphics, summary[0], 119, SUMMARY_Y, 122, 0, TEXT_COLOR);
        drawMetric(graphics, "band_channels", usageValue(menu.bandUsedChannels, menu.bandTotalChannels, BAND_VALUE_COLOR), SUMMARY_Y + LINE_SPACING);
        drawMetric(graphics, "linked_channels", usageValue(menu.emitterUsedChannels, menu.emitterTotalChannels, LINKED_VALUE_COLOR), SUMMARY_Y + 2 * LINE_SPACING);
        drawMetric(graphics, "received_channels", compactValue(menu.emitterTotalChannels, METRIC_VALUE_WIDTH)
                .withStyle(style -> style.withColor(BAND_VALUE_COLOR)), CHANNELS_Y);
        drawLine(graphics, panelLabel("requested_channels"), TEXT_X, CHANNELS_Y + CHANNEL_ROW_SPACING, 68, 0, TEXT_COLOR);
        drawLine(graphics, inputModeLabel(), 168, CHANNELS_Y + CHANNEL_ROW_SPACING, 73, 2,
                menu.bandName.isEmpty() ? TEXT_COLOR : menu.expectedChannels == 0 ? 0x795674 : 0x315c87);
        drawLine(graphics, compactValue(menu.expectedChannels, CHANNEL_VALUE_WIDTH), CHANNEL_VALUE_X, CHANNELS_Y + 2 * CHANNEL_ROW_SPACING,
                CHANNEL_VALUE_WIDTH, 1, BAND_VALUE_COLOR);
        int size = menu.linkDistance * 2 + 1;
        int maxSize = menu.maxLinkDistance * 2 + 1;
        drawLine(graphics, Component.translatable("ae2cs.menu.ender_emitter.chunk_range", size, maxSize), 176, 128, 69, 1, TEXT_COLOR);
        drawLine(graphics, Component.literal(menu.boosterCount + "/64"), MAP_X, 197, MAP_SIZE, 1, TEXT_COLOR);
    }

    private Component[] summaryLines() {
        Component band = menu.bandName.isEmpty() ? Component.translatable("ae2cs.menu.ender_broadcaster.none") : Component.literal(menu.bandName);
        return new Component[] {
                Component.translatable("ae2cs.menu.ender_emitter.current_band", band),
                Component.translatable("ae2cs.menu.ender_emitter.band_channels", menu.bandUsedChannels, menu.bandTotalChannels),
                Component.translatable("ae2cs.menu.ender_emitter.linked_channels", menu.emitterUsedChannels, menu.emitterTotalChannels)
        };
    }

    private Component actualChannelsLine() {
        return Component.translatable("ae2cs.menu.ender_emitter.actual_channels", menu.emitterTotalChannels);
    }

    private Component panelLabel(String label) {
        return Component.translatable("ae2cs.menu.ender_emitter.panel." + label);
    }

    private Component inputModeLabel() {
        return panelLabel(menu.bandName.isEmpty() ? "base_input" : menu.expectedChannels == 0 ? "paused_input" : "band_input");
    }

    private Component usageValue(long used, long total, int usedColor) {
        String exact = used + " / " + total;
        boolean fits = font.width(exact) <= METRIC_VALUE_WIDTH;
        String usedValue = fits ? String.valueOf(used) : ReadableNumberConverter.format(Math.max(0, used), 4);
        String totalValue = fits ? String.valueOf(total) : ReadableNumberConverter.format(Math.max(0, total), 4);
        return Component.literal(usedValue).withStyle(style -> style.withColor(usedColor))
                .append(Component.literal(" / ").withStyle(style -> style.withColor(SEPARATOR_COLOR)))
                .append(Component.literal(totalValue).withStyle(style -> style.withColor(TOTAL_VALUE_COLOR)));
    }

    private MutableComponent compactValue(long value, int width) {
        String exact = String.valueOf(value);
        return Component.literal(font.width(exact) <= width ? exact : ReadableNumberConverter.format(Math.max(0, value), 5));
    }

    private void drawMetric(GuiGraphics graphics, String label, Component value, int y) {
        drawLine(graphics, panelLabel(label), 111, y, 49, 0, TEXT_COLOR);
        drawLine(graphics, value, METRIC_VALUE_X, y, METRIC_VALUE_WIDTH, 2, TEXT_COLOR);
    }

    private String fitText(Component text, int width) {
        String value = text.getString();
        if (font.width(value) <= width) return value;
        return font.plainSubstrByWidth(value, Math.max(0, width - font.width("..."))) + "...";
    }

    private void drawLine(GuiGraphics graphics, Component text, int x, int y, int width, int alignment, int color) {
        Component value = font.width(text) <= width ? text : Component.literal(fitText(text, width));
        int textX = x + (alignment == 1 ? (width - font.width(value)) / 2 : alignment == 2 ? width - font.width(value) : 0);
        graphics.drawString(font, value, textX, y, color, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (!menu.getCarried().isEmpty()) return;
        Component[] summary = summaryLines();
        if (inside(mouseX, mouseY, TEXT_X, 20, TEXT_WIDTH, 14)) {
            drawTooltip(graphics, mouseX, mouseY, List.of(summary[0], inputModeLabel()));
        } else if (inside(mouseX, mouseY, TEXT_X, 36, TEXT_WIDTH, 14)) {
            drawTooltip(graphics, mouseX, mouseY, List.of(summary[1]));
        } else if (inside(mouseX, mouseY, TEXT_X, 53, TEXT_WIDTH, 14)) {
            drawTooltip(graphics, mouseX, mouseY, List.of(summary[2]));
        } else if (inside(mouseX, mouseY, TEXT_X, CHANNELS_Y, TEXT_WIDTH, CHANNEL_ROW_SPACING)) {
            drawTooltip(graphics, mouseX, mouseY, List.of(actualChannelsLine()));
        } else if (inside(mouseX, mouseY, TEXT_X, CHANNELS_Y + CHANNEL_ROW_SPACING, TEXT_WIDTH, 9) || inside(mouseX, mouseY, CHANNEL_VALUE_X - 1, CHANNEL_BUTTON_Y, CHANNEL_VALUE_WIDTH + 2, CHANNEL_BUTTON_HEIGHT)) {
            drawTooltip(graphics, mouseX, mouseY, List.of(
                    Component.translatable("ae2cs.menu.ender_emitter.panel.requested_value", menu.expectedChannels),
                    panelLabel("requested_hint"), inputModeLabel()));
        }
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (mouseX >= leftPos + 9 && mouseX < leftPos + 87 && mouseY >= topPos + 19 && mouseY < topPos + 122) {
            bandScrollbar.setCurrentScroll(bandScrollbar.getCurrentScroll() - (int) Math.signum(deltaY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
