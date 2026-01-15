package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCraftRecipeProvider extends AECSRecipeProvider
{
    public AECSCraftRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Crafting Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.resonatingSeed)
                .requires(AECSTags.Items.DUST_RESONATING)
                .requires(ConventionTags.FLUIX_DUST)
                .requires(ConventionTags.SKY_STONE_DUST)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(AECSItems.RESONATING_DUST), has(AECSTags.Items.DUST_RESONATING))
                .save(recipeOutput, getCrafterPath(AECSItems.resonatingSeed, false));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSItems.BLANK_PRINT_PRESS)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', AECSItems.SIMPLE_CIRCUIT_PRINT)
                .define('b', ConventionTags.INSCRIBER_PRESSES)
                .unlockedBy(getHasName(AECSItems.SIMPLE_CIRCUIT_PRINT), has(AECSItems.SIMPLE_CIRCUIT_PRINT))
                .save(recipeOutput, getCrafterPath(AECSItems.BLANK_PRINT_PRESS, true));

        // 晶能聚合器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AEItems.FLUIX_PEARL)
                .define('b', AEBlocks.MOLECULAR_ASSEMBLER)
                .define('c', Tags.Items.DUSTS_REDSTONE)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AEItems.LOGIC_PROCESSOR)
                .define('f', AEBlocks.CONDENSER)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK, true));

        // 电路切片机
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CIRCUIT_ETCHER_BLOCK)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ded")
                .define('a', AECSTags.Items.GEM_SKY_STONE)
                .define('b', AEBlocks.INSCRIBER)
                .define('c', AECSItems.RESONATING_PROCESSOR)
                .define('d', AEItems.ENGINEERING_PROCESSOR)
                .define('e', AECSItems.BLANK_PRINT_PRESS)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CIRCUIT_ETCHER_BLOCK, true));

        // 催生仓
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .define('a', AEBlocks.GROWTH_ACCELERATOR)
                .define('b', AEBlocks.QUARTZ_CLUSTER)
                .define('c', AEBlocks.QUARTZ_GLASS)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', ConventionTags.GLASS_CABLE)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, true));

        // 谐振仓
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.GEM_RESONATING)
                .define('b', AEBlocks.ENERGY_ACCEPTOR)
                .define('c', Tags.Items.INGOTS_IRON)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AECSTags.Items.GEM_SKY_STONE)
                .define('f', AEBlocks.VIBRATION_CHAMBER)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK, true));

        // 粉碎机
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .define('b', AEBlocks.INSCRIBER)
                .define('c', Tags.Items.INGOTS_IRON)
                .define('d', AECSBlocks.QUARTZ_GRINDSTONE_BLOCK)
                .define('e', AEItems.LOGIC_PROCESSOR)
                .define('f', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK, true));

        // 石英磨具
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.QUARTZ_GRINDSTONE_BLOCK)
                .pattern("aba")
                .pattern("cac")
                .pattern("aca")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AECSTags.Items.GEARS_WOOD)
                .define('c', ConventionTags.ALL_CERTUS_QUARTZ)
                .unlockedBy(getHasName(AECSItems.WOODEN_GEAR), has(AECSItems.WOODEN_GEAR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK, true));

        // 熵变反应器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.GEM_SKY_STONE)
                .define('b', AEItems.ENTROPY_MANIPULATOR)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .define('d', AEBlocks.CONDENSER)
                .define('e', Tags.Items.INGOTS_IRON)
                .define('f', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK, true));

        // 自装配式供应器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.STORAGE_BLOCK_SKY_STONE)
                .define('b', AEBlocks.MOLECULAR_ASSEMBLER)
                .define('c', AEBlocks.PATTERN_PROVIDER)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AEItems.SINGULARITY)
                .define('f', AEItems.CELL_COMPONENT_256K)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, true));

        // 末影广播装置
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENDER_BROADCASTER_BLOCK)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ada")
                .define('a', AEItems.MATTER_BALL)
                .define('b', AEParts.ME_P2P_TUNNEL)
                .define('c', AECSTags.Items.STORAGE_BLOCK_RESONATING)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENDER_BROADCASTER_BLOCK, true));

        // 末影发信器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENDER_EMITTER_BLOCK)
                .pattern("aba")
                .pattern(" c ")
                .pattern("ded")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AEBlocks.WIRELESS_ACCESS_POINT)
                .define('c', AECSItems.RESONATING_PROCESSOR)
                .define('d', ConventionTags.SMART_CABLE)
                .define('e', AECSTags.Items.STORAGE_BLOCK_ENDER_QUARTZ)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENDER_EMITTER_BLOCK, true));
    }
}