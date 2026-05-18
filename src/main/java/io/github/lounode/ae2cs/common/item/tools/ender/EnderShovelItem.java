package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.Item;

public class EnderShovelItem extends Item implements LinkableTool {
    public EnderShovelItem(Properties properties) {
        super(properties.shovel(AECSToolType.ENDER.getToolMaterial(), 1.5F, -3.0F)
                .repairable(AECSToolType.ENDER.getRepairIngredient()));
    }
}