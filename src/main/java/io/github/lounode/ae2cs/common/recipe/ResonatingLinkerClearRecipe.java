package io.github.lounode.ae2cs.common.recipe;

import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.item.ResonatingLinkerItem;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class ResonatingLinkerClearRecipe extends CustomRecipe {

    public ResonatingLinkerClearRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, @NotNull Level level) {
        ItemStack linker = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.is(AECSItems.RESONATING_LINKER.get()) || !linker.isEmpty()) {
                return false;
            }

            linker = stack;
        }

        return !linker.isEmpty() && ResonatingLinkerItem.hasStoredTargets(linker);
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer input, @NotNull RegistryAccess registries) {
        return AECSItems.RESONATING_LINKER.toStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.RESONATING_LINKER_CLEAR.get();
    }
}
