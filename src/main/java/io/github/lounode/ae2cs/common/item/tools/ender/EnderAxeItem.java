package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;

import net.minecraft.world.item.AxeItem;

public class EnderAxeItem extends AxeItem implements LinkableTool {

    public EnderAxeItem(Properties properties) {
        super(AECSToolType.ENDER.getToolTier(), 5.0f, -3.0f, properties);
    }
}
