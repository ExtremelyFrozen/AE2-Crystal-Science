package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.Item;

public class EnderAxeItem extends Item implements LinkableTool {
    public EnderAxeItem(Properties properties) {
        super(properties.axe(AECSToolType.ENDER.getToolMaterial(), 5.0F, -3.0F)
                .repairable(AECSToolType.ENDER.getRepairIngredient()));
    }
}
