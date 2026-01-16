package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.util.AGTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAGRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS AG Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AG_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AGSingletons.ORIGINATION_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CrystalAggregatorRecipeBuilder.aggregating(AGSingletons.EMBER_CRYSTAL, 64, 64000)
                .require(AECSItems.PURE_EMBER_CRYSTAL, 32)
                .require(AEBlocks.TINY_TNT, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.EMBER_SEED.toStack(4), 16000)
                .require(AGTags.EMBER_DUST, 1)
                .require(Items.BLAZE_POWDER, 1)
                .require(Tags.Items.GUNPOWDERS, 2)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AGSingletons.ORIGINATION_PROCESSOR, 64, 144000)
                .require(AGSingletons.ORIGINATION_PRINT, 64)
                .require(Tags.Items.DUSTS_REDSTONE, 64)
                .require(AEItems.SILICON_PRINT, 64)
                .save(compatOut);
    }
}