package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.AxeItem;

public class MeteorAxeItem extends AxeItem
{
    public MeteorAxeItem(Properties properties)
    {
        super(AECSToolType.METEOR.getToolTier(), 5.0F, -3.0F, properties);
    }
}
