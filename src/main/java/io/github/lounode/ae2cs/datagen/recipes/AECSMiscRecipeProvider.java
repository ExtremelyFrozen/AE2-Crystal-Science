package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.common.recipe.ResonatingPatternUpgradeRecipe;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSMiscRecipeProvider extends AECSRecipeProvider
{
    public AECSMiscRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Misc Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> recipeOutput)
    {

        // 爆炸以获得谐振水晶粉
        TransformRecipeBuilder.transform(recipeOutput, getTransformPath(AECSItems.RESONATING_DUST),
                AECSItems.RESONATING_DUST, 1,
                TransformCircumstance.EXPLOSION,
                AECSItems.PURE_METEOR_CRYSTAL,
                Blocks.REDSTONE_BLOCK,
                AECSItems.PURE_ENDER_QUARTZ);

        // 添加谐振样板配方
        SpecialRecipeBuilder.special(ResonatingPatternUpgradeRecipe::new)
                .save(recipeOutput, AE2CrystalScience.makeId("resonating_pattern_upgrade"));

        // 添加谐振样板拆解
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AEItems.BLANK_PATTERN)
                .requires(AECSItems.RESONATING_PATTERN)
                .unlockedBy("just_has_resonating_pattern", has(AECSItems.RESONATING_PATTERN))
                .save(recipeOutput);

        // 锻造台配方
        smithingTransform(recipeOutput, RecipeCategory.MISC, AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE,
                AECSItems.METEOR_CRYSTAL_PICKAXE, AECSItems.PURE_RESONATING_CRYSTAL, AECSItems.RESONATING_CRYSTAL_PICKAXE);
        smithingTransform(recipeOutput, RecipeCategory.MISC, AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE,
                AECSItems.METEOR_CRYSTAL_AXE, AECSItems.PURE_RESONATING_CRYSTAL, AECSItems.RESONATING_CRYSTAL_AXE);
        smithingTransform(recipeOutput, RecipeCategory.MISC, AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE,
                AECSItems.METEOR_CRYSTAL_SWORD, AECSItems.PURE_RESONATING_CRYSTAL, AECSItems.RESONATING_CRYSTAL_SWORD);
        smithingTransform(recipeOutput, RecipeCategory.MISC, AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE,
                AECSItems.METEOR_CRYSTAL_HOE, AECSItems.PURE_RESONATING_CRYSTAL, AECSItems.RESONATING_CRYSTAL_HOE);
        smithingTransform(recipeOutput, RecipeCategory.MISC, AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE,
                AECSItems.METEOR_CRYSTAL_SHOVEL, AECSItems.PURE_RESONATING_CRYSTAL, AECSItems.RESONATING_CRYSTAL_SHOVEL);

        // 压印系列
        InscriberRecipeBuilder.inscribe(AECSTags.Items.GEM_RESONATING, AECSItems.RESONATING_CIRCUIT_PRINT, 1)
                .setTop(Ingredient.of(AECSItems.RESONATING_PRINT_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_CIRCUIT_PRINT));
        InscriberRecipeBuilder.inscribe(ConventionTags.SKY_STONE_DUST, AECSItems.RESONATING_PROCESSOR, 1)
                .setTop(Ingredient.of(AECSItems.RESONATING_CIRCUIT_PRINT))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_PROCESSOR));

        InscriberRecipeBuilder.inscribe(Tags.Items.GEMS_QUARTZ, AECSItems.SIMPLE_CIRCUIT_PRINT, 1)
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.SIMPLE_CIRCUIT_PRINT));
        InscriberRecipeBuilder.inscribe(Tags.Items.DUSTS_REDSTONE, AECSItems.SIMPLE_PROCESSOR, 1)
                .setTop(Ingredient.of(AECSItems.SIMPLE_CIRCUIT_PRINT))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.SIMPLE_PROCESSOR));
    }
}
