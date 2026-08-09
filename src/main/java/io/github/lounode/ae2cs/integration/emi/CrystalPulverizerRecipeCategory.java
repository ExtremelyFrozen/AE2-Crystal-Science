package io.github.lounode.ae2cs.integration.emi;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;

import appeng.menu.interfaces.IProgressProvider;

import net.minecraft.Util;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public class CrystalPulverizerRecipeCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(AE2CrystalScience.makeId("crystal_pulverizer"),
            EmiStack.of(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK)) {

        @Override
        public Component getName() {
            return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_pulverizer");
        }
    };

    public static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/recipe/crystal_pulverizer.png");
    private static final int W = 162;
    private static final int H = 62;
    private static final int INPUT_X = 38;
    private static final int INPUT_Y = 21;
    private static final int OUTPUT_X = 90;
    private static final int OUTPUT_Y = 12;
    private static final int OUTPUT_COLUMNS = 2;
    private static final int OUTPUT_SLOT_SPACING = 18;

    private static final Rect2i energyTooltipArea = new Rect2i(128, 21, 6, 18);

    private final AdvancedProgressBar energyRateBar;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    private final CrystalPulverizerRecipe recipe;
    private final EmiStack fluidInput;
    private final EmiStack fluidOutput;

    public CrystalPulverizerRecipeCategory(RecipeHolder<CrystalPulverizerRecipe> holder) {
        super(RECIPE_TYPE, holder.id(), W, H);
        var recipe = holder.value();
        this.recipe = recipe;

        this.inputs.add(EmiIngredient.of(recipe.input().ingredient(), recipe.input().count()));

        this.outputs.add(EmiStack.of(recipe.result().copy()));
        for (int i = 1; i < 4; i++) {
            this.outputs.add(EmiStack.EMPTY);
        }
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
        energyRateBar.setX(128);
        energyRateBar.setY(21);

        workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress() {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.crystalPulverizerProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(64);
        workingProgressBar.setY(22);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BG, 0, 0, W, H, 0, 0, W, H, W, H);

        widgets.addSlot(this.inputs.getFirst(), INPUT_X, INPUT_Y).drawBack(false);
        if (!fluidInput.isEmpty()) {
            widgets.addTank(fluidInput, 1, 1, 18, 60, 16_000).drawBack(false);
        }

        for (int i = 0; i < 4; i++) {
            int outputX = OUTPUT_X + (i % OUTPUT_COLUMNS) * OUTPUT_SLOT_SPACING;
            int outputY = OUTPUT_Y + (i / OUTPUT_COLUMNS) * OUTPUT_SLOT_SPACING;
            widgets.addSlot(this.outputs.get(i), outputX, outputY).recipeContext(this).drawBack(false);
        }
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
