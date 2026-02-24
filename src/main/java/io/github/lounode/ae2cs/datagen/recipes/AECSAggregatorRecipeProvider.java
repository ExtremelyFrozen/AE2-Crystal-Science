package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSEnchantments;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSAggregatorRecipeProvider extends AECSRecipeProvider
{
    public AECSAggregatorRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Aggregator Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> recipeOutput)
    {

        // 谐振样板转换器
        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.RESONATING_PATTERN_CONVERTER, 1, 16000)
                .require(AEItems.BLANK_PATTERN, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(recipeOutput);

        // 初级样板供应器
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.toStack(), 4000)
                .require(AECSItems.SIMPLE_PROCESSOR, 1)
                .require(Blocks.CRAFTING_TABLE, 1)
                .require(AECSTags.Items.GEARS_WOOD, 2)
                .save(recipeOutput);

        // 集成接口
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(AEBlocks.PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AEBlocks.INTERFACE, 1)
                .save(recipeOutput);

        // 末影接口
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.ENDER_INTERFACE_BLOCK.toStack(), 16000)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 4)
                .require(AEBlocks.INTERFACE, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(recipeOutput);

        // 谐振样板供应器
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.toStack(), 16000)
                .require(AEBlocks.PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AECSBlocks.ENDER_INTERFACE_BLOCK, 1)
                .save(recipeOutput);

        // 石英震荡钟
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.toStack(), 16000)
                .require(AEParts.LEVEL_EMITTER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, 1)
                .save(recipeOutput);

        // 末影链接工具
        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.enderLink.toStack(), 16000)
                .require(AEItems.WIRELESS_RECEIVER, 1)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 4)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(recipeOutput);

        // 种子
        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.RESONATING_SEED, 32, 51200)
                .require(AECSTags.Items.DUST_RESONATING, 16)
                .require(ConventionTags.FLUIX_DUST, 16)
                .require(AEItems.SKY_DUST, 16)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.CERTUS_QUARTZ_SEED, 32, 51200)
                .require(AEItems.CERTUS_QUARTZ_DUST, 8)
                .require(Blocks.SAND, 32)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.FLUIX_CRYSTAL_SEED, 32, 51200)
                .require(AEItems.FLUIX_DUST, 8)
                .require(Blocks.SAND, 32)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.NETHER_QUARTZ_SEED, 32, 51200)
                .require(AECSTags.Items.DUST_QUARTZ, 8)
                .require(Blocks.SAND, 32)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.ENDER_QUARTZ_SEED, 32, 51200)
                .require(AECSTags.Items.DUST_QUARTZ, 8)
                .require(ConventionTags.ENDER_PEARL_DUST, 8)
                .require(Blocks.END_STONE, 16)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.METEOR_SEED, 32, 51200)
                .require(AEItems.SKY_DUST, 8)
                .require(ConventionTags.CERTUS_QUARTZ_DUST, 8)
                .require(Blocks.GRAVEL, 16)
                .unlockedBy(getHasName(Blocks.GRAVEL), has(Blocks.GRAVEL))
                .save(recipeOutput);


        // 扩展接口系列
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.toStack(), 16000)
                .require(AECSBlocks.ENDER_INTERFACE_BLOCK, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 2)
                .require(AEItems.CAPACITY_CARD, 3)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK.toStack(), 16000)
                .require(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 2)
                .require(AEItems.CAPACITY_CARD, 3)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 2)
                .require(AEItems.CAPACITY_CARD, 3)
                .save(recipeOutput);

        // 处理器聚合配方
        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.RESONATING_PROCESSOR, 32, 51200)
                .require(AECSItems.RESONATING_CIRCUIT_PRINT, 32)
                .require(AEItems.SKY_DUST, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(recipeOutput);
        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.SIMPLE_PROCESSOR, 32, 51200)
                .require(AECSItems.SIMPLE_CIRCUIT_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(recipeOutput);
        CrystalAggregatorRecipeBuilder.aggregating(AEItems.LOGIC_PROCESSOR, 32, 51200)
                .require(AEItems.LOGIC_PROCESSOR_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(recipeOutput);
        CrystalAggregatorRecipeBuilder.aggregating(AEItems.CALCULATION_PROCESSOR, 32, 51200)
                .require(AEItems.CALCULATION_PROCESSOR_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(recipeOutput);
        CrystalAggregatorRecipeBuilder.aggregating(AEItems.ENGINEERING_PROCESSOR, 32, 51200)
                .require(AEItems.ENGINEERING_PROCESSOR_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(recipeOutput);

        // 其他配方
        CrystalAggregatorRecipeBuilder.aggregating(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 32, 51200)
                .require(AECSTags.Items.PURE_CERTUS_QUARTZ_CRYSTAL, 16)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL, 16)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEItems.FLUIX_CRYSTAL, 32, 51200)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL, 8)
                .require(Tags.Items.DUSTS_REDSTONE, 8)
                .require(Tags.Items.GEMS_QUARTZ, 8)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.RESONATING_DUST, 32, 51200)
                .require(AECSTags.Items.GEM_SKY_STONE_CRYSTAL, 32)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 32)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 32)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Blocks.END_STONE, 64, 51200)
                .require(ConventionTags.ENDER_PEARL_DUST, 16)
                .require(Blocks.SAND, 32)
                .require(Blocks.COBBLESTONE, 32)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEBlocks.DAMAGED_BUDDING_QUARTZ, 1, 16000)
                .require(AECSItems.CERTUS_QUARTZ_SEED, 1)
                .require(AECSTags.Items.PURE_RESONATING_CRYSTAL, 1)
                .require(AEBlocks.QUARTZ_BLOCK, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEBlocks.CHIPPED_BUDDING_QUARTZ, 1, 16000)
                .require(AECSItems.CERTUS_QUARTZ_SEED, 1)
                .require(AECSTags.Items.PURE_RESONATING_CRYSTAL, 1)
                .require(AEBlocks.DAMAGED_BUDDING_QUARTZ, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEBlocks.FLAWED_BUDDING_QUARTZ, 1, 16000)
                .require(AECSItems.CERTUS_QUARTZ_SEED, 1)
                .require(AECSTags.Items.PURE_RESONATING_CRYSTAL, 1)
                .require(AEBlocks.CHIPPED_BUDDING_QUARTZ, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEBlocks.FLAWLESS_BUDDING_QUARTZ, 1, 80000)
                .require(AECSItems.CERTUS_QUARTZ_SEED, 6)
                .require(AECSTags.Items.PURE_RESONATING_CRYSTAL, 6)
                .require(AEBlocks.FLAWED_BUDDING_QUARTZ, 6)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEBlocks.MYSTERIOUS_CUBE, 1, 16000)
                .require(AEBlocks.NOT_SO_MYSTERIOUS_CUBE, 1)
                .require(AECSTags.Items.DUST_RESONATING, 4)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEItems.SINGULARITY, 1, 320000)
                .require(AEItems.MATTER_BALL, 32)
                .require(AEItems.FLUIX_PEARL, 1)
                .require(AECSTags.Items.DUST_RESONATING, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(AEItems.FLUIX_PEARL, 1, 16000)
                .require(Items.ENDER_PEARL, 1)
                .require(ConventionTags.FLUIX_DUST, 4)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.HEART_OF_THE_SEA, 1, 80000)
                .require(Items.NAUTILUS_SHELL, 4)
                .require(Items.ENDER_EYE, 1)
                .require(Items.PRISMARINE_CRYSTALS, 8)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.SLIME_BALL, 1, 16000)
                .require(AECSTags.Items.FLOURS, 1)
                .require(Items.LIME_DYE, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.REDSTONE, 9, 14400)
                .require(Tags.Items.DUSTS_REDSTONE, 1)
                .require(Tags.Items.DYES_RED, 1)
                .require(Items.SUGAR, 8)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.GUNPOWDER, 9, 14400)
                .require(Tags.Items.GUNPOWDER, 1)
                .require(Tags.Items.DYES_GRAY, 1)
                .require(Items.SUGAR, 8)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.GLOWSTONE_DUST, 9, 14400)
                .require(Tags.Items.DUSTS_GLOWSTONE, 1)
                .require(Tags.Items.DYES_ORANGE, 1)
                .require(Items.SUGAR, 8)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Blocks.CRYING_OBSIDIAN, 1, 16000)
                .require(Blocks.OBSIDIAN, 1)
                .require(Items.AMETHYST_SHARD, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Blocks.BUDDING_AMETHYST, 1, 16000)
                .require(Items.AMETHYST_SHARD, 1)
                .require(AECSItems.PURE_RESONATING_CRYSTAL, 1)
                .require(Blocks.AMETHYST_BLOCK, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Items.GLOW_INK_SAC, 1, 16000)
                .require(Tags.Items.DUSTS_GLOWSTONE, 1)
                .require(Items.INK_SAC, 1)
                .save(recipeOutput);

        CrystalAggregatorRecipeBuilder.aggregating(Blocks.BELL, 1, 16000)
                .require(Tags.Items.RODS_WOODEN, 3)
                .require(Tags.Items.INGOTS_GOLD, 4)
                .save(recipeOutput);

        // 附魔书获取
        CrystalAggregatorRecipeBuilder.aggregating(
                        enchantedItem(Items.ENCHANTED_BOOK, 1, AECSEnchantments.ENDER_LINK.get(), 1), 64000)
                .require(Items.BOOK, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(recipeOutput, "aggregator/enchanted_book_of_ender_link");
    }
}