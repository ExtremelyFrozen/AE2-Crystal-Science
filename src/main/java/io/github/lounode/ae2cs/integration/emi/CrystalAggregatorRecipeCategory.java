package io.github.lounode.ae2cs.integration.emi;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;

import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public class CrystalAggregatorRecipeCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(AE2CrystalScience.makeId("crystal_aggregator"),
            EmiStack.of(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK)) {

        @Override
        public Component getName() {
            return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_aggregator");
        }
    };

    public static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/recipe/crystal_aggregator.png");
    private static final int W = 162;
    private static final int H = 62;

    private static final Rect2i energyTooltipArea = new Rect2i(110, 23, 6, 18);

    private final AdvancedProgressBar energyRateBar;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    private final CrystalAggregatorRecipe recipe;
    private final EmiStack fluidInput;
    private final EmiStack fluidOutput;

    public CrystalAggregatorRecipeCategory(RecipeHolder<CrystalAggregatorRecipe> holder) {
        super(RECIPE_TYPE, holder.id(), W, H);
        var recipe = holder.value();
        this.recipe = recipe;

        List<SizedIngredient> req = recipe.required();
        for (int i = 0; i < 3; i++) {
            if (req.size() > i) {
                var ingredient = req.get(i).ingredient();
                var count = req.get(i).count();
                this.inputs.add(EmiIngredient.of(ingredient, count));
            } else {
                this.inputs.add(EmiStack.EMPTY);
            }
        }

        this.outputs.add(EmiStack.of(recipe.result().copy()));
        this.fluidInput = recipe.fluidInput() != null && recipe.fluidInput().getFluids().length > 0 ? EmiStack.of(recipe.fluidInput().getFluids()[0].getFluid(), recipe.fluidInput().getFluids()[0].getAmount()) : EmiStack.EMPTY;
        FluidStack recipeFluidOutput = recipe.fluidOutput();
        this.fluidOutput = recipeFluidOutput.isEmpty() ? EmiStack.EMPTY : EmiStack.of(recipeFluidOutput.getFluid(), recipeFluidOutput.getAmount());

        energyRateBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress() {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.energyProgress, AdvancedProgressBar.FillMode.BOTTOM_TO_TOP);
        energyRateBar.setX(125);
        energyRateBar.setY(23);

        workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress() {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.crystalAggregatorProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(70);
        workingProgressBar.setY(17);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BG, 0, 0, W, H, 0, 0, W, H, W, H);

        int xIn = 45;
        int y0 = 4;
        int dy = 18;
        for (int i = 0; i < 3; i++) {
            widgets.addSlot(this.inputs.get(i), xIn, y0 + i * dy).drawBack(false);
        }
        if (!fluidInput.isEmpty()) {
            widgets.addTank(fluidInput, 1, 1, 18, 60, 16_000).drawBack(false);
        }

        int xOut = 101;
        int yOut = y0 + dy + 1;
        widgets.addSlot(this.outputs.getFirst(), xOut, yOut).recipeContext(this).drawBack(false);
        if (!fluidOutput.isEmpty()) {
            widgets.addTank(fluidOutput, 143, 1, 18, 60, 16_000).recipeContext(this).drawBack(false);
        }

        widgets.addDrawable(0, 0, 0, 0, energyRateBar::renderWidget);
        widgets.addDrawable(0, 0, 0, 0, workingProgressBar::renderWidget);

        widgets.addTooltipText(List.of(Component.translatable("ae2cs.integration.jei.recipe_category.energy_cost.tooltip", recipe.energyCost())),
                energyTooltipArea.getX(), energyTooltipArea.getY(), energyTooltipArea.getWidth(), energyTooltipArea.getHeight());
    }

    private int getAnimMsInCycle() {
        long now = Util.getMillis();
        if (animStartMs < 0L) {
            animStartMs = now;
        }
        long elapsed = now - animStartMs;
        return (int) (elapsed % ANIM_DURATION_MS);
    }
}
