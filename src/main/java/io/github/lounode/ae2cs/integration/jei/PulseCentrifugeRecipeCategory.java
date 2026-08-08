package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.pulse_centrifuge.PulseCentrifugeRecipe;

import appeng.client.gui.style.Blitter;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

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

import java.util.Arrays;
import java.util.List;

public class PulseCentrifugeRecipeCategory implements IRecipeCategory<RecipeHolder<PulseCentrifugeRecipe>> {

    public static final RecipeType<RecipeHolder<PulseCentrifugeRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(AE2CrystalScience.makeId("pulse_centrifuge"));

    private static final ResourceLocation MENU_TEXTURE = AE2CrystalScience.makeId("textures/gui/pulse_centrifuge_menu.png");
    private static final Rect2i ENERGY_TOOLTIP_AREA = new Rect2i(128, 21, 6, 18);
    private static final int ANIMATION_DURATION_MS = 3_000;
    private static final int[][] OUTPUT_POSITIONS = { { 91, 13 }, { 109, 13 }, { 91, 31 }, { 109, 31 } };

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final AdvancedProgressBar energyBar;
    private final AdvancedProgressBar workingProgressBar;
    private long animationStart = -1;

    public PulseCentrifugeRecipeCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        background = guiHelper.createDrawable(
                AE2CrystalScience.makeId("textures/gui/recipe/pulse_centrifuge.png"), 0, 0, 162, 62);
        icon = guiHelper.createDrawableItemLike(AECSBlocks.PULSE_CENTRIFUGE_BLOCK);

        IProgressProvider animation = new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimationProgress();
            }

            @Override
            public int getMaxProgress() {
                return ANIMATION_DURATION_MS;
            }
        };
        energyBar = new AdvancedProgressBar(animation,
                Blitter.texture(MENU_TEXTURE, 256, 256).src(176, 34, 6, 18),
                AdvancedProgressBar.FillMode.BOTTOM_TO_TOP);
        energyBar.setX(128);
        energyBar.setY(21);

        workingProgressBar = new AdvancedProgressBar(animation,
                Blitter.texture(MENU_TEXTURE, 256, 256).src(198, 0, 22, 33),
                AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(64);
        workingProgressBar.setY(15);
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<PulseCentrifugeRecipe>> getRecipeType() {
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
    public void draw(@NotNull RecipeHolder<PulseCentrifugeRecipe> recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, slots, graphics, mouseX, mouseY);
        background.draw(graphics);
        workingProgressBar.renderWidget(graphics, (int) mouseX, (int) mouseY, 0);
        energyBar.renderWidget(graphics, (int) mouseX, (int) mouseY, 0);
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("ae2cs.integration.jei.recipe_category.pulse_centrifuge");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<PulseCentrifugeRecipe> recipe,
                           @NotNull IRecipeSlotsView slots, double mouseX, double mouseY) {
        IRecipeCategory.super.getTooltip(tooltip, recipe, slots, mouseX, mouseY);
        if (ENERGY_TOOLTIP_AREA.contains((int) mouseX, (int) mouseY)) {
            tooltip.add(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip",
                    recipe.value().energyCost()));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder,
                          @NotNull RecipeHolder<PulseCentrifugeRecipe> holder,
                          @NotNull IFocusGroup focuses) {
        PulseCentrifugeRecipe recipe = holder.value();
        builder.addInputSlot(40, 22).addItemStacks(Arrays.asList(recipe.input().getItems()));
        List<ItemStack> results = recipe.results();
        for (int i = 0; i < results.size(); i++) {
            builder.addOutputSlot(OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1])
                    .addItemStack(results.get(i));
        }
    }

    private int getAnimationProgress() {
        long now = Util.getMillis();
        if (animationStart < 0) animationStart = now;
        return (int) ((now - animationStart) % ANIMATION_DURATION_MS);
    }
}
