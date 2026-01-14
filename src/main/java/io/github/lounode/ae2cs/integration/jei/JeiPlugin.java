package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import tamaized.ae2jeiintegration.integration.modules.jei.categories.EntropyManipulatorCategory;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    @Override
    public @NotNull ResourceLocation getPluginUid()
    {
        return AE2CrystalScience.makeId("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration)
    {
        IModPlugin.super.registerCategories(registration);
        registration.addRecipeCategories(new CircuitEtcherRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalAggregatorRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalPulverizerRecipeCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration)
    {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        {
            List<CircuitEtcherRecipe> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CIRCUIT_ETCHER.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(CircuitEtcherRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<CrystalAggregatorRecipe> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CRYSTAL_AGGREGATOR.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(CrystalAggregatorRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<CrystalPulverizerRecipe> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CRYSTAL_PULVERIZER.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(CrystalPulverizerRecipeCategory.RECIPE_TYPE, recipes);
        }


    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(
                AECSBlocks.CIRCUIT_ETCHER_BLOCK,
                CircuitEtcherRecipeCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK,
                CrystalAggregatorRecipeCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_PULVERIZER_BLOCK,
                CrystalPulverizerRecipeCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                AECSBlocks.QUARTZ_GRINDSTONE_BLOCK,
                CrystalPulverizerRecipeCategory.RECIPE_TYPE
        );

        if (ModList.get().isLoaded(AECSConstants.JEI_AE_INTEGRATION_ID))
        {
            registration.addRecipeCatalyst(
                    AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK,
                    EntropyManipulatorCategory.RECIPE_TYPE
            );
        }
    }

}
