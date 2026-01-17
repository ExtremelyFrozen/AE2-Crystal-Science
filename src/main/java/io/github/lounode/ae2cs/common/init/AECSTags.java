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
        public static final TagKey<Block> ORES_CERTUS_QUARTZ = tag("ores/certus_quartz");

        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ =
                aecsTag("storage_blocks/pure_crystal/ender_quartz_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL =
                aecsTag("storage_blocks/pure_crystal/resonating_crystal_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL =
                aecsTag("storage_blocks/pure_crystal/meteor_crystal_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL =
                aecsTag("storage_blocks/pure_crystal/redstone_crystal_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL =
                aecsTag("storage_blocks/pure_crystal/quantum_crystal_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ =
                aecsTag("storage_blocks/pure_crystal/rose_quartz_block");
        public static final TagKey<Block> STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL =
                aecsTag("storage_blocks/pure_crystal/irradiated_crystal_block");
        public static final TagKey<Block> STORAGE_BLOCK_SILICON = tag("storage_blocks/silicon");

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

        public static final TagKey<Item> PURE_CRYSTAL = aecsTag("pure_crystal");
        public static final TagKey<Item> PURE_CERTUS_QUARTZ_CRYSTAL = aecsTag("pure_crystal/certus_quartz");
        public static final TagKey<Item> PURE_FLUIX_CRYSTAL = aecsTag("pure_crystal/fluix");
        public static final TagKey<Item> PURE_NETHER_QUARTZ_CRYSTAL = aecsTag("pure_crystal/nether_quartz");
        public static final TagKey<Item> PURE_ENDER_QUARTZ = aecsTag("pure_crystal/ender_quartz");
        public static final TagKey<Item> PURE_METEOR_CRYSTAL = aecsTag("pure_crystal/meteor_crystal");
        public static final TagKey<Item> PURE_RESONATING_CRYSTAL = aecsTag("pure_crystal/resonating_crystal");
        public static final TagKey<Item> PURE_ENTRO_CRYSTAL = aecsTag("pure_crystal/entro_crystal");
        public static final TagKey<Item> PURE_REDSTONE_CRYSTAL = aecsTag("pure_crystal/redstone_crystal");
        public static final TagKey<Item> PURE_QUANTUM_CRYSTAL = aecsTag("pure_crystal/quantum_crystal");
        public static final TagKey<Item> PURE_ROSE_QUARTZ = aecsTag("pure_crystal/rose_quartz");
        public static final TagKey<Item> PURE_IRRADIATED_CRYSTAL = aecsTag("pure_crystal/irradiated_crystal");
        public static final TagKey<Item> PURE_EMBER_CRYSTAL = aecsTag("pure_crystal/ember_crystal");

        public static final TagKey<Item> DUST_RESONATING = tag("dusts/resonating");
        public static final TagKey<Item> DUST_QUARTZ = tag("dusts/quartz");
        public static final TagKey<Item> DUST_QUANTUM_ALLOY = tag("dusts/quantum_alloy");

        public static final TagKey<Item> GEM_RESONATING = tag("gems/resonating");
        public static final TagKey<Item> GEM_SKY_STONE_CRYSTAL = tag("gems/sky_stone_crystal");
        public static final TagKey<Item> GEM_ENDER_QUARTZ = tag("gems/ender_quartz");

        public static final TagKey<Item> GEARS = tag("gears");
        public static final TagKey<Item> GEARS_WOOD = tag("gears/wood");

        public static final TagKey<Item> FLOURS = tag("flours");
        public static final TagKey<Item> FLOURS_WHEAT = tag("flours/wheat");

        public static final TagKey<Item> ORES_CERTUS_QUARTZ = tag("ores/certus_quartz");

        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ =
                tag("storage_blocks/pure_crystal/ender_quartz_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL =
                tag("storage_blocks/pure_crystal/resonating_crystal_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL =
                tag("storage_blocks/pure_crystal/meteor_crystal_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL =
                tag("storage_blocks/pure_crystal/redstone_crystal_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL =
                tag("storage_blocks/pure_crystal/quantum_crystal_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ =
                tag("storage_blocks/pure_crystal/rose_quartz_block");
        public static final TagKey<Item> STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL =
                tag("storage_blocks/pure_crystal/irradiated_crystal_block");
        public static final TagKey<Item> STORAGE_BLOCK_SILICON = tag("storage_blocks/silicon");

        // 仅引用获取
        public static final TagKey<Item> STORAGE_BLOCK_CERTUS_QUARTZ = tag("storage_blocks/certus_quartz");
        public static final TagKey<Item> STORAGE_BLOCK_SKY_STEEL = tag("storage_blocks/sky_steel");
        public static final TagKey<Item> DUSTS_URANIUM = tag("dusts/uranium");

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
