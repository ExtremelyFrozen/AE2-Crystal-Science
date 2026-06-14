package io.github.lounode.ae2cs.integration.emi;

import appeng.integration.modules.emi.EmiEntropyRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;
import net.minecraft.world.level.material.Fluids;

@EmiEntrypoint
public class EmiPlugin implements dev.emi.emi.api.EmiPlugin
{
    @Override
    public void register(EmiRegistry registry)
    {
        registry.addRecipeHandler(AECSMenus.RESONANT_TEMPLATE_CODING_TERM_MENU.get(),
                new ResonantEmiEncodePatternHandler());

        registry.addWorkstation(EmiEntropyRecipe.CATEGORY, EmiStack.of(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK));

        registry.addCategory(CircuitEtcherRecipeCategory.RECIPE_TYPE);
        registry.addWorkstation(CircuitEtcherRecipeCategory.RECIPE_TYPE, EmiStack.of(AECSBlocks.CIRCUIT_ETCHER_BLOCK));
        registry.getRecipeManager().getAllRecipesFor(AECSRecipeTypes.CIRCUIT_ETCHER.get())
                .stream()
                .map(CircuitEtcherRecipeCategory::new)
                .forEach(registry::addRecipe);

        registry.addCategory(CrystalAggregatorRecipeCategory.RECIPE_TYPE);
        registry.addWorkstation(CrystalAggregatorRecipeCategory.RECIPE_TYPE, EmiStack.of(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK));
        registry.getRecipeManager().getAllRecipesFor(AECSRecipeTypes.CRYSTAL_AGGREGATOR.get())
                .stream()
                .map(CrystalAggregatorRecipeCategory::new)
                .forEach(registry::addRecipe);

        registry.addCategory(CrystalPulverizerRecipeCategory.RECIPE_TYPE);
        registry.addWorkstation(CrystalPulverizerRecipeCategory.RECIPE_TYPE, EmiStack.of(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK));
        registry.addWorkstation(CrystalPulverizerRecipeCategory.RECIPE_TYPE, EmiStack.of(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK));
        registry.getRecipeManager().getAllRecipesFor(AECSRecipeTypes.CRYSTAL_PULVERIZER.get())
                .stream()
                .map(CrystalPulverizerRecipeCategory::new)
                .forEach(registry::addRecipe);

        registry.addCategory(CrystalGrowthCategory.RECIPE_TYPE);
        registry.addWorkstation(CrystalGrowthCategory.RECIPE_TYPE, EmiStack.of(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK));
        registry.addWorkstation(CrystalGrowthCategory.RECIPE_TYPE, EmiStack.of(Fluids.WATER));
        AECSItems.getCrystalSeeds()
                .stream()
                .map(crystalSeedItemDeferredItem -> new CrystalGrowthCategory(crystalSeedItemDeferredItem.get()))
                .forEach(registry::addRecipe);
    }
}
