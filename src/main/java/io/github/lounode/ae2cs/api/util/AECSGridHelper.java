package io.github.lounode.ae2cs.api.util;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class AECSGridHelper
{
    /**
     * 获取网络中随机一个控制器节点
     */
    @Nullable
    public static IGridNode getControlNode(@NotNull IGrid grid)
    {
        Iterator<IGridNode> it = grid.getMachineNodes(ControllerBlockEntity.class).iterator();
        return it.hasNext() ? it.next() : null;
    }
}
