package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import net.minecraft.world.item.Item;

public class MeteorPickaxeItem extends Item {
    public MeteorPickaxeItem(Properties properties) {
        super(properties.pickaxe(AECSToolType.METEOR.getToolMaterial(), 1.0F, -2.8F)
                .repairable(AECSToolType.METEOR.getRepairIngredient()));
    }
}
