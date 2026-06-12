package io.github.lounode.ae2cs.util;

import net.minecraft.core.BlockPos;

public final class BlockPosHelper {

    private BlockPosHelper() {}

    /**
     * 检查两个pos是否六向相邻
     */
    public static boolean isAdjacent(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return dx + dy + dz == 1;
    }
}
