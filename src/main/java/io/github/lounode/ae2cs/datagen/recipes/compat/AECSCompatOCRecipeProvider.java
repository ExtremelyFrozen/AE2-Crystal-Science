package io.github.lounode.ae2cs.datagen.recipes.compat;

import com.wintercogs.ae2omnicells.common.init.OCItems;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatOCRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatOCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS OMNI CELLS Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.OMNI_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.OMNI_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.COMPLEX_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.MULTIDIMENSIONAL_EXPANSION_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
    }
}