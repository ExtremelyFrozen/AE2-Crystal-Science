package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import com.glodblock.github.appflux.common.AFSingletons;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
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

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.pureRedstoneCrystal, AECSBlocks.PURE_REDSTONE_CRYSTAL_BLOCK);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AFSingletons.ENERGY_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        chargedRecipeWithAggregator(compatOut, AECSItems.pureRedstoneCrystal, AFSingletons.REDSTONE_CRYSTAL);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 8000)
                .require(AECSItems.pureRedstoneCrystal, 1)
                .save(compatOut);
        CrystalPulverizerRecipeBuilder.pulverizing(Items.REDSTONE.getDefaultInstance(), 8000)
                .require(AECSItems.REDSTONE_CRYSTAL_DUST, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.redstoneCrystalSeed.toStack(4), 16000)
                .require(Tags.Items.DUSTS_GLOWSTONE, 2)
                .require(Tags.Items.DUSTS_REDSTONE, 1)
                .require(AECSItems.REDSTONE_CRYSTAL_DUST, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AFSingletons.ENERGY_PROCESSOR, 64, 144000)
                .require(AFSingletons.ENERGY_PROCESSOR_PRINT, 64)
                .require(Tags.Items.DUSTS_REDSTONE, 64)
                .require(AEItems.SILICON_PRINT, 64)
                .save(compatOut);
    }
}