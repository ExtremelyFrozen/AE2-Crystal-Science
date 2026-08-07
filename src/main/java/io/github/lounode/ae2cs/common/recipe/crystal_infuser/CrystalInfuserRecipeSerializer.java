package io.github.lounode.ae2cs.common.recipe.crystal_infuser;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CrystalInfuserRecipeSerializer implements RecipeSerializer<CrystalInfuserRecipe> {

    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    public static final MapCodec<CrystalInfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_a", EMPTY).forGetter(CrystalInfuserRecipe::inputA),
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_b", EMPTY).forGetter(CrystalInfuserRecipe::inputB),
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_c", EMPTY).forGetter(CrystalInfuserRecipe::inputC),
            SizedIngredient.FLAT_CODEC.optionalFieldOf("input_d", EMPTY).forGetter(CrystalInfuserRecipe::inputD),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystalInfuserRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalInfuserRecipe::energyCost))
            .apply(inst, CrystalInfuserRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalInfuserRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC, CrystalInfuserRecipe::inputA,
            SizedIngredient.STREAM_CODEC, CrystalInfuserRecipe::inputB,
            SizedIngredient.STREAM_CODEC, CrystalInfuserRecipe::inputC,
            SizedIngredient.STREAM_CODEC, CrystalInfuserRecipe::inputD,
            ItemStack.STREAM_CODEC, CrystalInfuserRecipe::result,
            ByteBufCodecs.VAR_INT, CrystalInfuserRecipe::energyCost,
            CrystalInfuserRecipe::new);

    @Override
    public @NotNull MapCodec<CrystalInfuserRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CrystalInfuserRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
