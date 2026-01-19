package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.SwordItem;

public class EnderSwordItem extends SwordItem implements LinkableTool
{
    public EnderSwordItem(Properties properties)
    {
        super(AECSToolType.ENDER.getToolTier(), properties.attributes(createAttributes(AECSToolType.ENDER.getToolTier(), 3, -2.4F)));
    }
}