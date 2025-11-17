package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AECSTags
{
    public static final TagKey<Item> CRYSTAL_SEEDS = TagKey.create(Registries.ITEM, AE2CrystalScience.makeId("crystal_seeds"));
    public static final TagKey<Item> PURIFIED_CRYSTAL = TagKey.create(Registries.ITEM, AE2CrystalScience.makeId("purified_crystal"));
}
