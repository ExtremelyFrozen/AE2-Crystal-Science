package io.github.lounode.ae2cs.common.recipe;

import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.registration.WTDefinition;
import de.mari_023.ae2wtlib.wut.recipe.Common;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WirelessUniversalTerminalCombineRecipe extends CustomRecipe {

    public WirelessUniversalTerminalCombineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        return findInputs(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Inputs inputs = findInputs(input);
        if (inputs == null) {
            return ItemStack.EMPTY;
        }

        ItemStack wut = new ItemStack(AE2wtlibAPI.getWUT());
        wut = Common.mergeTerminal(wut, inputs.resonant(), inputs.resonantDefinition());
        return Common.mergeTerminal(wut, inputs.other(), inputs.otherDefinition());
    }

    @Nullable
    private static Inputs findInputs(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return null;
        }

        ItemStack resonant = ItemStack.EMPTY;
        ItemStack other = ItemStack.EMPTY;
        WTDefinition otherDefinition = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(AECSItems.WIRELESS_RESONANT_TERMINAL.get())) {
                if (!resonant.isEmpty()) {
                    return null;
                }
                resonant = stack;
                continue;
            }

            if (AE2wtlibAPI.isUniversalTerminal(stack)) {
                return null;
            }

            WTDefinition definition = WTDefinition.ofOrNull(stack);
            if (definition == null || definition.item() == AECSItems.WIRELESS_RESONANT_TERMINAL.get() || !other.isEmpty()) {
                return null;
            }
            other = stack;
            otherDefinition = definition;
        }

        if (resonant.isEmpty() || other.isEmpty() || otherDefinition == null) {
            return null;
        }

        WTDefinition resonantDefinition = WTDefinition.ofOrNull(resonant);
        if (resonantDefinition == null) {
            return null;
        }

        return new Inputs(resonant, resonantDefinition, other, otherDefinition);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(AE2wtlibAPI.getWUT());
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(AECSItems.WIRELESS_RESONANT_TERMINAL.get()));
        ingredients.add(Ingredient.of(WTDefinition.wirelessTerminals().stream()
                .filter(definition -> definition.item() != AECSItems.WIRELESS_RESONANT_TERMINAL.get())
                .map(definition -> new ItemStack((ItemLike) definition.item()))
                .toArray(ItemStack[]::new)));
        return ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.WIRELESS_UNIVERSAL_TERMINAL_COMBINE.get();
    }

    private record Inputs(ItemStack resonant, WTDefinition resonantDefinition, ItemStack other, WTDefinition otherDefinition) {}
}
