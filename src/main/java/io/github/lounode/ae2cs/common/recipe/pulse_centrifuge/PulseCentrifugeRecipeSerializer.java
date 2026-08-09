package io.github.lounode.ae2cs.common.recipe.pulse_centrifuge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PulseCentrifugeRecipeSerializer implements RecipeSerializer<PulseCentrifugeRecipe> {

    private static final Codec<List<ItemStack>> RESULTS_CODEC = ItemStack.CODEC.listOf().validate(results -> {
        if (results.isEmpty() || results.size() > 4) {
            return DataResult.error(() -> "Pulse centrifuge recipes require 1-4 results");
        }
        return DataResult.success(results);
    });

    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> RESULTS_STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(4));

    public static final MapCodec<PulseCentrifugeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(PulseCentrifugeRecipe::input),
            RESULTS_CODEC.fieldOf("results").forGetter(PulseCentrifugeRecipe::results),
            Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(PulseCentrifugeRecipe::energyCost))
            .apply(instance, PulseCentrifugeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PulseCentrifugeRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC, PulseCentrifugeRecipe::input,
            RESULTS_STREAM_CODEC, PulseCentrifugeRecipe::results,
            ByteBufCodecs.VAR_INT, PulseCentrifugeRecipe::energyCost,
            PulseCentrifugeRecipe::new);

    @Override
    public @NotNull MapCodec<PulseCentrifugeRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, PulseCentrifugeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
