package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import com.fish_dan_.data_energistics.recipe.DataRipperReassemblerIngredient;
import com.fish_dan_.data_energistics.recipe.DataRipperReassemblerRecipe;
import com.glodblock.github.extendedae.util.EAETags;
import com.wintercogs.ae2omnicells.common.init.OCBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 为 Data Energistics 的数据水晶补充谐振晶体生长链配方。
 */
public class AECSCompatDataEnergisticsRecipeProvider extends AECSRecipeProvider {

    public AECSCompatDataEnergisticsRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS DataEnergistics Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        RecipeOutput compatOut = originalOut.withConditions(modLoaded(AECSConstants.DATA_ENERGISTICS_ID));
        Item dataDust = externalItem("data_dust");
        Item dataCircuitBoard = externalItem("data_circuit_board");
        Item dataProcessor = externalItem("data_processor");

        var extendedAeOut = compatOut.withConditions(modLoaded(AECSConstants.EAE_ID));
        var omniOut = extendedAeOut.withConditions(modLoaded(AECSConstants.OMNI_CELL_ID));

        CircuitEtcherRecipeBuilder.etching(dataProcessor, 36, 57600)
                .require(EAETags.ENTRO_BLOCK, 9)
                .require(OCBlocks.SINGULARITY_BLOCK, 8)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(omniOut, "circuit_etcher/data_processor_from_omni_singularity_block");

        CircuitEtcherRecipeBuilder.etching(dataProcessor, 36, 57600)
                .require(EAETags.ENTRO_BLOCK, 9)
                .require(AEItems.QUANTUM_ENTANGLED_SINGULARITY, 36)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(extendedAeOut, "circuit_etcher/data_processor_from_quantum_entangled_singularity");

        CrystalAggregatorRecipeBuilder.aggregating(dataProcessor, 32, 51200)
                .require(dataCircuitBoard, 32)
                .require(AEItems.QUANTUM_ENTANGLED_SINGULARITY, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut, "aggregator/data_processor");

        var dataSeedRecipe = new DataRipperReassemblerRecipe(
                List.of(
                        new DataRipperReassemblerIngredient(Ingredient.of(dataDust), 8),
                        new DataRipperReassemblerIngredient(Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED), 4),
                        new DataRipperReassemblerIngredient(Ingredient.of(Blocks.SAND), 16)),
                List.of(),
                List.of(AECSItems.DATA_CRYSTAL_SEED.toStack(32)),
                List.of(),
                200,
                null,
                null);
        compatOut.accept(AE2CrystalScience.makeId("data_reassembler/data_crystal_seed"), dataSeedRecipe, null);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.DATA_CRYSTAL_SEED, 32, 51200)
                .require(dataDust, 8)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 8)
                .require(Blocks.SAND, 32)
                .save(compatOut, "aggregator/data_crystal_seed");

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC,
                externalItem("data_inscriber_template"), AECSItems.BLANK_PRINT_PRESS);

        CrystalPulverizerRecipeBuilder.pulverizing(externalItem("obsidian_dust"), 4, 8000)
                .require(Tags.Items.OBSIDIANS, 1)
                .save(compatOut, "pulverizer/data_energistics_obsidian_dust");

        CrystalPulverizerRecipeBuilder.pulverizing(dataDust, 1, 8000)
                .require(AECSTags.Items.GEM_DATA_CRYSTAL, 1)
                .save(compatOut, "pulverizer/data_dust_from_data_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(dataDust, 1, 8000)
                .require(AECSItems.PURE_DATA_CRYSTAL, 1)
                .save(compatOut, "pulverizer/data_dust_from_pure_data_crystal");
    }

    private static Item externalItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(AECSConstants.DATA_ENERGISTICS_ID, path));
    }
}
