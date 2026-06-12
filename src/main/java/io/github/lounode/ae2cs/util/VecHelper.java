package io.github.lounode.ae2cs.util;

import net.minecraft.core.Vec3i;

public class VecHelper {

    private VecHelper() {}

    /**
     * 判断两个 Vec3i 之间是否小于等于指定的切比雪夫距离
     */
    public static boolean closerThanChebyshev(Vec3i a, Vec3i b, int maxDistance) {
        int dx = Math.abs(a.getX() - b.getX());
        if (dx > maxDistance) return false;

        int dy = Math.abs(a.getY() - b.getY());
        if (dy > maxDistance) return false;

        int dz = Math.abs(a.getZ() - b.getZ());
        return dz <= maxDistance;
    }

    /**
     * 获取两个Vec3i的切比雪夫距离
     */
    public static int chebyshevDistance(Vec3i a, Vec3i b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return Math.max(dx, Math.max(dy, dz));
    }
}
