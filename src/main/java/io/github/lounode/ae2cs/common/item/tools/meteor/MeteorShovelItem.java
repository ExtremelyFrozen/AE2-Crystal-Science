package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.Item;

public class MeteorShovelItem extends Item {
    public MeteorShovelItem(Properties properties) {
        super(properties.shovel(AECSToolType.METEOR.getToolMaterial(), 1.5F, -3.0F)
                .repairable(AECSToolType.METEOR.getRepairIngredient()));
    }
}
