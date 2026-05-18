package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.ConventionTags;
import appeng.core.definitions.AEItems;
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
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSMiscRecipeProvider extends AECSRecipeProvider
{
    public AECSMiscRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes()
    {
        var recipeOutput = this.output;

        // 爆炸以获得谐振水晶粉
        TransformRecipeBuilder.transform(recipeOutput, getTransformPath(AECSItems.RESONATING_DUST),
                AECSItems.RESONATING_DUST, 1,
                TransformCircumstance.EXPLOSION,
                AECSItems.PURE_METEOR_CRYSTAL,
                Blocks.REDSTONE_BLOCK,
                AECSItems.PURE_ENDER_QUARTZ);

        // 添加谐振样板配方
        SpecialRecipeBuilder.special(() -> new ResonatingPatternUpgradeRecipe(CraftingBookCategory.MISC))
                .save(recipeOutput, recipeKey(AE2CrystalScience.makeId("resonating_pattern_upgrade")));

        // 添加谐振样板拆解
        shapeless(RecipeCategory.MISC, AEItems.BLANK_PATTERN)
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
        InscriberRecipeBuilder.inscribe(tag(AECSTags.Items.GEM_RESONATING), AECSItems.RESONATING_CIRCUIT_PRINT, 1)
                .setTop(Ingredient.of(AECSItems.RESONATING_PRINT_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_CIRCUIT_PRINT));
        InscriberRecipeBuilder.inscribe(tag(ConventionTags.SKY_STONE_DUST), AECSItems.RESONATING_PROCESSOR, 1)
                .setTop(Ingredient.of(AECSItems.RESONATING_CIRCUIT_PRINT))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_PROCESSOR));

        InscriberRecipeBuilder.inscribe(tag(Tags.Items.GEMS_QUARTZ), AECSItems.SIMPLE_CIRCUIT_PRINT, 1)
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.SIMPLE_CIRCUIT_PRINT));
        InscriberRecipeBuilder.inscribe(tag(Tags.Items.DUSTS_REDSTONE), AECSItems.SIMPLE_PROCESSOR, 1)
                .setTop(Ingredient.of(AECSItems.SIMPLE_CIRCUIT_PRINT))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.SIMPLE_PROCESSOR));
    }

    public static class Runner extends RecipeProvider.Runner
    {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
        {
            super(output, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput output)
        {
            return new AECSMiscRecipeProvider(provider, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "AECS Misc Recipes";
        }
    }
}
