package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.SizedIngredient;
import io.github.lounode.ae2cs.common.recipe.input.SingleItemStackRecipeInput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipe implements Recipe<SingleItemStackRecipeInput>
{
    private final ResourceLocation id;
    private final SizedIngredient input;
    private final ItemStack result;
    private final int energyCost;

    public CrystalPulverizerRecipe(ResourceLocation id, SizedIngredient input, ItemStack result, int energyCost)
    {
        this.id = id;
        if (input.ingredient().isEmpty() || input.count() <= 0)
        {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        if (energyCost <= 0)
        {
            throw new IllegalArgumentException("Time must be positive");
        }

        this.input = input;
        this.result = result;
        this.energyCost = energyCost;
    }

    public SizedIngredient input()
    {
        return input;
    }

    public ItemStack result()
    {
        return result;
    }

    public int energyCost()
    {
        return energyCost;
    }

    @Override
    public boolean matches(@NotNull SingleItemStackRecipeInput singleRecipeInput, @NotNull Level level)
    {
        return input.test(singleRecipeInput.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleItemStackRecipeInput singleRecipeInput, @NotNull RegistryAccess provider)
    {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess provider)
    {
        return result;
    }

    @Override
    public @NotNull ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return AECSRecipeSerializers.CRYSTAL_PULVERIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType()
    {
        return AECSRecipeTypes.CRYSTAL_PULVERIZER.get();
    }
}
