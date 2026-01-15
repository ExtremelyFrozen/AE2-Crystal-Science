package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSStonecutterRecipeProvider extends AECSRecipeProvider
{
    public AECSStonecutterRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Stonecutter Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        // 木齿轮（已经包含去皮原木）
        stonecutterResultFromTag(recipeOutput, RecipeCategory.MISC, AECSItems.WOODEN_GEAR, ItemTags.LOGS);

        // 压印模板
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.SILICON_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.CALCULATION_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.ENGINEERING_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.LOGIC_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.RESONATING_PRINT_PRESS, AECSItems.BLANK_PRINT_PRESS);
    }
}
