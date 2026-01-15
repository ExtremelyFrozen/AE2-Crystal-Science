package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAAERecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAAERecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS AAE Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AAE_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AAEItems.QUANTUM_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);
    }
}