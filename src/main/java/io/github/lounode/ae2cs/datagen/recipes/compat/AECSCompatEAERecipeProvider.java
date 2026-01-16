package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.util.EAETags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatEAERecipeProvider extends AECSRecipeProvider
{
    public AECSCompatEAERecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS EAE Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.EAE_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, EAESingletons.CONCURRENT_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CircuitEtcherRecipeBuilder.etching(EAESingletons.CONCURRENT_PROCESSOR, 36, 57600)
                .require(EAETags.ENTRO_BLOCK, 9)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MOSTLY_ENTROIZED_FLUIX_BUDDING, 1, 16000)
                .require(AECSItems.entroCrystalSeed, 1)
                .require(AECSItems.pureResonatingCrystal, 1)
                .require(AEBlocks.FLUIX_BLOCK, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.entroCrystalSeed, 4, 16000)
                .require(EAETags.ENTRO_DUST, 1)
                .require(AECSTags.Items.DUST_QUARTZ, 3)
                .require(ConventionTags.SKY_STONE_DUST, 1)
                .save(compatOut, "aggregator/" + getItemName(AECSItems.entroCrystalSeed) + "_from_dust");

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.entroCrystalSeed, 6, 16000)
                .require(EAESingletons.ENTRO_SEED, 2)
                .require(AECSTags.Items.DUST_QUARTZ, 4)
                .save(compatOut, "aggregator/" + getItemName(AECSItems.entroCrystalSeed) + "_from_original_seed");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.EX_ASSEMBLER, 1, 32000)
                .require(AEBlocks.MOLECULAR_ASSEMBLER, 4)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEItems.SPEED_CARD, 2)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MACHINE_FRAME, 1, 32000)
                .require(EAETags.ENTRO_INGOT, 4)
                .require(AEBlocks.QUARTZ_GLASS, 1)
                .require(Tags.Items.INGOTS_IRON, 4)
                .save(compatOut, "aggregator/" + getItemName(EAESingletons.MACHINE_FRAME) + "_from_entro_ingot");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MACHINE_FRAME, 1, 32000)
                .require(AECSItems.pureEntroCrystal, 4)
                .require(AEBlocks.QUARTZ_GLASS, 1)
                .require(Tags.Items.INGOTS_IRON, 4)
                .save(compatOut, "aggregator/" + getItemName(EAESingletons.MACHINE_FRAME) + "_from_pure_entro_crystal");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.WIRELESS_HUB, 1, 32000)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEBlocks.QUANTUM_LINK, 1)
                .require(EAESingletons.WIRELESS_CONNECTOR, 1)
                .save(compatOut);
    }
}