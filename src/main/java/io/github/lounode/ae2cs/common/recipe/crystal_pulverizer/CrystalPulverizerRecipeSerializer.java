package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import io.github.lounode.ae2cs.common.recipe.SizedIngredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalPulverizerRecipeSerializer implements RecipeSerializer<CrystalPulverizerRecipe> {

    @Override
    public @NotNull CrystalPulverizerRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        if (!json.has("input")) {
            throw new JsonSyntaxException("Missing required field 'input'");
        }
        SizedIngredient input = SizedIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));

        if (!json.has("result")) {
            throw new JsonSyntaxException("Missing required field 'result'");
        }
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        int energyCost = GsonHelper.getAsInt(json, "energy_cost", 200);

        return new CrystalPulverizerRecipe(id, input, result, energyCost);
    }

    @Override
    public @Nullable CrystalPulverizerRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        SizedIngredient input = SizedIngredient.fromNetwork(buf);
        ItemStack result = buf.readItem();
        int energyCost = buf.readVarInt();

        return new CrystalPulverizerRecipe(id, input, result, energyCost);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull CrystalPulverizerRecipe recipe) {
        recipe.input().toNetwork(buf);
        buf.writeItem(recipe.result());
        buf.writeVarInt(recipe.energyCost());
    }
}
