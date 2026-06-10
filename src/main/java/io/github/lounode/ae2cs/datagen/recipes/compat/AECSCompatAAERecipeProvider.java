package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
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
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.pedroksl.advanced_ae.common.definitions.AAEFluids;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
import net.pedroksl.advanced_ae.datagen.AAEConventionTags;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.neoforged.neoforge.common.conditions.NeoForgeConditions.modLoaded;

public class AECSCompatAAERecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAAERecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        var compatOut = this.output.withConditions(modLoaded(AECSConstants.AAE_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_QUANTUM_CRYSTAL, AECSBlocks.PURE_QUANTUM_CRYSTAL_BLOCK);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AAEItems.QUANTUM_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);


        CrystalAggregatorRecipeBuilder.aggregating(AAEItems.QUANTUM_ALLOY.stack(), 16000)
                .require(AECSTags.Items.DUST_QUANTUM_ALLOY, 1)
                .require(Tags.Items.INGOTS, 1)
                .save(compatOut);

        // TODO AAE目前不支持tag
        ReactionChamberRecipeBuilder.react(AECSItems.QUANTUM_CRYSTAL_SEED, 4, 80000)
                .input(AEItems.SKY_DUST)
                .input(AECSItems.QUANTUM_CRYSTAL_DUST)
                .input(AECSItems.NETHER_QUARTZ_DUST)
                .fluid(AAEFluids.QUANTUM_INFUSION.source(),1000)
                .save(compatOut, getReactionPath(AECSItems.QUANTUM_CRYSTAL_SEED));

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.QUANTUM_CRYSTAL_DUST, 1, 8000)
                .require(AAEItems.QUANTUM_ALLOY, 1)
                .save(compatOut, "quantum_crystal_dust_from_ingot");

        CrystalPulverizerRecipeBuilder.pulverizing(AAEItems.QUANTUM_INFUSED_DUST, 1, 8000)
                .require(AAEItems.SHATTERED_SINGULARITY, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AAEItems.QUANTUM_PROCESSOR, 32, 51200)
                .require(AAEItems.QUANTUM_PROCESSOR_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(AAEItems.QUANTUM_PROCESSOR, 9, 14400)
                .require(AAEConventionTags.QUANTUM_ALLOY_STORAGE_BLOCK_ITEM, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);
    }

    protected static Identifier getReactionPath(ItemLike output)
    {
        return AE2CrystalScience.makeId(getPrefixedItemName("reaction", output));
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
            return new AECSCompatAAERecipeProvider(provider, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "AECS AAE Compat Recipes";
        }
    }
}