package io.github.lounode.ae2cs.common.recipe.crystal_aggregator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Optional;

public class CrystalAggregatorRecipeSerializer
{

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<SizedIngredient>> OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC =
            ByteBufCodecs.optional(SizedIngredient.STREAM_CODEC);

    public static final MapCodec<CrystalAggregatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_a").forGetter(recipe -> optional(recipe.inputA())),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_b").forGetter(recipe -> optional(recipe.inputB())),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_c").forGetter(recipe -> optional(recipe.inputC())),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrystalAggregatorRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalAggregatorRecipe::energyCost)
    ).apply(inst, CrystalAggregatorRecipeSerializer::create));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalAggregatorRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputA()),
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputB()),
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputC()),
                    ItemStackTemplate.STREAM_CODEC, CrystalAggregatorRecipe::result,
                    ByteBufCodecs.VAR_INT, CrystalAggregatorRecipe::energyCost,
                    CrystalAggregatorRecipeSerializer::create
            );

    private static Optional<SizedIngredient> optional(SizedIngredient ingredient)
    {
        return Optional.ofNullable(ingredient);
    }

    private static CrystalAggregatorRecipe create(Optional<SizedIngredient> inputA, Optional<SizedIngredient> inputB,
                                                  Optional<SizedIngredient> inputC, ItemStackTemplate result, int energyCost)
    {
        return new CrystalAggregatorRecipe(inputA.orElse(null), inputB.orElse(null), inputC.orElse(null), result, energyCost);
    }
}
