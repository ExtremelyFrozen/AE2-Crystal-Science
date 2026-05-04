package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.recipe.ResonatingLinkerClearRecipe;
import io.github.lounode.ae2cs.common.recipe.ResonatingPatternUpgradeRecipe;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipeSerializer;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipeSerializer;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class AECSRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AECSConstants.MODID);

    public static final Supplier<RecipeSerializer<CircuitEtcherRecipe>> CIRCUIT_ETCHER =
            RECIPE_SERIALIZERS.register("circuit_etcher_recipe_serializer", CircuitEtcherRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<CrystalAggregatorRecipe>> CRYSTAL_AGGREGATOR =
            RECIPE_SERIALIZERS.register("crystal_aggregator_recipe_serializer", CrystalAggregatorRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<CrystalPulverizerRecipe>> CRYSTAL_PULVERIZER =
            RECIPE_SERIALIZERS.register("crystal_pulverizer_recipe_serializer", CrystalPulverizerRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<ResonatingPatternUpgradeRecipe>> RESONATING_PATTERN_UPGRADE =
            RECIPE_SERIALIZERS.register("resonating_pattern_upgrade", () -> new SimpleCraftingRecipeSerializer<>(ResonatingPatternUpgradeRecipe::new));

    public static final Supplier<RecipeSerializer<ResonatingLinkerClearRecipe>> RESONATING_LINKER_CLEAR =
            RECIPE_SERIALIZERS.register("resonating_linker_clear", () -> new SimpleCraftingRecipeSerializer<>(ResonatingLinkerClearRecipe::new));


    public static void register(IEventBus modEventBus)
    {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
