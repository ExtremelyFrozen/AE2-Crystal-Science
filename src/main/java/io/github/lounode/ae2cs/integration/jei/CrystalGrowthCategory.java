package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalGrowthCategory implements IRecipeCategory<CrystalSeedItem>
{
    public static IRecipeType<CrystalSeedItem> RECIPE_TYPE = IRecipeType.create(AECSConstants.MODID, "crystal_growth",
            CrystalSeedItem.class);

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;


    public CrystalGrowthCategory(IJeiHelpers jeiHelper)
    {
        var guiHelper = jeiHelper.getGuiHelper();
        this.background = guiHelper.createDrawable(AE2CrystalScience.makeId("textures/gui/recipe/crystal_growth.png"), 0, 0, 135, 58);
        this.icon = guiHelper.createDrawableItemLike(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK);
        this.arrow = guiHelper.createAnimatedRecipeArrow(100);
    }

    @Override
    public @NotNull IRecipeType<CrystalSeedItem> getRecipeType()
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
    public void draw(@NotNull CrystalSeedItem recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY)
    {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        background.draw(guiGraphics);
        arrow.draw(guiGraphics, 53, 22);
    }

    @Override
    public @NotNull Component getTitle()
    {
        return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_growth");
    }

    @Override
    public @Nullable IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CrystalSeedItem seedItem, @NotNull IFocusGroup focuses)
    {
        int xIn = 23;
        int yIn = 22;
        builder.addInputSlot(xIn, yIn).add(seedItem.getDefaultInstance());

        int xOut = 86;
        int yOut = yIn;
        builder.addOutputSlot(xOut, yOut).add(seedItem.getGrowTo().getDefaultInstance());
    }
}
