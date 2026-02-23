package io.github.lounode.ae2cs.common.recipe;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.api.util.PatternHelper;
import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ResonatingPatternUpgradeRecipe extends CustomRecipe
{

    public ResonatingPatternUpgradeRecipe(ResourceLocation id, CraftingBookCategory category)
    {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, @NotNull Level level)
    {
        ItemStack pattern = ItemStack.EMPTY;
        ItemStack crystal = ItemStack.EMPTY;

        for (int i = 0; i < input.getItems().size(); i++)
        {
            var s = input.getItem(i);
            if (s.isEmpty()) continue;

            if (isEncodedProcessingPattern(s))
            {
                if (!pattern.isEmpty()) return false;
                pattern = s;
            }
            else if (isResonatingDust(s))
            {
                if (!crystal.isEmpty()) return false;
                crystal = s;
            }
            else
            {
                return false;
            }
        }

        return !pattern.isEmpty() && !crystal.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer input, @NotNull RegistryAccess registries)
    {
        ItemStack processingPattern = ItemStack.EMPTY;

        for (int i = 0; i < input.getItems().size(); i++)
        {
            var s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (isEncodedProcessingPattern(s))
            {
                processingPattern = s;
                break;
            }
        }

        if (processingPattern.isEmpty()) return ItemStack.EMPTY;

        // 将处理样板编码为谐振样板，不成功时内部会返回ItemStack.EMPTY
        return ResonatingPatternDetails.encode(processingPattern);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return AECSRecipeSerializers.RESONATING_PATTERN_UPGRADE.get();
    }

    private static boolean isEncodedProcessingPattern(ItemStack stack)
    {
        return stack.is(AEItems.PROCESSING_PATTERN.asItem()) && PatternHelper.getAEProcessingPattern(stack) != null;
    }

    private static boolean isResonatingDust(ItemStack stack)
    {
        return stack.is(AECSTags.Items.DUST_RESONATING);
    }
}
