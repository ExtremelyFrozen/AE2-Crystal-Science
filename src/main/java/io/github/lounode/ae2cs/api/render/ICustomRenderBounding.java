package io.github.lounode.ae2cs.api.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public interface ICustomRenderBounding
{
    boolean enableCustomRenderBounding();

    int getRange();

    default AABB getCustomBoundingBox(BlockPos centerPos)
    {
        if (enableCustomRenderBounding())
        {
            return new AABB(
                    centerPos.getX() - getRange(),
                    centerPos.getY() - getRange(),
                    centerPos.getZ() - getRange(),
                    centerPos.getX() + getRange(),
                    centerPos.getY() + getRange(),
                    centerPos.getZ() + getRange()
            );
        }
        else
            return new AABB(centerPos);
    }
}
