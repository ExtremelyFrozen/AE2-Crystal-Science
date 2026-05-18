package io.github.lounode.ae2cs.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;

public class ResonatingPatternUpgradeRecipeSerializer {
    public static final MapCodec<ResonatingPatternUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ResonatingPatternUpgradeRecipe::category))
            .apply(inst, ResonatingPatternUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonatingPatternUpgradeRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    CraftingBookCategory.STREAM_CODEC, ResonatingPatternUpgradeRecipe::category,
                    ResonatingPatternUpgradeRecipe::new
            );
}
