package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class CrystalPulverizerRecipeSerializer
{
    public static final MapCodec<CrystalPulverizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.NESTED_CODEC.fieldOf("input").forGetter(CrystalPulverizerRecipe::input),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrystalPulverizerRecipe::result),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(CrystalPulverizerRecipe::energyCost)
    ).apply(inst, CrystalPulverizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalPulverizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, CrystalPulverizerRecipe::input,
                    ItemStackTemplate.STREAM_CODEC, CrystalPulverizerRecipe::result,
                    ByteBufCodecs.VAR_INT, CrystalPulverizerRecipe::energyCost,
                    CrystalPulverizerRecipe::new
            );
}
