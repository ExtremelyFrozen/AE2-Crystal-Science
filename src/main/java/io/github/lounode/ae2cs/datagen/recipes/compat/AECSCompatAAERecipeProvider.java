package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.pedroksl.advanced_ae.common.definitions.AAEFluids;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
import net.pedroksl.advanced_ae.datagen.AAEConventionTags;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipeBuilder;
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

        CircuitEtcherRecipeBuilder.etching(AAEItems.QUANTUM_PROCESSOR, 9, 14400)
                .require(AAEConventionTags.QUANTUM_ALLOY_STORAGE_BLOCK_ITEM, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AAEItems.QUANTUM_ALLOY.stack(), 16000)
                .require(AECSTags.Items.DUST_QUANTUM_ALLOY, 1)
                .require(Tags.Items.INGOTS, 1)
                .save(compatOut);

        ReactionChamberRecipeBuilder.react(AECSItems.quantumCrystalSeed, 4, 80000)
                .input(ConventionTags.SKY_STONE_DUST)
                .input(AECSTags.Items.DUST_QUANTUM_ALLOY)
                .input(AECSTags.Items.DUST_QUARTZ)
                .fluid(AAEFluids.QUANTUM_INFUSION.stack(1000))
                .save(compatOut, getReactionPath(AECSItems.quantumCrystalSeed));

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.QUANTUM_CRYSTAL_DUST, 1, 1600)
                .require(AAEItems.QUANTUM_ALLOY, 1)
                .save(compatOut, "quantum_crystal_dust_from_ingot");
    }

    protected static ResourceLocation getReactionPath(ItemLike output)
    {
        return AE2CrystalScience.makeId(getPrefixedItemName("reaction", output));
    }

}