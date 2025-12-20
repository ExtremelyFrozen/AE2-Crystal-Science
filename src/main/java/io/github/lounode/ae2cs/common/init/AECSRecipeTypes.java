package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AECSRecipeTypes
{
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AECSConstants.MODID);

    public static final Supplier<RecipeType<CircuitEtcherRecipe>> CIRCUIT_ETCHER =
            RECIPE_TYPES.register("circuit_etcher_recipe",
                    () -> RecipeType.simple(AE2CrystalScience.makeId("circuit_etcher_recipe")));

    public static final Supplier<RecipeType<CrystalAggregatorRecipe>> CRYSTAL_AGGREGATOR =
            RECIPE_TYPES.register("crystal_aggregator_recipe",
                    () -> RecipeType.simple(AE2CrystalScience.makeId("crystal_aggregator_recipe")));

    public static final Supplier<RecipeType<CrystalPulverizerRecipe>> CRYSTAL_PULVERIZER =
            RECIPE_TYPES.register("crystal_pulverizer_recipe",
                    () -> RecipeType.simple(AE2CrystalScience.makeId("crystal_pulverizer_recipe")));

    public static void register(IEventBus modEventBus)
    {
        RECIPE_TYPES.register(modEventBus);
    }
}
