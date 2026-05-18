package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.recipe.ResonatingPatternUpgradeRecipe;
import io.github.lounode.ae2cs.common.recipe.ResonatingPatternUpgradeRecipeSerializer;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipeSerializer;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipeSerializer;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AECSRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AECSConstants.MODID);

    public static final Supplier<RecipeSerializer<CircuitEtcherRecipe>> CIRCUIT_ETCHER =
            RECIPE_SERIALIZERS.register(
                    "circuit_etcher_recipe_serializer",
                    () -> new RecipeSerializer<>(
                            CircuitEtcherRecipeSerializer.CODEC,
                            CircuitEtcherRecipeSerializer.STREAM_CODEC
                    )
            );

    public static final Supplier<RecipeSerializer<CrystalAggregatorRecipe>> CRYSTAL_AGGREGATOR =
            RECIPE_SERIALIZERS.register(
                    "crystal_aggregator_recipe_serializer",
                    () -> new RecipeSerializer<>(
                            CrystalAggregatorRecipeSerializer.CODEC,
                            CrystalAggregatorRecipeSerializer.STREAM_CODEC
                    )
            );

    public static final Supplier<RecipeSerializer<CrystalPulverizerRecipe>> CRYSTAL_PULVERIZER =
            RECIPE_SERIALIZERS.register(
                    "crystal_pulverizer_recipe_serializer",
                    () -> new RecipeSerializer<>(
                            CrystalPulverizerRecipeSerializer.CODEC,
                            CrystalPulverizerRecipeSerializer.STREAM_CODEC
                    )
            );

    public static final Supplier<RecipeSerializer<ResonatingPatternUpgradeRecipe>> RESONATING_PATTERN_UPGRADE =
            RECIPE_SERIALIZERS.register(
                    "resonating_pattern_upgrade_recipe_serializer",
                    () -> new RecipeSerializer<>(
                            ResonatingPatternUpgradeRecipeSerializer.CODEC,
                            ResonatingPatternUpgradeRecipeSerializer.STREAM_CODEC
                    )
            );


    public static void register(IEventBus modEventBus)
    {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
