package io.github.lounode.ae2cs.common.item.tools.meteor;

import io.github.lounode.ae2cs.common.item.tools.AECSToolType;

import net.minecraft.world.item.PickaxeItem;

public class MeteorPickaxeItem extends PickaxeItem {

    public MeteorPickaxeItem(Properties properties) {
        super(AECSToolType.METEOR.getToolTier(), 1, -2.8F, properties);
    }
}
