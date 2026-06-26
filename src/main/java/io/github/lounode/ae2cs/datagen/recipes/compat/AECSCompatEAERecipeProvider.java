package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSCompatEAERecipeProvider extends AECSRecipeProvider {

    public AECSCompatEAERecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS EAE Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut) {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.EAE_ID));

        CrystalAggregatorRecipeBuilder.aggregating(EPPItemAndBlock.EX_ASSEMBLER, 1, 16000)
                .require(AEBlocks.MOLECULAR_ASSEMBLER, 4)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEItems.SPEED_CARD, 2)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(EPPItemAndBlock.WIRELESS_HUB, 1, 16000)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEBlocks.QUANTUM_LINK, 1)
                .require(EPPItemAndBlock.WIRELESS_CONNECTOR, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(EPPItemAndBlock.EX_PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(EPPItemAndBlock.EX_INTERFACE, 1)
                .save(compatOut, "aggregator/" + getItemName(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK) + "_from_extended_pattern_and_interface");
    }
}
