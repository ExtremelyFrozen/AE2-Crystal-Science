package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipeSerializer implements RecipeSerializer<CrystalPulverizerRecipe> {

    public static final MapCodec<CrystalPulverizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(CrystalPulverizerRecipe::input),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystalPulverizerRecipe::result),
            SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid_input").forGetter(r -> java.util.Optional.ofNullable(r.fluidInput())),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_output").forGetter(r -> r.fluidOutput().isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(r.fluidOutput())),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalPulverizerRecipe::energyCost)).apply(inst,
                    (input, result, fluidInput, fluidOutput, energyCost) -> new CrystalPulverizerRecipe(
                            input, result, fluidInput.orElse(null), fluidOutput.orElse(FluidStack.EMPTY), energyCost)));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalPulverizerRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                SizedIngredient.STREAM_CODEC.encode(buf, recipe.input());
                ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).encode(buf, java.util.Optional.ofNullable(recipe.fluidInput()));
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.fluidOutput());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.energyCost());
            }, buf -> new CrystalPulverizerRecipe(
                    SizedIngredient.STREAM_CODEC.decode(buf), ItemStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).decode(buf).orElse(null),
                    FluidStack.OPTIONAL_STREAM_CODEC.decode(buf), ByteBufCodecs.VAR_INT.decode(buf)));

    @Override
    public @NotNull MapCodec<CrystalPulverizerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CrystalPulverizerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
