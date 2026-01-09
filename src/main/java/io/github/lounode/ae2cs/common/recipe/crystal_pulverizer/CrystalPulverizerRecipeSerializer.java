package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipeSerializer implements RecipeSerializer<CrystalPulverizerRecipe>
{
    public static final MapCodec<CrystalPulverizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(CrystalPulverizerRecipe::input),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystalPulverizerRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalPulverizerRecipe::energyCost)
    ).apply(inst, CrystalPulverizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalPulverizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, CrystalPulverizerRecipe::input,
                    ItemStack.STREAM_CODEC, CrystalPulverizerRecipe::result,
                    ByteBufCodecs.VAR_INT, CrystalPulverizerRecipe::energyCost,
                    CrystalPulverizerRecipe::new
            );

    @Override
    public @NotNull MapCodec<CrystalPulverizerRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CrystalPulverizerRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
