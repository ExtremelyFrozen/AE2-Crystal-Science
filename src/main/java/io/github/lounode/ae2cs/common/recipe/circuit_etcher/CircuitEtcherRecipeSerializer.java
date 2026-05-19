package io.github.lounode.ae2cs.common.recipe.circuit_etcher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Optional;

public class CircuitEtcherRecipeSerializer
{

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<SizedIngredient>> OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC =
            ByteBufCodecs.optional(SizedIngredient.STREAM_CODEC);

    public static final MapCodec<CircuitEtcherRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_a").forGetter(recipe -> optional(recipe.inputA())),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_b").forGetter(recipe -> optional(recipe.inputB())),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_c").forGetter(recipe -> optional(recipe.inputC())),
            ItemStack.CODEC.fieldOf("result").forGetter(CircuitEtcherRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 3200).forGetter(CircuitEtcherRecipe::energyCost)
    ).apply(inst, CircuitEtcherRecipeSerializer::create));

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitEtcherRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputA()),
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputB()),
                    OPTIONAL_SIZED_INGREDIENT_STREAM_CODEC, recipe -> optional(recipe.inputC()),
                    ItemStack.STREAM_CODEC, CircuitEtcherRecipe::result,
                    ByteBufCodecs.VAR_INT, CircuitEtcherRecipe::energyCost,
                    CircuitEtcherRecipeSerializer::create
            );

    private static Optional<SizedIngredient> optional(SizedIngredient ingredient)
    {
        return Optional.ofNullable(ingredient);
    }

    private static CircuitEtcherRecipe create(Optional<SizedIngredient> inputA, Optional<SizedIngredient> inputB,
                                             Optional<SizedIngredient> inputC, ItemStack result, int energyCost)
    {
        return new CircuitEtcherRecipe(inputA.orElse(null), inputB.orElse(null), inputC.orElse(null), result, energyCost);
    }
}
