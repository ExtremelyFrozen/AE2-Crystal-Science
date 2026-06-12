package io.github.lounode.ae2cs.api.util;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class AECSGridHelper {

    /**
     * 获取网络中随机一个控制器节点
     */
    @Nullable
    public static IGridNode getControlNode(@NotNull IGrid grid) {
        Iterator<IGridNode> it = grid.getMachineNodes(ControllerBlockEntity.class).iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * 获取该节点在其通向控制器的路径下还能额外承载/分发的频道数。
     * - 网络 booting/repath 期间返回 0（避免抖动）
     * - INFINITE 返回 Integer.MAX_VALUE
     * - 端点设备会从其父节点（控制器方向第一跳）开始计算，避免把端点自身 maxChannels 当成瓶颈
     */
    public static int getDistributableChannelsOnControllerPath(IGridNode node) {
        if (!(node instanceof GridNode gn)) return 0;

        var grid = gn.getGrid();
        if (grid == null) return 0;

        var ps = grid.getPathingService();
        if (ps.getChannelMode() == ChannelMode.INFINITE) return Integer.MAX_VALUE;

        // booting/repath中，保守返回 0
        if (!gn.hasGridBooted()) return 0;

        // controller自己不作为瓶颈
        if (gn.getOwner() instanceof ControllerBlockEntity) return Integer.MAX_VALUE;

        // 如果是“非线缆类节点”，从父节点开始算
        GridNode cur = isCableLike(gn) ? gn : parentTowardController(gn);
        if (cur == null) return 0;

        int available = Integer.MAX_VALUE;

        // 向上爬到 controller，取路径上最小 slack
        for (int guard = 0; guard < 2048 && cur != null; guard++) {
            if (cur.getOwner() instanceof ControllerBlockEntity) break;

            if (!cur.hasFlag(GridFlags.CANNOT_CARRY)) {
                int slack = cur.getMaxChannels() - cur.getUsedChannels();
                if (slack < available) {
                    available = slack;
                    if (available <= 0) return 0;
                }
            }

            cur = parentTowardController(cur);
        }

        return Math.max(0, available);
    }

    /**
     * 是否是线缆类节点
     */
    private static boolean isCableLike(GridNode n) {
        // DENSE_CAPACITY指致密线缆、PREFERRED是普通线缆
        return n.hasFlag(GridFlags.DENSE_CAPACITY) || n.hasFlag(GridFlags.PREFERRED);
    }

    private static @Nullable GridNode parentTowardController(GridNode n) {
        try {
            // controller route 的那条边
            var route = (GridConnection) n.getControllerRoute();
            var other = route.getOtherSide(n);
            return (other instanceof GridNode gn) ? gn : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
