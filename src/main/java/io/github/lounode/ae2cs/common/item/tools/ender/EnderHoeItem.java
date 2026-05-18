package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.Item;

public class EnderHoeItem extends Item implements LinkableTool {
    public EnderHoeItem(Properties properties) {
        super(properties.hoe(AECSToolType.ENDER.getToolMaterial(), -3.0F, 0.0F)
                .repairable(AECSToolType.ENDER.getRepairIngredient()));
    }
}