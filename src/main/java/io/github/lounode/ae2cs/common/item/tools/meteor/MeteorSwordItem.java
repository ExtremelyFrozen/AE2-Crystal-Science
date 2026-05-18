package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.Item;

public class MeteorSwordItem extends Item {
    public MeteorSwordItem(Properties properties) {
        super(properties.sword(AECSToolType.METEOR.getToolMaterial(), 3, -2.4F)
                .repairable(AECSToolType.METEOR.getRepairIngredient()));
    }
}
