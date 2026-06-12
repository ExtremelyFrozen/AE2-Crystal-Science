package io.github.lounode.ae2cs.common.recipe;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class ResonatingPatternUpgradeRecipe extends CustomRecipe {

    public ResonatingPatternUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        ItemStack pattern = ItemStack.EMPTY;
        ItemStack crystal = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            var s = input.getItem(i);
            if (s.isEmpty()) continue;

            if (isEncodedProcessingPattern(s)) {
                if (!pattern.isEmpty()) return false;
                pattern = s;
            } else if (isResonatingDust(s)) {
                if (!crystal.isEmpty()) return false;
                crystal = s;
            } else {
                return false;
            }
        }

        return !pattern.isEmpty() && !crystal.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack processingPattern = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            var s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (isEncodedProcessingPattern(s)) {
                processingPattern = s;
                break;
            }
        }

        if (processingPattern.isEmpty()) return ItemStack.EMPTY;

        return ResonatingPatternDetails.encode(processingPattern);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.RESONATING_PATTERN_UPGRADE.get();
    }

    private static boolean isEncodedProcessingPattern(ItemStack stack) {
        return stack.is(AEItems.PROCESSING_PATTERN.asItem()) && stack.has(AEComponents.ENCODED_PROCESSING_PATTERN);
    }

    private static boolean isResonatingDust(ItemStack stack) {
        return stack.is(AECSTags.Items.DUST_RESONATING);
    }
}
