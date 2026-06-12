package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import io.github.lounode.ae2cs.api.networking.ServerPlayerInfo;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.BandWhiteListManagerMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class BandWhiteListManagerGUI extends AEBaseScreen<BandWhiteListManagerMenu> {

    // 可滚动区域
    private static final Rect2i WHITELIST_AREA = new Rect2i(9, 22, 158, 187); // 左侧：白名单
    private static final Rect2i OTHER_AREA = new Rect2i(193, 22, 158, 187); // 右侧：其它玩家

    private static final int ROW_H = 17;

    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    // 快照（用于判断是否需要重建布局）
    private ServerPlayerInfo lastWhitelistInfo = null;
    private ServerPlayerInfo lastOtherInfo = null;

    private List<Map.Entry<UUID, String>> whitelistEntries = List.of();
    private List<Map.Entry<UUID, String>> otherEntries = List.of();

    // 面板池
    private final List<WhiteListPlayerPanel> whitelistPool = new ArrayList<>();
    private final List<WhiteListPlayerPanel> otherPool = new ArrayList<>();

    // 行数与滚动状态
    private int visibleWhitelistRows = 1;
    private int visibleOtherRows = 1;

    private int totalWhitelistRows = 0;
    private int totalOtherRows = 0;

    // 顶部行-发挥类似滚动条的记录功能
    private int whitelistTopRow = 0;
    private int otherTopRow = 0;

    // 用于判断是否需要重新布局
    private int lastWhitelistTopRow = -1;
    private int lastOtherTopRow = -1;

    public BandWhiteListManagerGUI(BandWhiteListManagerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/band_white_list_manager_menu.json"));
        AESubScreen.addBackButton(menu, "back_button", widgets);
    }

    @Override
    protected void init() {
        super.init();

        this.visibleWhitelistRows = Math.max(1, WHITELIST_AREA.getHeight() / ROW_H);
        this.visibleOtherRows = Math.max(1, OTHER_AREA.getHeight() / ROW_H);

        hideAllWhitelistPanels();
        hideAllOtherPanels();

        whitelistPool.clear();
        otherPool.clear();

        this.lastWhitelistTopRow = -1;
        this.lastOtherTopRow = -1;

        this.totalWhitelistRows = 0;
        this.totalOtherRows = 0;

        this.whitelistTopRow = 0;
        this.otherTopRow = 0;

        refreshFromMenuIfNeeded(true);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        refreshFromMenuIfNeeded(false);

        if (whitelistTopRow != lastWhitelistTopRow) {
            lastWhitelistTopRow = whitelistTopRow;
            layoutWhitelistPanels();
        }

        if (otherTopRow != lastOtherTopRow) {
            lastOtherTopRow = otherTopRow;
            layoutOtherPanels();
        }
    }

    private void refreshFromMenuIfNeeded(boolean force) {
        ServerPlayerInfo wl = menu.whiteListInfo;
        ServerPlayerInfo other = menu.otherPlayerInfo;

        if (wl == null) wl = new ServerPlayerInfo(Map.of());
        if (other == null) other = new ServerPlayerInfo(Map.of());

        if (!force && Objects.equals(wl, lastWhitelistInfo) && Objects.equals(other, lastOtherInfo)) {
            return;
        }

        this.lastWhitelistInfo = wl;
        this.lastOtherInfo = other;

        this.whitelistEntries = new ArrayList<>(wl.playerInfo().entrySet());
        this.otherEntries = new ArrayList<>(other.playerInfo().entrySet());

        ensurePoolsUpToDate();

        // 更新总行数并夹紧 topRow
        this.totalWhitelistRows = whitelistEntries.size();
        this.totalOtherRows = otherEntries.size();

        this.whitelistTopRow = clampTopRow(whitelistTopRow, totalWhitelistRows, visibleWhitelistRows);
        this.otherTopRow = clampTopRow(otherTopRow, totalOtherRows, visibleOtherRows);

        this.lastWhitelistTopRow = -1;
        this.lastOtherTopRow = -1;

        layoutWhitelistPanels();
        layoutOtherPanels();
    }

    private static int clampTopRow(int topRow, int totalRows, int visibleRows) {
        int maxTop = Math.max(0, totalRows - visibleRows);
        return Mth.clamp(topRow, 0, maxTop);
    }

    private void ensurePoolsUpToDate() {
        // whitelist pool
        for (int i = whitelistPool.size(); i < whitelistEntries.size(); i++) {
            var p = new WhiteListPlayerPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            whitelistPool.add(p);
        }
        for (int i = whitelistEntries.size(); i < whitelistPool.size(); i++) {
            var p = whitelistPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        // other pool
        for (int i = otherPool.size(); i < otherEntries.size(); i++) {
            var p = new WhiteListPlayerPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            otherPool.add(p);
        }
        for (int i = otherEntries.size(); i < otherPool.size(); i++) {
            var p = otherPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void hideAllWhitelistPanels() {
        for (var p : whitelistPool) {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void hideAllOtherPanels() {
        for (var p : otherPool) {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void layoutWhitelistPanels() {
        hideAllWhitelistPanels();

        if (whitelistEntries.isEmpty()) {
            return;
        }

        int maxTop = Math.max(0, totalWhitelistRows - visibleWhitelistRows);
        int startRow = Math.min(whitelistTopRow, maxTop);
        int endExclusive = Math.min(startRow + visibleWhitelistRows, totalWhitelistRows);

        int x0 = leftPos + WHITELIST_AREA.getX();
        int y0 = topPos + WHITELIST_AREA.getY();

        for (int row = startRow; row < endExclusive; row++) {
            var e = whitelistEntries.get(row);
            var panel = whitelistPool.get(row);

            panel.setInfo(e.getKey(), e.getValue());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }

    private void layoutOtherPanels() {
        hideAllOtherPanels();

        if (otherEntries.isEmpty()) {
            return;
        }

        int maxTop = Math.max(0, totalOtherRows - visibleOtherRows);
        int startRow = Math.min(otherTopRow, maxTop);
        int endExclusive = Math.min(startRow + visibleOtherRows, totalOtherRows);

        int x0 = leftPos + OTHER_AREA.getX();
        int y0 = topPos + OTHER_AREA.getY();

        for (int row = startRow; row < endExclusive; row++) {
            var e = otherEntries.get(row);
            var panel = otherPool.get(row);

            panel.setInfo(e.getKey(), e.getValue());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }

    private boolean isMouseInArea(double mouseX, double mouseY, Rect2i area) {
        int ax = leftPos + area.getX();
        int ay = topPos + area.getY();
        return mouseX >= ax && mouseX < ax + area.getWidth() && mouseY >= ay && mouseY < ay + area.getHeight();
    }

    /**
     * 在没有滚动条的情况下，我们使用此方法手动进行滚动操作
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int amount = (int) Math.ceil(Math.abs(deltaY));
        if (amount <= 0) {
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        if (isMouseInArea(mouseX, mouseY, WHITELIST_AREA)) {
            int dir = deltaY < 0 ? 1 : -1;
            int old = whitelistTopRow;
            whitelistTopRow = clampTopRow(whitelistTopRow + dir * amount, totalWhitelistRows, visibleWhitelistRows);

            if (whitelistTopRow != old) {
                // 立即布局
                lastWhitelistTopRow = -1;
                layoutWhitelistPanels();
                return true;
            }
            return true; // 区域内吞掉滚轮事件
        }

        if (isMouseInArea(mouseX, mouseY, OTHER_AREA)) {
            int dir = deltaY < 0 ? 1 : -1;
            int old = otherTopRow;
            otherTopRow = clampTopRow(otherTopRow + dir * amount, totalOtherRows, visibleOtherRows);

            if (otherTopRow != old) {
                lastOtherTopRow = -1;
                layoutOtherPanels();
                return true;
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
