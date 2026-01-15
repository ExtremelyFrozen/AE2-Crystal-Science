package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import com.glodblock.github.appflux.common.AFSingletons;
import com.glodblock.github.extendedae.common.EAESingletons;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.sapporo1101.appgen.common.AGSingletons;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
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
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.SILICON_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.CALCULATION_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.ENGINEERING_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AEItems.LOGIC_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.RESONATING_PRINT_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AFSingletons.ENERGY_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AAEItems.QUANTUM_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, EAESingletons.CONCURRENT_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
        stonecutterResultFromItem(recipeOutput, RecipeCategory.MISC, AGSingletons.ORIGINATION_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
    }
}
