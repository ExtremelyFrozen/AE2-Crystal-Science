package io.github.lounode.ae2cs.common.recipe.crystal_aggregator;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CrystalAggregatorRecipeSerializer implements RecipeSerializer<CrystalAggregatorRecipe> {

    // 缺省值
    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    public static final MapCodec<CrystalAggregatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_a", EMPTY).forGetter(CrystalAggregatorRecipe::inputA),
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_b", EMPTY).forGetter(CrystalAggregatorRecipe::inputB),
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_c", EMPTY).forGetter(CrystalAggregatorRecipe::inputC),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystalAggregatorRecipe::result),
            SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid_input").forGetter(r -> java.util.Optional.ofNullable(r.fluidInput())),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_output").forGetter(r -> r.fluidOutput().isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(r.fluidOutput())),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalAggregatorRecipe::energyCost)).apply(inst,
                    (a, b, c, result, fluidInput, fluidOutput, energyCost) -> new CrystalAggregatorRecipe(
                            a, b, c, result, fluidInput.orElse(null), fluidOutput.orElse(FluidStack.EMPTY), energyCost)));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalAggregatorRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                SizedIngredient.STREAM_CODEC.encode(buf, recipe.inputA());
                SizedIngredient.STREAM_CODEC.encode(buf, recipe.inputB());
                SizedIngredient.STREAM_CODEC.encode(buf, recipe.inputC());
                ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).encode(buf, java.util.Optional.ofNullable(recipe.fluidInput()));
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.fluidOutput());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.energyCost());
            }, buf -> new CrystalAggregatorRecipe(
                    SizedIngredient.STREAM_CODEC.decode(buf), SizedIngredient.STREAM_CODEC.decode(buf),
                    SizedIngredient.STREAM_CODEC.decode(buf), ItemStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).decode(buf).orElse(null),
                    FluidStack.OPTIONAL_STREAM_CODEC.decode(buf), ByteBufCodecs.VAR_INT.decode(buf)));

    @Override
    public @NotNull MapCodec<CrystalAggregatorRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CrystalAggregatorRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
