package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_infuser.CrystalInfuserRecipe;

import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrystalInfuserRecipeCategory implements IRecipeCategory<RecipeHolder<CrystalInfuserRecipe>> {

    public static final RecipeType<RecipeHolder<CrystalInfuserRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(AE2CrystalScience.makeId("crystal_infuser"));

    private static final Rect2i ENERGY_TOOLTIP_AREA = new Rect2i(109, 21, 6, 18);
    private static final int ANIM_DURATION_MS = 3_000;

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawableStatic energyIndicator;
    private final AdvancedProgressBar workingProgressBar;
    private long animStartMs = -1L;

    public CrystalInfuserRecipeCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        background = guiHelper.createDrawable(AE2CrystalScience.makeId("textures/gui/recipe/crystal_infuser.png"),
                0, 0, 162, 62);
        energyIndicator = guiHelper.createDrawable(
                AE2CrystalScience.makeId("textures/gui/crystal_infuser_menu.png"),
                176, 34, 6, 18);
        icon = guiHelper.createDrawableItemLike(AECSBlocks.CRYSTAL_INFUSER_BLOCK);

        IProgressProvider animation = new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress() {
                return ANIM_DURATION_MS;
            }
        };
        workingProgressBar = new AdvancedProgressBar(animation, AECSBlitter.crystalInfuserProgress,
                AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(64);
        workingProgressBar.setY(15);
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<CrystalInfuserRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void draw(@NotNull RecipeHolder<CrystalInfuserRecipe> recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, slots, graphics, mouseX, mouseY);
        background.draw(graphics);
        energyIndicator.draw(graphics, 129, 21);
        workingProgressBar.renderWidget(graphics, (int) mouseX, (int) mouseY, 0);
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_infuser");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<CrystalInfuserRecipe> recipe,
                           @NotNull IRecipeSlotsView slots, double mouseX, double mouseY) {
        IRecipeCategory.super.getTooltip(tooltip, recipe, slots, mouseX, mouseY);
        if (ENERGY_TOOLTIP_AREA.contains((int) mouseX, (int) mouseY)) {
            tooltip.add(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip",
                    recipe.value().energyCost()));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RecipeHolder<CrystalInfuserRecipe> recipe,
                          @NotNull IFocusGroup focuses) {
        int[][] positions = { { 91, 13 }, { 109, 13 }, { 91, 31 }, { 109, 31 } };
        List<SizedIngredient> ingredients = recipe.value().required();
        for (int i = 0; i < positions.length; i++) {
            var slot = builder.addInputSlot(positions[i][0], positions[i][1]);
            if (i < ingredients.size()) {
                slot.addItemStacks(List.of(ingredients.get(i).getItems()));
            }
        }
        builder.addOutputSlot(34, 16)
                .setOutputSlotBackground()
                .addItemStack(recipe.value().result().copy());
    }

    private int getAnimMsInCycle() {
        long now = Util.getMillis();
        if (animStartMs < 0) animStartMs = now;
        return (int) ((now - animStartMs) % ANIM_DURATION_MS);
    }
}
