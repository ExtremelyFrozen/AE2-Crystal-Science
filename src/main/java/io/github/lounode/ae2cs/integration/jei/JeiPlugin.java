package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.menu.CircuitEtcherMenu;
import io.github.lounode.ae2cs.common.menu.CrystalAggregatorMenu;
import io.github.lounode.ae2cs.common.menu.CrystalInfuserMenu;
import io.github.lounode.ae2cs.common.menu.CrystalPulverizerMenu;
import io.github.lounode.ae2cs.common.menu.EntropyVariationReactionChamberMenu;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_infuser.CrystalInfuserRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import io.github.lounode.ae2cs.integration.RecipeViewerNavigation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import org.jetbrains.annotations.NotNull;
import tamaized.ae2jeiintegration.integration.modules.jei.categories.EntropyManipulatorCategory;

import java.util.List;
import java.util.Objects;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return AE2CrystalScience.makeId("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);
        registration.addRecipeCategories(new CircuitEtcherRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalAggregatorRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalPulverizerRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalInfuserRecipeCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new CrystalGrowthCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        {
            List<RecipeHolder<CircuitEtcherRecipe>> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CIRCUIT_ETCHER.get())
                    .stream()
                    .toList();

            registration.addRecipes(CircuitEtcherRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<RecipeHolder<CrystalAggregatorRecipe>> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CRYSTAL_AGGREGATOR.get())
                    .stream()
                    .toList();

            registration.addRecipes(CrystalAggregatorRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<RecipeHolder<CrystalPulverizerRecipe>> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CRYSTAL_PULVERIZER.get())
                    .stream()
                    .toList();

            registration.addRecipes(CrystalPulverizerRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            List<RecipeHolder<CrystalInfuserRecipe>> recipes = level.getRecipeManager()
                    .getAllRecipesFor(AECSRecipeTypes.CRYSTAL_INFUSER.get())
                    .stream()
                    .toList();

            registration.addRecipes(CrystalInfuserRecipeCategory.RECIPE_TYPE, recipes);
        }

        {
            registration.addRecipes(CrystalGrowthCategory.RECIPE_TYPE, AECSItems.getCrystalSeeds().stream().map(DeferredHolder::get).toList());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new MachineRecipeTransferInfo<>(
                CircuitEtcherMenu.class,
                AECSMenus.CIRCUIT_ETCHER_MENU.get(),
                CircuitEtcherRecipeCategory.RECIPE_TYPE));
        registration.addRecipeTransferHandler(new MachineRecipeTransferInfo<>(
                CrystalAggregatorMenu.class,
                AECSMenus.CRYSTAL_AGGREGATOR_MENU.get(),
                CrystalAggregatorRecipeCategory.RECIPE_TYPE));
        registration.addRecipeTransferHandler(new MachineRecipeTransferInfo<>(
                CrystalPulverizerMenu.class,
                AECSMenus.CRYSTAL_PULVERIZER_MENU.get(),
                CrystalPulverizerRecipeCategory.RECIPE_TYPE));
        registration.addRecipeTransferHandler(new MachineRecipeTransferInfo<>(
                CrystalInfuserMenu.class,
                AECSMenus.CRYSTAL_INFUSER_MENU.get(),
                CrystalInfuserRecipeCategory.RECIPE_TYPE));

        if (ModList.get().isLoaded(AECSConstants.JEI_AE_INTEGRATION_ID)) {
            registration.addRecipeTransferHandler(new MachineRecipeTransferInfo<>(
                    EntropyVariationReactionChamberMenu.class,
                    AECSMenus.ENTROPY_VARIATION_REACTION_CHAMBER_MENU.get(),
                    EntropyManipulatorCategory.RECIPE_TYPE));

            var jeiHelpers = registration.getJeiHelpers();
            var menuType = Objects.requireNonNull(AECSMenus.RESONANT_TEMPLATE_CODING_TERM_MENU.get());
            var transferHelper = Objects.requireNonNull(registration.getTransferHelper());
            var ingredientVisibility = Objects.requireNonNull(jeiHelpers.getIngredientVisibility());
            registration.addUniversalRecipeTransferHandler(new ResonantEncodePatternTransferHandler(
                    menuType,
                    transferHelper,
                    ingredientVisibility));
        }
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static void showRecipes(RecipeViewerNavigation.MachineCategory category) {
        if (runtime == null) {
            return;
        }

        var recipeType = switch (category) {
            case CIRCUIT_ETCHER -> CircuitEtcherRecipeCategory.RECIPE_TYPE;
            case CRYSTAL_AGGREGATOR -> CrystalAggregatorRecipeCategory.RECIPE_TYPE;
            case CRYSTAL_PULVERIZER -> CrystalPulverizerRecipeCategory.RECIPE_TYPE;
            case CRYSTAL_INFUSER -> CrystalInfuserRecipeCategory.RECIPE_TYPE;
            case ENTROPY_REACTION -> ModList.get().isLoaded(AECSConstants.JEI_AE_INTEGRATION_ID) ? EntropyManipulatorCategory.RECIPE_TYPE : null;
        };
        if (recipeType != null) {
            runtime.getRecipesGui().showTypes(List.of(recipeType));
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                AECSBlocks.CIRCUIT_ETCHER_BLOCK,
                CircuitEtcherRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK,
                CrystalAggregatorRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_PULVERIZER_BLOCK,
                CrystalPulverizerRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_INFUSER_BLOCK,
                CrystalInfuserRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(
                AECSBlocks.QUARTZ_GRINDSTONE_BLOCK,
                CrystalPulverizerRecipeCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(
                AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK,
                CrystalGrowthCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(
                NeoForgeTypes.FLUID_STACK,
                new FluidStack(Fluids.WATER, 1000),
                CrystalGrowthCategory.RECIPE_TYPE);

        if (ModList.get().isLoaded(AECSConstants.JEI_AE_INTEGRATION_ID)) {
            registration.addRecipeCatalyst(
                    AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK,
                    EntropyManipulatorCategory.RECIPE_TYPE);
        }
    }
}
