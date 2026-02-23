package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.ShovelItem;

public class MeteorShovelItem extends ShovelItem
{
    public MeteorShovelItem(Properties properties)
    {
        super(AECSToolType.METEOR.getToolTier(), 1.5F, -3.0F, properties);
    }
}
