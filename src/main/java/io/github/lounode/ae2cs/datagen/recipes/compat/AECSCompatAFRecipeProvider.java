package io.github.lounode.ae2cs.datagen.recipes.compat;

import com.glodblock.github.appflux.common.AFSingletons;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
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

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AFSingletons.ENERGY_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        chargedRecipeWithAggregator(compatOut, AECSItems.pureRedstoneCrystal, AFSingletons.REDSTONE_CRYSTAL);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 1600)
                .require(AECSItems.pureRedstoneCrystal, 1)
                .save(compatOut);
        CrystalPulverizerRecipeBuilder.pulverizing(Items.REDSTONE.getDefaultInstance(), 1600)
                .require(AECSItems.REDSTONE_CRYSTAL_DUST, 1)
                .save(compatOut);
    }
}