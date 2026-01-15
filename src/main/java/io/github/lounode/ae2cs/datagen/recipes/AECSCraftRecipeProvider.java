package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
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
    }
}