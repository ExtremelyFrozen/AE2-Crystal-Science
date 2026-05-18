package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.Item;

public class MeteorHoeItem extends Item {
    public MeteorHoeItem(Properties properties) {
        super(properties.hoe(AECSToolType.METEOR.getToolMaterial(), -4.0F, 0.0F)
                .repairable(AECSToolType.METEOR.getRepairIngredient()));
    }
}
