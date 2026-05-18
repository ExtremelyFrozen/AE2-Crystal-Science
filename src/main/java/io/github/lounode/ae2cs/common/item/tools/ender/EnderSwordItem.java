package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.Item;

public class EnderSwordItem extends Item implements LinkableTool {
    public EnderSwordItem(Properties properties) {
        super(properties.sword(AECSToolType.ENDER.getToolMaterial(), 3, -2.4F)
                .repairable(AECSToolType.ENDER.getRepairIngredient()));
    }
}