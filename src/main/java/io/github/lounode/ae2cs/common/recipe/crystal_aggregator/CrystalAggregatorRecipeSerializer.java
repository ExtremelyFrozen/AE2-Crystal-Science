package io.github.lounode.ae2cs.common.recipe.crystal_aggregator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class CrystalAggregatorRecipeSerializer
{

    // 缺省值
    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.of(), 1);

    public static final MapCodec<CrystalAggregatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_a", EMPTY).forGetter(CrystalAggregatorRecipe::inputA),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_b", EMPTY).forGetter(CrystalAggregatorRecipe::inputB),
            SizedIngredient.NESTED_CODEC.optionalFieldOf("input_c", EMPTY).forGetter(CrystalAggregatorRecipe::inputC),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystalAggregatorRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalAggregatorRecipe::energyCost)
    ).apply(inst, CrystalAggregatorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalAggregatorRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, CrystalAggregatorRecipe::inputA,
                    SizedIngredient.STREAM_CODEC, CrystalAggregatorRecipe::inputB,
                    SizedIngredient.STREAM_CODEC, CrystalAggregatorRecipe::inputC,
                    ItemStack.STREAM_CODEC, CrystalAggregatorRecipe::result,
                    ByteBufCodecs.VAR_INT, CrystalAggregatorRecipe::energyCost,
                    CrystalAggregatorRecipe::new
            );
}