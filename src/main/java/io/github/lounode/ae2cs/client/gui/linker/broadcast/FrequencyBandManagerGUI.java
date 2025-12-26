package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandManagerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FrequencyBandManagerGUI extends AEBaseScreen<FrequencyBandManagerMenu>
{
    private static final Rect2i BROADCASTER_AREA = new Rect2i(8, 110, 140, 80);
    private static final int ROW_H = 16;
    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    private Component bandName = Component.empty();
    private Component channelUsage = Component.empty();
    private Component bandError = Component.empty();
    private AETextField inputPassword;
    private AE2Button confirmChangePasswordButton;
    private AECheckbox changePublicBox;
    private AECheckbox changeAllowMemoryCardBox;
    private AE2Button openBandWhiteManagerButton;
    private AE2Button removeBandButton;

    private final Scrollbar broadcasterScrollbar;

    private record DisplayEntry(GlobalPos pos, boolean isSender)
    {
    }

    private List<DisplayEntry> broadcasterEntries = List.of();
    private final List<BroadcasterPanel> broadcasterPanelPool = new ArrayList<>();

    private int visibleBroadcasterRows = 1;
    private int totalBroadcasterRows = 0;
    private int lastBroadcasterTopRow = -1;
    private int lastBroadcasterHash = 0;

    private int clickRemoveButtonTicks = 0;

    public FrequencyBandManagerGUI(FrequencyBandManagerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/frequency_band_manager_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);

        inputPassword = widgets.addTextField("input_password_field");
        inputPassword.setPlaceholder(Component.translatable("ae2cs.menu.frequency_manager_menu.input_password_field"));
        confirmChangePasswordButton = widgets.addButton(
                "confirm_password_change_button",
                Component.translatable("ae2cs.menu.frequency_manager_menu.confirm_password_button"),
                () -> menu.sendChangePasswordAction(inputPassword.getValue()));
        // 很tm操蛋，为什么tm的checkbox的runable是在自身已经切换完状态后才运行的？？？有点反直觉
        changePublicBox = widgets.addCheckbox(
                "change_public_box",
                Component.translatable("ae2cs.menu.frequency_manager_menu.change_public_box"),
                () -> menu.sendChangePublicAction(changePublicBox.isSelected()));
        changeAllowMemoryCardBox = widgets.addCheckbox(
                "change_allow_memory_card_box",
                Component.translatable("ae2cs.menu.frequency_manager_menu.change_allow_memory_card_box"),
                () -> menu.sendChangeAllowMemoryCardAction(changeAllowMemoryCardBox.isSelected()));
        openBandWhiteManagerButton = widgets.addButton(
                "open_band_whitelist_manager_button",
                Component.translatable("ae2cs.menu.frequency_manager_menu.open_band_whitelist_menu"),
                menu::sendOpenBandManagerMenu);
        removeBandButton = widgets.addButton(
                "remove_band_button",
                Component.translatable("ae2cs.menu.frequency_manager_menu.remove_band_button"),
                () ->
                {
                    if (clickRemoveButtonTicks > 0)
                    {
                        menu.sendDeleteBand();
                    }
                    else
                    {
                        clickRemoveButtonTicks = 40;
                        getPlayer().displayClientMessage(Component.translatable("ae2cs.menu.frequency_manager_menu.click_again"), false);
                    }
                });

        this.broadcasterScrollbar = widgets.addScrollBar("broadcaster_scrollbar", Scrollbar.BIG);
        this.broadcasterScrollbar.setHeight(BROADCASTER_AREA.getHeight());
        this.broadcasterScrollbar.setRange(0, 0, 1);
    }

    @Override
    protected void init()
    {
        super.init();

        this.visibleBroadcasterRows = Math.max(1, BROADCASTER_AREA.getHeight() / ROW_H);
        refreshBroadcastersFromMenuIfNeeded(true);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font, bandName, 10, 20, 4210752, false);
        guiGraphics.drawString(this.font, channelUsage, 10, 30, 4210752, false);
        guiGraphics.drawString(this.font, bandError, 10, 90, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        clickRemoveButtonTicks--;
        bandName = Component.translatable("ae2cs.menu.frequency_manager_menu.band_name", menu.bandDetailInfo.name());
        channelUsage = Component.translatable("ae2cs.menu.frequency_manager_menu.band_usage", menu.usedChannels + "/" + menu.usableChannels);
        changePublicBox.setSelected(menu.bandDetailInfo.isPublic());
        changeAllowMemoryCardBox.setSelected(menu.bandDetailInfo.allowedMemoryCardCopy());
        bandError = switch (menu.bandDetailInfo.errorState())
        {
            case FINE -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.fine");
            case MISSING_CONTROLLER ->
                    Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.missing_controller");
            case CONTROLLER_CONFLICT ->
                    Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.controller_conflict");
            case SENDER_ERROR -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.sender_error");
            case NO_SENDER -> Component.translatable("ae2cs.menu.frequency_manager_menu.band_error.no_sender");
        };

        // 如果服务端同步过来了新 sender/receiver 列表，刷新滚动列表
        refreshBroadcastersFromMenuIfNeeded(false);

        // 滚动位置变化时重排
        int topRow = broadcasterScrollbar.getCurrentScroll();
        if (topRow != lastBroadcasterTopRow)
        {
            lastBroadcasterTopRow = topRow;
            reLayoutBroadcasterPanels();
        }
    }

    private void refreshBroadcastersFromMenuIfNeeded(boolean force)
    {
        var detail = menu.bandDetailInfo;
        if (detail == null)
        {
            broadcasterEntries = List.of();
            updateBroadcasterScrollbarRange();
            reLayoutBroadcasterPanels();
            return;
        }

        int h = Objects.hash(detail.senderList(), detail.receiverList());
        if (!force && h == lastBroadcasterHash)
        {
            return;
        }
        lastBroadcasterHash = h;

        List<DisplayEntry> tmp = new ArrayList<>();
        for (var p : detail.senderList())
        {
            tmp.add(new DisplayEntry(p, true));
        }
        for (var p : detail.receiverList())
        {
            tmp.add(new DisplayEntry(p, false));
        }
        this.broadcasterEntries = tmp;

        ensureBroadcasterPanelPoolUpToDate();
        updateBroadcasterScrollbarRange();

        this.lastBroadcasterTopRow = -1;
        reLayoutBroadcasterPanels();
    }

    private void ensureBroadcasterPanelPoolUpToDate()
    {
        for (int i = broadcasterPanelPool.size(); i < broadcasterEntries.size(); i++)
        {
            var p = new BroadcasterPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            broadcasterPanelPool.add(p);
        }

        // 隐藏多余面板
        for (int i = broadcasterEntries.size(); i < broadcasterPanelPool.size(); i++)
        {
            var p = broadcasterPanelPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void updateBroadcasterScrollbarRange()
    {
        this.totalBroadcasterRows = broadcasterEntries.size();
        int maxScroll = Math.max(0, totalBroadcasterRows - visibleBroadcasterRows);

        broadcasterScrollbar.setRange(0, maxScroll, 1);

        if (broadcasterScrollbar.getCurrentScroll() > maxScroll)
        {
            broadcasterScrollbar.setCurrentScroll(maxScroll);
        }
    }

    private void reLayoutBroadcasterPanels()
    {
        // 先全部隐藏
        for (var p : broadcasterPanelPool)
        {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        if (broadcasterEntries.isEmpty())
        {
            return;
        }

        int maxTop = Math.max(0, totalBroadcasterRows - visibleBroadcasterRows);
        int startRow = Math.min(broadcasterScrollbar.getCurrentScroll(), maxTop);
        int endExclusive = Math.min(startRow + visibleBroadcasterRows, totalBroadcasterRows);

        int x0 = leftPos + BROADCASTER_AREA.getX();
        int y0 = topPos + BROADCASTER_AREA.getY();

        for (int row = startRow; row < endExclusive; row++)
        {
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
