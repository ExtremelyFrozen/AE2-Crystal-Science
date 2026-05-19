package io.github.lounode.ae2cs.integration.jei;

import appeng.client.integrations.jei.EntropyManipulatorCategory;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    @Override
    public @NotNull Identifier getPluginUid()
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
        registration.addRecipeCategories(new CrystalGrowthCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration)
    {
        var server = ServerLifecycleHooks.getCurrentServer();

        var recipeMap = server == null ? RecipeCache.getRecipeMap() : server.getRecipeManager().recipeMap();

        {
            List<RecipeHolder<CircuitEtcherRecipe>> recipes = recipeMap
                    .byType(AECSRecipeTypes.CIRCUIT_ETCHER.get())
                    .stream()
                    .toList();

            registration.addRecipes(CircuitEtcherRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<RecipeHolder<CrystalAggregatorRecipe>> recipes = recipeMap
                    .byType(AECSRecipeTypes.CRYSTAL_AGGREGATOR.get())
                    .stream()
                    .toList();

            registration.addRecipes(CrystalAggregatorRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<RecipeHolder<CrystalPulverizerRecipe>> recipes = recipeMap
                    .byType(AECSRecipeTypes.CRYSTAL_PULVERIZER.get())
                    .stream()
                    .toList();

            registration.addRecipes(CrystalPulverizerRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            registration.addRecipes(CrystalGrowthCategory.RECIPE_TYPE, AECSItems.getCrystalSeeds().stream().map(DeferredHolder::get).toList());
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration)
    {
        registration.addCraftingStation(
                CircuitEtcherRecipeCategory.RECIPE_TYPE,
                AECSBlocks.CIRCUIT_ETCHER_BLOCK
        );

        registration.addCraftingStation(
                CrystalAggregatorRecipeCategory.RECIPE_TYPE,
                AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK
        );

        registration.addCraftingStation(
                CrystalPulverizerRecipeCategory.RECIPE_TYPE,
                AECSBlocks.CRYSTAL_PULVERIZER_BLOCK
        );
        registration.addCraftingStation(
                CrystalPulverizerRecipeCategory.RECIPE_TYPE,
                AECSBlocks.QUARTZ_GRINDSTONE_BLOCK
        );

        registration.addCraftingStation(
                CrystalGrowthCategory.RECIPE_TYPE,
                AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK
        );
        registration.addCraftingStation(
                CrystalGrowthCategory.RECIPE_TYPE,
                NeoForgeTypes.FLUID_STACK,
                new FluidStack(Fluids.WATER, 1000)
        );

        registration.addCraftingStation(
                EntropyManipulatorCategory.TYPE,
                AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK
        );

    }

}
