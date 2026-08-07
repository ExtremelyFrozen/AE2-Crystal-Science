package io.github.lounode.ae2cs.integration.emi;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_infuser.CrystalInfuserRecipe;

import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public class CrystalInfuserRecipeCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(
            AE2CrystalScience.makeId("crystal_infuser"), EmiStack.of(AECSBlocks.CRYSTAL_INFUSER_BLOCK)) {

        @Override
        public Component getName() {
            return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_infuser");
        }
    };

    public static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/recipe/crystal_infuser.png");
    private static final Rect2i ENERGY_TOOLTIP_AREA = new Rect2i(129, 21, 5, 18);
    private static final ResourceLocation MACHINE_SPRITES = AE2CrystalScience.makeId("textures/gui/crystal_infuser_menu.png");
    private static final int ANIM_DURATION_MS = 3_000;

    private final CrystalInfuserRecipe recipe;
    private final AdvancedProgressBar workingProgressBar;
    private long animStartMs = -1L;

    public CrystalInfuserRecipeCategory(RecipeHolder<CrystalInfuserRecipe> holder) {
        super(RECIPE_TYPE, holder.id(), 162, 62);
        recipe = holder.value();
        for (SizedIngredient ingredient : recipe.required()) {
            inputs.add(EmiIngredient.of(ingredient.ingredient(), ingredient.count()));
        }
        while (inputs.size() < 4) inputs.add(EmiStack.EMPTY);
        outputs.add(EmiStack.of(recipe.result().copy()));

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
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BG, 0, 0, 162, 62, 0, 0, 162, 62, 162, 62);
        int[][] positions = { { 91, 13 }, { 109, 13 }, { 91, 31 }, { 109, 31 } };
        for (int i = 0; i < 4; i++) {
            widgets.addSlot(inputs.get(i), positions[i][0], positions[i][1]).drawBack(false);
        }
        widgets.addSlot(outputs.getFirst(), 34, 16).large(true).recipeContext(this).drawBack(false);
        widgets.addTexture(MACHINE_SPRITES, 129, 21, 6, 18, 176, 34, 6, 18, 256, 256);
        widgets.addDrawable(0, 0, 0, 0, workingProgressBar::renderWidget);
        widgets.addTooltipText(List.of(Component.translatable(
                "ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.energyCost())),
                ENERGY_TOOLTIP_AREA.getX(), ENERGY_TOOLTIP_AREA.getY(), ENERGY_TOOLTIP_AREA.getWidth(), ENERGY_TOOLTIP_AREA.getHeight());
    }

    private int getAnimMsInCycle() {
        long now = Util.getMillis();
        if (animStartMs < 0) animStartMs = now;
        return (int) ((now - animStartMs) % ANIM_DURATION_MS);
    }
}
