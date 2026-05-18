package io.github.lounode.ae2cs.integration.emi;

import appeng.menu.interfaces.IProgressProvider;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class CrystalPulverizerRecipeCategory extends BasicEmiRecipe
{
    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(AE2CrystalScience.makeId("crystal_pulverizer"),
            EmiStack.of(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK))
    {
        @Override
        public Component getName()
        {
            return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_pulverizer");
        }
    };

    public static final Identifier BG = AE2CrystalScience.makeId("textures/gui/recipe/crystal_pulverizer.png");
    private static final int W = 135;
    private static final int H = 58;

    private static final Rect2i energyTooltipArea = new Rect2i(109, 21, 6, 18);

    private final AdvancedProgressBar energyRateBar;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    private final CrystalPulverizerRecipe recipe;

    public CrystalPulverizerRecipeCategory(RecipeHolder<CrystalPulverizerRecipe> holder)
    {
        super(RECIPE_TYPE, holder.id(), 135, 58);
        var recipe = holder.value();
        this.recipe = recipe;

        this.inputs.add(EmiIngredient.of(recipe.input().ingredient(), recipe.input().count()));

        this.outputs.add(EmiStack.of(recipe.result().copy()));

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
    public void addWidgets(WidgetHolder widgets)
    {
        widgets.addTexture(BG, 0, 0, W, H, 0, 0, W, H, 256, 256);

        int xIn = 22;
        int yIn = 21;
        widgets.addSlot(this.inputs.getFirst(), xIn, yIn).drawBack(false);

        int xOut = 85;
        int yOut = yIn;
        widgets.addSlot(this.outputs.getFirst(), xOut, yOut).recipeContext(this).drawBack(false);

        widgets.addDrawable(0, 0, 0, 0, energyRateBar::renderWidget);
        widgets.addDrawable(0, 0, 0, 0, workingProgressBar::renderWidget);

        widgets.addTooltipText(List.of(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.energyCost())),
                energyTooltipArea.getX(), energyTooltipArea.getY(), energyTooltipArea.getWidth(), energyTooltipArea.getHeight());
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