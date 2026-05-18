package io.github.lounode.ae2cs.common.recipe.circuit_etcher;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.input.ThreeItemStackRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CircuitEtcherRecipe implements Recipe<ThreeItemStackRecipeInput>
{
    private final SizedIngredient inputA;
    private final SizedIngredient inputB;
    private final SizedIngredient inputC;
    private final ItemStack result;
    private final int energyCost;

    // 真正所需的输入的缓存
    private final List<SizedIngredient> effective;

    public CircuitEtcherRecipe(SizedIngredient inputA, SizedIngredient inputB, SizedIngredient inputC,
                               ItemStack result, int energyCost)
    {
        this.inputA = inputA;
        this.inputB = inputB;
        this.inputC = inputC;
        this.result = result;
        this.energyCost = energyCost;

        this.effective = new ArrayList<>(3);
        addIfRequired(this.effective, inputA);
        addIfRequired(this.effective, inputB);
        addIfRequired(this.effective, inputC);
    }

    private static void addIfRequired(List<SizedIngredient> list, SizedIngredient si)
    {
        if (si != null && !si.ingredient().isEmpty() && si.count() > 0)
        {
            list.add(si);
        }
    }

    public SizedIngredient inputA()
    {
        return inputA;
    }

    public SizedIngredient inputB()
    {
        return inputB;
    }

    public SizedIngredient inputC()
    {
        return inputC;
    }

    public ItemStack result()
    {
        return result;
    }

    public int energyCost()
    {
        return energyCost;
    }

    public List<SizedIngredient> required()
    {
        return effective;
    }

    // matches：只要 findMatch != null 就算匹配
    @Override
    public boolean matches(@NotNull ThreeItemStackRecipeInput in, @NotNull Level level)
    {
        return findMatch(in) != null;
    }

    /**
     * 返回数组 map：map[t] 表示第 t 个“有效输入”(effective.get(t)) 应该从机器的哪个槽位取。
     * 匹配失败返回 null。
     * <p>
     * 行为：
     * - 只要 3 个槽位中“存在槽位分配”能满足配方所有需求，就匹配
     * - 其它槽位可以是 ItemStack.EMPTY 或杂物，不影响匹配
     * - 不会跨槽位统一数量！
     */
    public int[] findMatch(ThreeItemStackRecipeInput in)
    {
        ItemStack[] stacks = {
                in.getInputA(),
                in.getInputB(),
                in.getInputC()
        };

        int effectiveSize = effective.size();
        if (effectiveSize == 0) return null;

        if (effectiveSize == 1)
        {
            SizedIngredient A = effective.get(0);
            for (int i = 0; i < 3; i++)
            {
                if (A.test(stacks[i])) return new int[]{i};
            }
            return null;
        }

        if (effectiveSize == 2)
        {
            SizedIngredient A = effective.get(0);
            SizedIngredient B = effective.get(1);

            for (int i = 0; i < 3; i++)
            {
                if (!A.test(stacks[i])) continue;
                for (int j = 0; j < 3; j++)
                {
                    if (j == i) continue;
                    if (B.test(stacks[j])) return new int[]{i, j};
                }
            }
            return null;
        }

        // effectiveSize == 3
        SizedIngredient A = effective.get(0);
        SizedIngredient B = effective.get(1);
        SizedIngredient C = effective.get(2);

        for (int i = 0; i < 3; i++)
        {
            if (!A.test(stacks[i])) continue;
            for (int j = 0; j < 3; j++)
            {
                if (j == i) continue;
                if (!B.test(stacks[j])) continue;

                int k = 3 - i - j;
                if (C.test(stacks[k])) return new int[]{i, j, k};
            }
        }
        return null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ThreeItemStackRecipeInput in)
    {
        return result.copy();
    }

    @Override
    public @NotNull ItemStack getResultItem()
    {
        return result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> list = NonNullList.create();
        for (var si : effective) list.add(si.ingredient());
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= effective.size();
    }

    @Override
    public @NotNull RecipeType<?> getType()
    {
        return AECSRecipeTypes.CIRCUIT_ETCHER.get();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return AECSRecipeSerializers.CIRCUIT_ETCHER.get();
    }
}