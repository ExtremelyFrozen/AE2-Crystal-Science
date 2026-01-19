package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.ShovelItem;

public class EnderShovelItem extends ShovelItem implements LinkableTool
{
    public EnderShovelItem(Properties properties)
    {
        super(AECSToolType.ENDER.getToolTier(), properties.attributes(createAttributes(AECSToolType.ENDER.getToolTier(), 1.5F, -3.0F)));
    }
}