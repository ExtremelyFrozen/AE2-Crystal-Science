package io.github.lounode.ae2cs.integration.emi;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.pulse_centrifuge.PulseCentrifugeRecipe;

import appeng.client.gui.style.Blitter;
import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public class PulseCentrifugeRecipeCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(
            AE2CrystalScience.makeId("pulse_centrifuge"), EmiStack.of(AECSBlocks.PULSE_CENTRIFUGE_BLOCK)) {

        @Override
        public Component getName() {
            return Component.translatable("ae2cs.integration.jei.recipe_category.pulse_centrifuge");
        }
    };

    private static final ResourceLocation BACKGROUND = AE2CrystalScience.makeId("textures/gui/recipe/pulse_centrifuge_emi.png");
    private static final ResourceLocation MENU_TEXTURE = AE2CrystalScience.makeId("textures/gui/pulse_centrifuge_menu.png");
    private static final Rect2i ENERGY_TOOLTIP_AREA = new Rect2i(128, 21, 6, 18);
    private static final int ANIMATION_DURATION_MS = 3_000;
    private static final int[][] OUTPUT_POSITIONS = { { 91, 13 }, { 109, 13 }, { 91, 31 }, { 109, 31 } };

    private final PulseCentrifugeRecipe recipe;
    private final AdvancedProgressBar energyBar;
    private final AdvancedProgressBar workingProgressBar;
    private long animationStart = -1;

    public PulseCentrifugeRecipeCategory(RecipeHolder<PulseCentrifugeRecipe> holder) {
        super(RECIPE_TYPE, holder.id(), 162, 62);
        recipe = holder.value();
        inputs.add(EmiIngredient.of(recipe.input().ingredient(), recipe.input().count()));
        for (ItemStack result : recipe.results()) {
            outputs.add(EmiStack.of(result));
        }

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
                Blitter.texture(MENU_TEXTURE, 256, 256).src(198, 1, 20, 21),
                AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(66);
        workingProgressBar.setY(20);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 162, 62, 0, 0, 162, 62, 162, 62);
        widgets.addSlot(inputs.getFirst(), 38, 21).drawBack(false);
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), OUTPUT_POSITIONS[i][0] - 1, OUTPUT_POSITIONS[i][1] - 1)
                    .recipeContext(this)
                    .drawBack(false);
        }
        widgets.addDrawable(0, 0, 0, 0, workingProgressBar::renderWidget);
        widgets.addDrawable(0, 0, 0, 0, energyBar::renderWidget);
        widgets.addTooltipText(List.of(Component.translatable(
                "ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.energyCost())),
                ENERGY_TOOLTIP_AREA.getX(), ENERGY_TOOLTIP_AREA.getY(),
                ENERGY_TOOLTIP_AREA.getWidth(), ENERGY_TOOLTIP_AREA.getHeight());
    }

    private int getAnimationProgress() {
        long now = Util.getMillis();
        if (animationStart < 0) animationStart = now;
        return (int) ((now - animationStart) % ANIMATION_DURATION_MS);
    }
}
