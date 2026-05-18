package io.github.lounode.ae2cs.integration.jei;

import appeng.menu.interfaces.IProgressProvider;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalPulverizerRecipeCategory implements IRecipeCategory<RecipeHolder<CrystalPulverizerRecipe>>
{
    public static IRecipeType<RecipeHolder<CrystalPulverizerRecipe>> RECIPE_TYPE = IRecipeHolderType.create(AE2CrystalScience.makeId("crystal_pulverizer"));

    private static final Rect2i energyTooltipArea = new Rect2i(109, 21, 6, 18);

    private final IDrawableStatic background;
    private final IDrawable icon;

    private final AdvancedProgressBar energyRateBar;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    public CrystalPulverizerRecipeCategory(IJeiHelpers jeiHelper)
    {
        var guiHelper = jeiHelper.getGuiHelper();
        this.background = guiHelper.createDrawable(AE2CrystalScience.makeId("textures/gui/recipe/crystal_pulverizer.png"), 0, 0, 135, 58);
        this.icon = guiHelper.createDrawableItemLike(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK);

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
        }, AECSBlitter.crystalPulverizerProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(53);
        workingProgressBar.setY(22);
    }

    @Override
    public @NotNull IRecipeType<RecipeHolder<CrystalPulverizerRecipe>> getRecipeType()
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
    public void draw(@NotNull RecipeHolder<CrystalPulverizerRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY)
    {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        background.draw(guiGraphics);
        energyRateBar.renderWidget(guiGraphics, (int) mouseX, (int) mouseY, 0.0f);
        workingProgressBar.renderWidget(guiGraphics, (int) mouseX, (int) mouseY, 0.0f);
    }

    @Override
    public @NotNull Component getTitle()
    {
        return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_pulverizer");
    }

    @Override
    public @Nullable IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<CrystalPulverizerRecipe> recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
    {
        IRecipeCategory.super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
        if (energyTooltipArea.contains((int) mouseX, (int) mouseY))
        {
            tooltip.add(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.value().energyCost()));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RecipeHolder<CrystalPulverizerRecipe> recipe, @NotNull IFocusGroup focuses)
    {
        int xIn = 23;
        int yIn = 22;
        builder.addInputSlot(xIn, yIn).add(recipe.value().input().ingredient());

        int xOut = 86;
        int yOut = yIn;
        builder.addOutputSlot(xOut, yOut).add(recipe.value().result().copy());
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