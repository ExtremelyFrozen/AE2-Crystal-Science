package io.github.lounode.ae2cs.common.recipe.circuit_etcher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class CircuitEtcherRecipeSerializer
{

    // 缺省值
    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.of(), 1);

    public static final MapCodec<CircuitEtcherRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_a", EMPTY).forGetter(CircuitEtcherRecipe::inputA),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_b", EMPTY).forGetter(CircuitEtcherRecipe::inputB),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_c", EMPTY).forGetter(CircuitEtcherRecipe::inputC),
            ItemStack.CODEC.fieldOf("result").forGetter(CircuitEtcherRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 3200).forGetter(CircuitEtcherRecipe::energyCost)
    ).apply(inst, CircuitEtcherRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitEtcherRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, CircuitEtcherRecipe::inputA,
                    SizedIngredient.STREAM_CODEC, CircuitEtcherRecipe::inputB,
                    SizedIngredient.STREAM_CODEC, CircuitEtcherRecipe::inputC,
                    ItemStack.STREAM_CODEC, CircuitEtcherRecipe::result,
                    ByteBufCodecs.VAR_INT, CircuitEtcherRecipe::energyCost,
                    CircuitEtcherRecipe::new
            );
}
