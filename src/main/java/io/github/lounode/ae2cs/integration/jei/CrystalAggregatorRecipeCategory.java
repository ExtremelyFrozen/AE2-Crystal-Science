package io.github.lounode.ae2cs.integration.jei;

import appeng.menu.interfaces.IProgressProvider;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CrystalAggregatorRecipeCategory implements IRecipeCategory<CrystalAggregatorRecipe>
{
    public static RecipeType<CrystalAggregatorRecipe> RECIPE_TYPE = RecipeType.create(AECSConstants.MODID, "crystal_aggregator",
            CrystalAggregatorRecipe.class);

    private static final Rect2i energyTooltipArea = new Rect2i(109, 21, 6, 18);

    private final IDrawableStatic background;
    private final IDrawable icon;

    private final AdvancedProgressBar energyRateBar;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    public CrystalAggregatorRecipeCategory(IJeiHelpers jeiHelper)
    {
        var guiHelper = jeiHelper.getGuiHelper();
        this.background = guiHelper.createDrawable(AE2CrystalScience.makeId("textures/gui/recipe/crystal_aggregator.png"), 0, 0, 135, 58);
        this.icon = guiHelper.createDrawableItemLike(AECSBlocks.CIRCUIT_ETCHER_BLOCK);

        energyRateBar = new AdvancedProgressBar(new IProgressProvider()
        {
            @Override
            public int getCurrentProgress()
            {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress()
            {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.energyProgress, AdvancedProgressBar.FillMode.BOTTOM_TO_TOP);
        energyRateBar.setX(109);
        energyRateBar.setY(21);

        workingProgressBar = new AdvancedProgressBar(new IProgressProvider()
        {
            @Override
            public int getCurrentProgress()
            {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress()
            {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.crystalAggregatorProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(54);
        workingProgressBar.setY(15);
    }

    @Override
    public @NotNull RecipeType<CrystalAggregatorRecipe> getRecipeType()
    {
        return RECIPE_TYPE;
    }

    @Override
    public int getWidth()
    {
        return background.getWidth();
    }

    @Override
    public int getHeight()
    {
        return background.getHeight();
    }

    @Override
    public void draw(@NotNull CrystalAggregatorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        background.draw(guiGraphics);
        energyRateBar.renderWidget(guiGraphics, (int) mouseX, (int) mouseY, 0.0f);
        workingProgressBar.renderWidget(guiGraphics, (int) mouseX, (int) mouseY, 0.0f);
    }

    @Override
    public @NotNull Component getTitle()
    {
        return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_aggregator");
    }

    @Override
    public @Nullable IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull CrystalAggregatorRecipe recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
    {
        IRecipeCategory.super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
        if (energyTooltipArea.contains((int) mouseX, (int) mouseY))
        {
            tooltip.add(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.energyCost()));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CrystalAggregatorRecipe recipe, @NotNull IFocusGroup focuses)
    {
        int xIn = 30;
        int y0 = 3;
        int dy = 18;

        List<SizedIngredient> ingredients = recipe.required();
        for (int i = 0; i < 3; i++)
        {
            IRecipeSlotBuilder slotBuilder = builder.addInputSlot(xIn, y0 + i * dy);
            if (ingredients.size() > i)
            {
                slotBuilder.addItemStacks(Arrays.asList(ingredients.get(i).getItems()));
            }
        }

        int xOut = 86;
        int yOut = y0 + dy + 1;
        builder.addOutputSlot(xOut, yOut).addItemStack(recipe.result().copy());
    }

    private int getAnimMsInCycle()
    {
        long now = Util.getMillis();
        if (animStartMs < 0L)
        {
            animStartMs = now;
        }
        long elapsed = now - animStartMs;
        return (int) (elapsed % ANIM_DURATION_MS);
    }
}