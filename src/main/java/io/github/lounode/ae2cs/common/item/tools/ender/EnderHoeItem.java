package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;

import net.minecraft.world.item.HoeItem;

public class EnderHoeItem extends HoeItem implements LinkableTool {

    public EnderHoeItem(Properties properties) {
        super(AECSToolType.ENDER.getToolTier(), properties.attributes(createAttributes(AECSToolType.ENDER.getToolTier(), -3.0F, 0.0F)));
    }
}
