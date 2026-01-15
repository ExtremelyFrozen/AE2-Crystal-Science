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

        public static final TagKey<Block> STORAGE_BLOCK_SKY_STONE = tag("storage_blocks/sky_stone");
        public static final TagKey<Block> STORAGE_BLOCK_RESONATING = tag("storage_blocks/resonating");
        public static final TagKey<Block> STORAGE_BLOCK_ENDER_QUARTZ = tag("storage_blocks/ender_quartz");

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

        public static final TagKey<Item> DUST_RESONATING = tag("dusts/resonating");
        public static final TagKey<Item> DUST_QUARTZ = tag("dusts/quartz");

        public static final TagKey<Item> GEM_RESONATING = tag("gems/resonating");
        public static final TagKey<Item> GEM_SKY_STONE = tag("gems/sky_stone");
        public static final TagKey<Item> GEM_ENDER_QUARTZ = tag("gems/ender_quartz");

        public static final TagKey<Item> STORAGE_BLOCK_SKY_STONE = tag("storage_blocks/sky_stone");
        public static final TagKey<Item> STORAGE_BLOCK_RESONATING = tag("storage_blocks/resonating");
        public static final TagKey<Item> STORAGE_BLOCK_ENDER_QUARTZ = tag("storage_blocks/ender_quartz");

        public static final TagKey<Item> GEARS = tag("gears");
        public static final TagKey<Item> GEARS_WOOD = tag("gears/wood");

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
