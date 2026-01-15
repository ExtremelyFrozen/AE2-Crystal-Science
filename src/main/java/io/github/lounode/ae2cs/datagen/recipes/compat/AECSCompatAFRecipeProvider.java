package io.github.lounode.ae2cs.datagen.recipes.compat;

import com.glodblock.github.appflux.common.AFSingletons;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAFRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAFRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS AF Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AF_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AFSingletons.ENERGY_PROCESSOR_PRESS, AECSItems.SIMPLE_PRINT_PRESS);
    }
}