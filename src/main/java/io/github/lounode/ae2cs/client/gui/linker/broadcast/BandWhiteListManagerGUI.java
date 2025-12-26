package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.Scrollbar;
import io.github.lounode.ae2cs.api.networking.ServerPlayerInfo;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.BandWhiteListManagerMenu;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class BandWhiteListManagerGUI extends AEBaseScreen<BandWhiteListManagerMenu>
{
    // 可滚动区域
    private static final Rect2i WHITELIST_AREA = new Rect2i(8, 32, 140, 96);   // 左侧：白名单
    private static final Rect2i OTHER_AREA = new Rect2i(160, 32, 140, 96); // 右侧：其它玩家

    private static final int ROW_H = 16;

    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    private final Scrollbar whitelistScrollbar;
    private final Scrollbar otherScrollbar;

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

    private int lastWhitelistTopRow = -1;
    private int lastOtherTopRow = -1;

    public BandWhiteListManagerGUI(BandWhiteListManagerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/band_white_list_manager_menu.json"));

        AESubScreen.addBackButton(menu, "back_button", widgets);

        this.whitelistScrollbar = widgets.addScrollBar("whitelist_scrollbar", Scrollbar.BIG);
        this.otherScrollbar = widgets.addScrollBar("other_scrollbar", Scrollbar.BIG);

        this.whitelistScrollbar.setHeight(WHITELIST_AREA.getHeight());
        this.otherScrollbar.setHeight(OTHER_AREA.getHeight());

        this.whitelistScrollbar.setRange(0, 0, 1);
        this.otherScrollbar.setRange(0, 0, 1);
    }

    @Override
    protected void init()
    {
        super.init();

        this.visibleWhitelistRows = Math.max(1, WHITELIST_AREA.getHeight() / ROW_H);
        this.visibleOtherRows = Math.max(1, OTHER_AREA.getHeight() / ROW_H);

        this.whitelistScrollbar.setHeight(WHITELIST_AREA.getHeight());
        this.otherScrollbar.setHeight(OTHER_AREA.getHeight());

        hideAllWhitelistPanels();
        hideAllOtherPanels();

        whitelistPool.clear();
        otherPool.clear();

        this.lastWhitelistTopRow = -1;
        this.lastOtherTopRow = -1;
        this.totalWhitelistRows = 0;
        this.totalOtherRows = 0;
        this.whitelistScrollbar.setCurrentScroll(0);
        this.otherScrollbar.setCurrentScroll(0);

        refreshFromMenuIfNeeded(true);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        refreshFromMenuIfNeeded(false);

        int wlTop = whitelistScrollbar.getCurrentScroll();
        if (wlTop != lastWhitelistTopRow)
        {
            lastWhitelistTopRow = wlTop;
            layoutWhitelistPanels();
        }

        int otherTop = otherScrollbar.getCurrentScroll();
        if (otherTop != lastOtherTopRow)
        {
            lastOtherTopRow = otherTop;
            layoutOtherPanels();
        }
    }

    private void refreshFromMenuIfNeeded(boolean force)
    {
        ServerPlayerInfo wl = menu.whiteListInfo;
        ServerPlayerInfo other = menu.otherPlayerInfo;

        if (wl == null) wl = new ServerPlayerInfo(Map.of());
        if (other == null) other = new ServerPlayerInfo(Map.of());

        if (!force && Objects.equals(wl, lastWhitelistInfo) && Objects.equals(other, lastOtherInfo))
        {
            return;
        }

        this.lastWhitelistInfo = wl;
        this.lastOtherInfo = other;

        this.whitelistEntries = new ArrayList<>(wl.playerInfo().entrySet());
        this.otherEntries = new ArrayList<>(other.playerInfo().entrySet());

        ensurePoolsUpToDate();

        updateScrollbarRanges();

        this.lastWhitelistTopRow = -1;
        this.lastOtherTopRow = -1;

        layoutWhitelistPanels();
        layoutOtherPanels();
    }

    private void ensurePoolsUpToDate()
    {
        // whitelist pool
        for (int i = whitelistPool.size(); i < whitelistEntries.size(); i++)
        {
            var p = new WhiteListPlayerPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            whitelistPool.add(p);
        }
        for (int i = whitelistEntries.size(); i < whitelistPool.size(); i++)
        {
            var p = whitelistPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        // other pool
        for (int i = otherPool.size(); i < otherEntries.size(); i++)
        {
            var p = new WhiteListPlayerPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            otherPool.add(p);
        }
        for (int i = otherEntries.size(); i < otherPool.size(); i++)
        {
            var p = otherPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void updateScrollbarRanges()
    {
        // whitelist
        this.totalWhitelistRows = whitelistEntries.size();
        int wlMaxScroll = Math.max(0, totalWhitelistRows - visibleWhitelistRows);
        whitelistScrollbar.setRange(0, wlMaxScroll, 1);
        if (whitelistScrollbar.getCurrentScroll() > wlMaxScroll)
        {
            whitelistScrollbar.setCurrentScroll(wlMaxScroll);
        }

        // other
        this.totalOtherRows = otherEntries.size();
        int otherMaxScroll = Math.max(0, totalOtherRows - visibleOtherRows);
        otherScrollbar.setRange(0, otherMaxScroll, 1);
        if (otherScrollbar.getCurrentScroll() > otherMaxScroll)
        {
            otherScrollbar.setCurrentScroll(otherMaxScroll);
        }
    }

    private void hideAllWhitelistPanels()
    {
        for (var p : whitelistPool)
        {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void hideAllOtherPanels()
    {
        for (var p : otherPool)
        {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    private void layoutWhitelistPanels()
    {
        hideAllWhitelistPanels();

        if (whitelistEntries.isEmpty())
        {
            return;
        }

        int maxTop = Math.max(0, totalWhitelistRows - visibleWhitelistRows);
        int startRow = Math.min(whitelistScrollbar.getCurrentScroll(), maxTop);
        int endExclusive = Math.min(startRow + visibleWhitelistRows, totalWhitelistRows);

        int x0 = leftPos + WHITELIST_AREA.getX();
        int y0 = topPos + WHITELIST_AREA.getY();

        for (int row = startRow; row < endExclusive; row++)
        {
            var e = whitelistEntries.get(row);
            var panel = whitelistPool.get(row);

            panel.setInfo(e.getKey(), e.getValue());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }

    private void layoutOtherPanels()
    {
        hideAllOtherPanels();

        if (otherEntries.isEmpty())
        {
            return;
        }

        int maxTop = Math.max(0, totalOtherRows - visibleOtherRows);
        int startRow = Math.min(otherScrollbar.getCurrentScroll(), maxTop);
        int endExclusive = Math.min(startRow + visibleOtherRows, totalOtherRows);

        int x0 = leftPos + OTHER_AREA.getX();
        int y0 = topPos + OTHER_AREA.getY();

        for (int row = startRow; row < endExclusive; row++)
        {
            var e = otherEntries.get(row);
            var panel = otherPool.get(row);

            panel.setInfo(e.getKey(), e.getValue());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }
}
