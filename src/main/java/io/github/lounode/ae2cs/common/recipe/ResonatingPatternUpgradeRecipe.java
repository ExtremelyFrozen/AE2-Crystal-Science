package io.github.lounode.ae2cs.common.recipe;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public @NotNull ItemStack assemble(CraftingContainer input, RegistryAccess registries)
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

        EncodedProcessingPattern src = processingPattern.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (src == null) return ItemStack.EMPTY;

        ItemStack out = AECSItems.RESONATING_PATTERN.get().getDefaultInstance();

        // targets 与 sparseInputs 同长度，默认全 empty
        List<Optional<EncodedResonatingPattern.Target>> targets = new ArrayList<>(src.sparseInputs().size());
        for (int i = 0; i < src.sparseInputs().size(); i++)
        {
            targets.add(Optional.empty());
        }

        out.set(AECSDataComponents.ENCODED_RESONATING_PATTERN.get(),
                new EncodedResonatingPattern(src.sparseInputs(), src.sparseOutputs(), targets));
        out.set(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), 0);

        return out;
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
        return stack.is(AEItems.PROCESSING_PATTERN.asItem()) && stack.has(AEComponents.ENCODED_PROCESSING_PATTERN);
    }

    private static boolean isResonatingDust(ItemStack stack)
    {
        return stack.is(AECSTags.Items.DUST_RESONATING);
    }
}