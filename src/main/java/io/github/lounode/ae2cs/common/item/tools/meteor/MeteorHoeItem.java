package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.HoeItem;

public class MeteorHoeItem extends HoeItem
{
    public MeteorHoeItem(Properties properties)
    {
        super(AECSToolType.METEOR.getToolTier(), -4, 0.0F, properties);
    }
}
