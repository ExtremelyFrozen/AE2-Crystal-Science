package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.PickaxeItem;

public class EnderPickaxeItem extends PickaxeItem implements LinkableTool
{
    public EnderPickaxeItem(Properties properties)
    {
        super(AECSToolType.ENDER.getToolTier(), 1, -2.8F, properties);
    }
}
