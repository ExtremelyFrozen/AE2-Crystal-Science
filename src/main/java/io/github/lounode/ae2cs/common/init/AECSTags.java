package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AECSTags
{
    public static class Blocks
    {
        public static final TagKey<Block> CERTUS_QUARTZ_ORE = tag("ores/certus_quartz");

        private static TagKey<Block> tag(String name)
        {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> aecsTag(String name)
        {
            return BlockTags.create(AE2CrystalScience.makeId(name));
        }
    }

    public static class Items
    {
        public static final TagKey<Item> CRYSTAL_SEEDS = aecsTag("crystal_seeds");
        public static final TagKey<Item> PURIFIED_CRYSTAL = aecsTag("purified_crystal");

        private static TagKey<Item> tag(String name)
        {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> aecsTag(String name)
        {
            return ItemTags.create(AE2CrystalScience.makeId(name));
        }
    }
}
