package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.SwordItem;

public class MeteorSwordItem extends SwordItem
{
    public MeteorSwordItem(Properties properties)
    {
        super(AECSToolType.METEOR.getToolTier(), 3, -2.4F, properties);
    }
}
