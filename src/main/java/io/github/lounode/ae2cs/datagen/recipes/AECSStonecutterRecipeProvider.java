package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSStonecutterRecipeProvider extends AECSRecipeProvider
{
    public AECSStonecutterRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes()
    {
        var recipeOutput = this.output;

        // 木齿轮（已经包含去皮原木）
        stonecutterResultFromTag(recipeOutput, RecipeCategory.MISC, AECSItems.WOODEN_GEAR, ItemTags.LOGS);

        // 压印模板
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.SILICON_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.CALCULATION_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.ENGINEERING_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.LOGIC_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.RESONATING_PRINT_PRESS, AECSItems.BLANK_PRINT_PRESS);
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
            return new AECSStonecutterRecipeProvider(provider, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "AECS Stonecutter Recipes";
        }
    }
}
