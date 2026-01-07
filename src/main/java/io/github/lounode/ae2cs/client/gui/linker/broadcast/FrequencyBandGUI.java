package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.FrequencyBandMenu;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FrequencyBandGUI extends AEBaseScreen<FrequencyBandMenu>
{
    /**
     * 可用于显示频段的区域（相对 GUI 左上角）
     */
    private static final Rect2i PANEL_AREA = new Rect2i(9, 39, 158, 170);

    /**
     * 单个频段面板高度
     */
    private static final int ROW_H = 17;

    /**
     * 隐藏停靠点
     */
    private static final int HIDE_X = -10000;
    private static final int HIDE_Y = -10000;

    private static final int SCROLL_HEIGHT = 169;

    private final Scrollbar scrollbar;
    private final AETextField searchField;

    // 当前频段快照
    private BroadcastBandsField lastBandsInfo = null;
    private List<BroadcastBandsField.Entry> bands = List.of(); // 当前显示用的条目快照

    // 过滤 + 布局状态
    private String searchQuery = "";
    private final List<Integer> filteredIndex = new ArrayList<>();
    private final List<FrequencyBandInfoPanel> panelPool = new ArrayList<>();

    private int visibleRows = 0;
    private int totalRows = 0;
    private int lastTopRow = -1;

    public FrequencyBandGUI(FrequencyBandMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title, StyleManager.loadStyleDoc("/screens/frequency_band_menu.json"));
        AESubScreen.addBackButton(menu, "back_button", widgets);

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.searchField = widgets.addTextField("search");

        this.scrollbar.setHeight(SCROLL_HEIGHT);
        this.scrollbar.setRange(0, 0, 1);

        this.searchField.setResponder(s -> {
            this.searchQuery = (s == null ? "" : s.trim().toLowerCase(Locale.ROOT));
            rebuildFilter();
            updateScrollbarRangeByFilteredSize();
            this.scrollbar.setCurrentScroll(0);
            this.lastTopRow = -1;
            reLayoutVisiblePanels();
        });
    }

    @Override
    protected void init()
    {
        super.init();
        this.visibleRows = Math.max(1, PANEL_AREA.getHeight() / ROW_H);
        this.scrollbar.setHeight(SCROLL_HEIGHT);

        panelPool.clear();

        refreshBandsFromMenuIfNeeded(true);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        // 如果服务端同步过来了新数据刷新频段排布
        refreshBandsFromMenuIfNeeded(false);

        // 滚动位置变化时重排
        int topRow = scrollbar.getCurrentScroll();
        if (topRow != lastTopRow)
        {
            lastTopRow = topRow;
            reLayoutVisiblePanels();
        }
    }

    /**
     * 从menu读取bandsInfo，若变化则刷新排布
     */
    private void refreshBandsFromMenuIfNeeded(boolean force)
    {
        BroadcastBandsField current = menu.bandsInfo;
        if (current == null)
        {
            current = new BroadcastBandsField(List.of());
        }

        if (!force && Objects.equals(current, lastBandsInfo))
        {
            return;
        }

        this.lastBandsInfo = current;
        this.bands = current.bands();

        ensurePanelPoolUpToDate();
        rebuildFilter();
        updateScrollbarRangeByFilteredSize();
        this.lastTopRow = -1;
        reLayoutVisiblePanels();
    }

    /**
     * 确保面板数量能够显示全部频段
     */
    private void ensurePanelPoolUpToDate()
    {
        for (int i = panelPool.size(); i < bands.size(); i++)
        {
            var p = new FrequencyBandInfoPanel(HIDE_X, HIDE_Y, menu);
            p.active = false;
            p.visible = false;
            addRenderableWidget(p);
            panelPool.add(p);
        }

        // 隐藏多余面板
        for (int i = bands.size(); i < panelPool.size(); i++)
        {
            var p = panelPool.get(i);
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }
    }

    /**
     * 重建过滤索引
     */
    private void rebuildFilter()
    {
        filteredIndex.clear();

        if (bands.isEmpty())
        {
            return;
        }

        if (searchQuery.isEmpty())
        {
            for (int i = 0; i < bands.size(); i++) filteredIndex.add(i);
        }
        else
        {
            for (int i = 0; i < bands.size(); i++)
            {
                String name = bands.get(i).name();
                if (name != null && name.toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)))
                {
                    filteredIndex.add(i);
                }
            }
        }
    }

    /**
     * 根据filteredIndex更新滚动范围
     */
    private void updateScrollbarRangeByFilteredSize()
    {
        this.totalRows = filteredIndex.size();
        int maxScroll = Math.max(0, totalRows - visibleRows);

        // 每格滚动 1 行
        scrollbar.setRange(0, maxScroll, 1);

        if (scrollbar.getCurrentScroll() > maxScroll)
        {
            scrollbar.setCurrentScroll(maxScroll);
        }
    }

    /**
     * 按当前滚动和过滤情况，排布频段面板
     */
    private void reLayoutVisiblePanels()
    {
        for (FrequencyBandInfoPanel p : panelPool)
        {
            p.active = false;
            p.visible = false;
            p.setX(HIDE_X);
            p.setY(HIDE_Y);
        }

        if (filteredIndex.isEmpty())
        {
            return;
        }

        int maxTop = Math.max(0, totalRows - visibleRows);
        int startRow = Math.min(scrollbar.getCurrentScroll(), maxTop);
        int endExclusive = Math.min(startRow + visibleRows, totalRows);

        int x0 = leftPos + PANEL_AREA.getX();
        int y0 = topPos + PANEL_AREA.getY();

        for (int row = startRow; row < endExclusive; row++)
        {
            int idxInBands = filteredIndex.get(row);
            var entry = bands.get(idxInBands);

            var panel = panelPool.get(idxInBands);
            panel.setInfo(entry.name(), entry.isPublic(), entry.isEncrypted());
            panel.setX(x0);
            panel.setY(y0 + (row - startRow) * ROW_H);
            panel.active = true;
            panel.visible = true;
        }
    }
}