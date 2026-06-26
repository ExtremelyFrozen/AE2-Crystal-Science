package io.github.lounode.ae2cs.common.recipe.crystal_aggregator;

import io.github.lounode.ae2cs.common.recipe.SizedIngredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalAggregatorRecipeSerializer implements RecipeSerializer<CrystalAggregatorRecipe> {

    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    @Override
    public @NotNull CrystalAggregatorRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        SizedIngredient a = readSizedIngredientOptional(json, "input_a");
        SizedIngredient b = readSizedIngredientOptional(json, "input_b");
        SizedIngredient c = readSizedIngredientOptional(json, "input_c");

        if (!json.has("result")) {
            throw new JsonSyntaxException("Missing required field 'result'");
        }
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        int energyCost = GsonHelper.getAsInt(json, "energy_cost", 200);

        return new CrystalAggregatorRecipe(id, a, b, c, result, energyCost);
    }

    @Override
    public @Nullable CrystalAggregatorRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        SizedIngredient a = SizedIngredient.fromNetwork(buf);
        SizedIngredient b = SizedIngredient.fromNetwork(buf);
        SizedIngredient c = SizedIngredient.fromNetwork(buf);

        ItemStack result = buf.readItem();
        int energyCost = buf.readVarInt();

        return new CrystalAggregatorRecipe(id, a, b, c, result, energyCost);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull CrystalAggregatorRecipe recipe) {
        recipe.inputA().toNetwork(buf);
        recipe.inputB().toNetwork(buf);
        recipe.inputC().toNetwork(buf);

        buf.writeItem(recipe.result());
        buf.writeVarInt(recipe.energyCost());
    }

    private static SizedIngredient readSizedIngredientOptional(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return EMPTY;
        }

        JsonObject obj = GsonHelper.convertToJsonObject(json.get(key), key);
        return SizedIngredient.fromJson(obj);
    }
}
