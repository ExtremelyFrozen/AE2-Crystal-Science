package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FrequencyBandManagerGUI extends AEBaseScreen<FrequencyBandManagerMenu> {

    private static final Rect2i BROADCASTER_AREA = new Rect2i(9, 21, 158, 119);
    private static final int ROW_H = 17;
    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;
    private static final int SCROLL_HEIGHT = 120;

    private Component bandName = Component.empty();
    private Component channelUsage = Component.empty();
    private Component bandError = Component.empty();
    private AETextField inputPassword;
    private AE2Button confirmChangePasswordButton;

    private AECSToggleButton changePublicButton;
    private AECSToggleButton changeAllowMemoryCardButton;
    private AECSIconButton removeBandButton;
    private AECSIconButton openBandWhiteManagerButton;

    private final Scrollbar broadcasterScrollbar;

    private record DisplayEntry(GlobalPos pos, boolean isSender) {}

    private List<DisplayEntry> broadcasterEntries = List.of();
    private final List<BroadcasterPanel> broadcasterPanelPool = new ArrayList<>();

    private int visibleBroadcasterRows = 1;
    private int totalBroadcasterRows = 0;
    private int lastBroadcasterTopRow = -1;
    private int lastBroadcasterHash = 0;

    private int clickRemoveButtonTicks = 0;

    public FrequencyBandManagerGUI(FrequencyBandManagerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_manager_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);

        inputPassword = widgets.addTextField("input_password_field");
        inputPassword.setPlaceholder(Component.translatable("ae2cs.menu.frequency_manager_menu.input_password_field"));
        confirmChangePasswordButton = widgets.addButton(
                "confirm_password_change_button",
                Component.translatable("ae2cs.menu.frequency_manager_menu.confirm_password_button"),
                () -> menu.sendChangePasswordAction(inputPassword.getValue()));

        changePublicButton = new AECSToggleButton(
                AdaptedAE2Icon.UNLOCKED, AdaptedAE2Icon.LOCKED,
                Component.translatable("ae2cs.menu.frequency_manager.button.change_public.title"),
                Component.translatable("ae2cs.menu.frequency_manager.button.change_public.desc"),
                menu::sendChangePublicAction);
        addToLeftToolbar(changePublicButton);

        changeAllowMemoryCardButton = new AECSToggleButton(
                AECSIcon.ALLOW_MEMORY_CARD, AECSIcon.DENY_MEMORY_CARD,
                Component.translatable("ae2cs.menu.frequency_manager.button.change_allow_memory_card.title"),
                Component.translatable("ae2cs.menu.frequency_manager.button.change_allow_memory_card.desc"),
                menu::sendChangeAllowMemoryCardAction);
        addToLeftToolbar(changeAllowMemoryCardButton);

        openBandWhiteManagerButton = new AECSIconButton(button -> menu.sendOpenBandManagerMenu()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AECSIcon.WHITE_LIST_MODE;
            }
        };
        openBandWhiteManagerButton.setMessage(Component.translatable("ae2cs.menu.frequency_manager.button.open_white_list_manager"));
        addToLeftToolbar(openBandWhiteManagerButton);

        removeBandButton = new AECSIconButton(button -> {
            if (clickRemoveButtonTicks > 0) {
                menu.sendDeleteBand();
            } else {
                clickRemoveButtonTicks = 40;
                getPlayer().displayClientMessage(Component.translatable("ae2cs.menu.frequency_manager_menu.click_again"), false);
            }
        }) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AdaptedAE2Icon.CLEAR;
            }
        };
        removeBandButton.setMessage(Component.translatable("ae2cs.menu.frequency_manager.button.remove_band"));
        this.addToLeftToolbar(removeBandButton);

        this.broadcasterScrollbar = widgets.addScrollBar("broadcaster_scrollbar", Scrollbar.BIG);
        this.broadcasterScrollbar.setHeight(SCROLL_HEIGHT);
        this.broadcasterScrollbar.setRange(0, 0, 1);
    }

    @Override
    protected void init() {
        super.init();

        this.visibleBroadcasterRows = Math.max(1, BROADCASTER_AREA.getHeight() / ROW_H);

        this.broadcasterScrollbar.setHeight(SCROLL_HEIGHT);

        this.broadcasterPanelPool.clear();

        this.lastBroadcasterTopRow = -1;
        this.totalBroadcasterRows = 0;
        this.broadcasterScrollbar.setCurrentScroll(0);
        refreshBroadcastersFromMenuIfNeeded(true);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        clickRemoveButtonTicks--;
        changePublicButton.setState(menu.bandDetailInfo.isPublic());
        changeAllowMemoryCardButton.setState(menu.bandDetailInfo.allowedMemoryCardCopy());

        bandName = Component.translatable("ae2cs.menu.frequency_manager_menu.band_name", menu.bandDetailInfo.name());
        channelUsage = Component.translatable("ae2cs.menu.frequency_manager_menu.band_usage", menu.usedChannels + "/" + menu.usableChannels);
        bandError = switch (menu.bandDetailInfo.errorState()) {
            case FINE -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.fine");
            case MISSING_CONTROLLER -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.missing_controller");
            case CONTROLLER_CONFLICT -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.controller_conflict");
            case SENDER_ERROR -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.sender_error");
            case NO_SENDER -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.no_sender");
        };
        setTextContent("band_name", bandName);
        setTextContent("channel_usage", channelUsage);
        setTextContent("band_error", bandError);

        // 如果服务端同步过来了新 sender/receiver 列表，刷新滚动列表
        refreshBroadcastersFromMenuIfNeeded(false);

        // 滚动位置变化时重排
        int topRow = broadcasterScrollbar.getCurrentScroll();
        if (topRow != lastBroadcasterTopRow) {
            lastBroadcasterTopRow = topRow;
            reLayoutBroadcasterPanels();
        }
    }

    private void refreshBroadcastersFromMenuIfNeeded(boolean force) {
        var detail = menu.bandDetailInfo;
        if (detail == null) {
            broadcasterEntries = List.of();
            updateBroadcasterScrollbarRange();
            reLayoutBroadcasterPanels();
            return;
        }

        int h = Objects.hash(detail.senderList(), detail.receiverList());
        if (!force && h == lastBroadcasterHash) {
            return;
        }
        lastBroadcasterHash = h;

        List<DisplayEntry> tmp = new ArrayList<>();
        for (var p : detail.senderList()) {
            tmp.add(new DisplayEntry(p, true));
        }
        for (var p : detail.receiverList()) {
            tmp.add(new DisplayEntry(p, false));
        }
        this.broadcasterEntries = tmp;

        ensureBroadcasterPanelPoolUpToDate();
        updateBroadcasterScrollbarRange();

        this.lastBroadcasterTopRow = -1;
        reLayoutBroadcasterPanels();
    }

    private void ensureBroadcasterPanelPoolUpToDate() {
        for (int i = broadcasterPanelPool.size(); i < broadcasterEntries.size(); i++) {
            var p = new BroadcasterPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            broadcasterPanelPool.add(p);
        }

        // 隐藏多余面板
        for (int i = broadcasterEntries.size(); i < broadcasterPanelPool.size(); i++) {
            var p = broadcasterPanelPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void updateBroadcasterScrollbarRange() {
        this.totalBroadcasterRows = broadcasterEntries.size();
        int maxScroll = Math.max(0, totalBroadcasterRows - visibleBroadcasterRows);

        broadcasterScrollbar.setRange(0, maxScroll, 1);

        if (broadcasterScrollbar.getCurrentScroll() > maxScroll) {
            broadcasterScrollbar.setCurrentScroll(maxScroll);
        }
    }

    private void reLayoutBroadcasterPanels() {
        // 先全部隐藏
        for (var p : broadcasterPanelPool) {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        if (broadcasterEntries.isEmpty()) {
            return;
        }

        int maxTop = Math.max(0, totalBroadcasterRows - visibleBroadcasterRows);
        int startRow = Math.min(broadcasterScrollbar.getCurrentScroll(), maxTop);
        int endExclusive = Math.min(startRow + visibleBroadcasterRows, totalBroadcasterRows);

        int x0 = leftPos + BROADCASTER_AREA.getX();
        int y0 = topPos + BROADCASTER_AREA.getY();

        for (int row = startRow; row < endExclusive; row++) {
            var e = broadcasterEntries.get(row);
            var panel = broadcasterPanelPool.get(row);

            panel.setInfo(e.pos(), e.isSender());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }
}
