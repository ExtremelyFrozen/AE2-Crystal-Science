package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.Item;

public class MeteorAxeItem extends Item {
    public MeteorAxeItem(Properties properties) {
        super(properties.axe(AECSToolType.METEOR.getToolMaterial(), 5.0F, -3.0F)
                .repairable(AECSToolType.METEOR.getRepairIngredient()));
    }
}
