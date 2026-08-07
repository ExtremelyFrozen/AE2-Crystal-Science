package io.github.lounode.ae2cs.integration;

import io.github.lounode.ae2cs.integration.emi.CircuitEtcherRecipeCategory;
import io.github.lounode.ae2cs.integration.emi.CrystalAggregatorRecipeCategory;
import io.github.lounode.ae2cs.integration.emi.CrystalInfuserRecipeCategory;
import io.github.lounode.ae2cs.integration.emi.CrystalPulverizerRecipeCategory;
import io.github.lounode.ae2cs.integration.emi.EntropyVariationReactionChamberRecipeCategory;
import io.github.lounode.ae2cs.integration.jei.JeiPlugin;

import net.neoforged.fml.ModList;

import dev.emi.emi.api.EmiApi;

public final class RecipeViewerNavigation {

    private RecipeViewerNavigation() {}

    public static void show(MachineCategory category) {
        if (ModList.get().isLoaded("emi")) {
            switch (category) {
                case CIRCUIT_ETCHER -> EmiApi.displayRecipeCategory(CircuitEtcherRecipeCategory.RECIPE_TYPE);
                case CRYSTAL_AGGREGATOR -> EmiApi.displayRecipeCategory(CrystalAggregatorRecipeCategory.RECIPE_TYPE);
                case CRYSTAL_PULVERIZER -> EmiApi.displayRecipeCategory(CrystalPulverizerRecipeCategory.RECIPE_TYPE);
                case CRYSTAL_INFUSER -> EmiApi.displayRecipeCategory(CrystalInfuserRecipeCategory.RECIPE_TYPE);
                case ENTROPY_REACTION -> EmiApi.displayRecipeCategory(EntropyVariationReactionChamberRecipeCategory.RECIPE_TYPE);
            }
            return;
        }

        if (ModList.get().isLoaded("jei")) {
            JeiPlugin.showRecipes(category);
        }
    }

    public enum MachineCategory {
        CIRCUIT_ETCHER,
        CRYSTAL_AGGREGATOR,
        CRYSTAL_PULVERIZER,
        CRYSTAL_INFUSER,
        ENTROPY_REACTION
    }
}
