package io.github.lounode.ae2cs.common.item.tools.ender;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.world.item.Item;

public class EnderPickaxeItem extends Item implements LinkableTool {
    public EnderPickaxeItem(Properties properties) {
        super(properties.pickaxe(AECSToolType.ENDER.getToolMaterial(), 1.0F, -2.8F)
                .repairable(AECSToolType.ENDER.getRepairIngredient()));
    }
}