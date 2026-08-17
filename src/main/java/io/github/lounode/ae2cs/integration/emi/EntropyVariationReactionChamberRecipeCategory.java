package io.github.lounode.ae2cs.integration.emi;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import appeng.menu.interfaces.IProgressProvider;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.ArrayList;
import java.util.List;

public class EntropyVariationReactionChamberRecipeCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(
            AE2CrystalScience.makeId("entropy_variation_reaction_chamber"),
            EmiStack.of(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK)) {

        @Override
        public Component getName() {
            return Component.translatable("block.ae2cs.entropy_variation_reaction_chamber");
        }
    };

    private static final ResourceLocation BG = AE2CrystalScience.makeId(
            "textures/gui/recipe/entropy_variation_reaction_chamber.png");
    private static final ResourceLocation AE2_EMI_TEXTURE = ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/jei.png");
    private static final int W = 162;
    private static final int BACKGROUND_H = 62;
    private static final int H = BACKGROUND_H;
    private static final int MODE_TEXT_Y = 50;
    private static final int FLUID_INPUT_X = 1;
    private static final int FLUID_INPUT_Y = 1;
    private static final int FLUID_INPUT_WIDTH = 18;
    private static final int FLUID_INPUT_HEIGHT = 60;
    private static final int FLUID_OUTPUT_X = 143;
    private static final int FLUID_OUTPUT_Y = 1;
    private static final int BLOCK_INPUT_X = 38;
    private static final int BLOCK_INPUT_Y = 21;
    private static final int OUTPUT_X = 90;
    private static final int OUTPUT_Y = 12;
    private static final int OUTPUT_COLUMNS = 2;
    private static final int SLOT_SPACING = 18;
    private static final int ANIM_DURATION_MS = 3_000;

    private final EmiStack inputBlock;
    private final EmiStack inputFluid;
    private final EmiStack outputFluid;
    private final List<EmiStack> outputStacks;
    private final EntropyMode mode;
    private final Component modeText;
    private final AdvancedProgressBar workingProgressBar;
    private long animStartMs = -1L;

    public EntropyVariationReactionChamberRecipeCategory(RecipeHolder<EntropyRecipe> holder) {
        super(RECIPE_TYPE, holder.id(), W, H);
        var recipe = holder.value();
        mode = recipe.getMode();
        modeText = getModeText(mode);

        inputBlock = recipe.getInput().block()
                .map(EntropyRecipe.BlockInput::block)
                .map(EntropyVariationReactionChamberRecipeCategory::asStack)
                .orElse(EmiStack.EMPTY);
        inputFluid = recipe.getInput().fluid()
                .map(EntropyRecipe.FluidInput::fluid)
                .filter(fluid -> fluid != Fluids.EMPTY)
                .map(fluid -> EmiStack.of(fluid, getFluidInputAmount(recipe)))
                .orElse(EmiStack.EMPTY);
        if (!inputBlock.isEmpty()) {
            inputs.add(inputBlock);
        }
        if (!inputFluid.isEmpty()) {
            inputs.add(inputFluid);
        }

        outputStacks = new ArrayList<>(4);
        recipe.getOutput().block()
                .map(EntropyRecipe.BlockOutput::block)
                .filter(block -> !block.defaultBlockState().isAir())
                .map(EntropyVariationReactionChamberRecipeCategory::asStack)
                .ifPresent(outputStacks::add);
        outputFluid = recipe.getOutput().fluid()
                .map(EntropyRecipe.FluidOutput::fluid)
                .filter(fluid -> fluid != Fluids.EMPTY)
                .map(fluid -> EmiStack.of(fluid, 1000))
                .orElse(EmiStack.EMPTY);
        recipe.getDrops().stream().map(EmiStack::of).limit(4 - outputStacks.size()).forEach(outputStacks::add);
        outputs.addAll(outputStacks);

        workingProgressBar = new AdvancedProgressBar(new IProgressProvider() {

            @Override
            public int getCurrentProgress() {
                return getAnimMsInCycle();
            }

            @Override
            public int getMaxProgress() {
                return ANIM_DURATION_MS;
            }
        }, AECSBlitter.entropyVariationProgress, AdvancedProgressBar.FillMode.LEFT_TO_RIGHT);
        workingProgressBar.setX(68);
        workingProgressBar.setY(24);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BG, 0, 0, W, BACKGROUND_H, 0, 0, W, BACKGROUND_H, W, BACKGROUND_H);
        if (!inputFluid.isEmpty()) {
            widgets.addTank(inputFluid, FLUID_INPUT_X, FLUID_INPUT_Y, FLUID_INPUT_WIDTH, FLUID_INPUT_HEIGHT, 16_000)
                    .drawBack(false);
        }
        widgets.addSlot(inputBlock, BLOCK_INPUT_X, BLOCK_INPUT_Y).drawBack(false);
        for (int i = 0; i < 4; i++) {
            EmiStack output = i < outputStacks.size() ? outputStacks.get(i) : EmiStack.EMPTY;
            int outputX = OUTPUT_X + (i % OUTPUT_COLUMNS) * SLOT_SPACING;
            int outputY = OUTPUT_Y + (i / OUTPUT_COLUMNS) * SLOT_SPACING;
            widgets.addSlot(output, outputX, outputY).recipeContext(this).drawBack(false);
        }
        if (!outputFluid.isEmpty()) {
            widgets.addTank(outputFluid, FLUID_OUTPUT_X, FLUID_OUTPUT_Y, FLUID_INPUT_WIDTH, FLUID_INPUT_HEIGHT, 16_000)
                    .recipeContext(this).drawBack(false);
        }
        widgets.addDrawable(0, 0, 0, 0, workingProgressBar::renderWidget);
        TextWidget modeTextWidget = widgets.addText(modeText, W / 2, MODE_TEXT_Y, 0x7E7E7E, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
        widgets.addTexture(AE2_EMI_TEXTURE, modeTextWidget.getBounds().x() - 9, MODE_TEXT_Y + 1,
                6, 6, getModeIconU(), 68);
    }

    private int getAnimMsInCycle() {
        long now = Util.getMillis();
        if (animStartMs < 0L) {
            animStartMs = now;
        }
        return (int) ((now - animStartMs) % ANIM_DURATION_MS);
    }

    private static long getFluidInputAmount(EntropyRecipe recipe) {
        boolean isSnowballRecipe = recipe.getInput().fluid()
                .map(input -> input.fluid() == Fluids.FLOWING_WATER)
                .orElse(false) && recipe.getDrops().stream().anyMatch(drop -> drop.is(Items.SNOWBALL));
        return isSnowballRecipe ? 250 : 1000;
    }

    private static Component getModeText(EntropyMode mode) {
        return switch (mode) {
            case HEAT -> Component.translatable("ae2cs.recipe.entropy_variation_reaction_chamber.mode.increase", 1600);
            case COOL -> Component.translatable("ae2cs.recipe.entropy_variation_reaction_chamber.mode.decrease", 1600);
        };
    }

    private int getModeIconU() {
        return switch (mode) {
            case HEAT -> 0;
            case COOL -> 6;
        };
    }

    private static EmiStack asStack(Block block) {
        return EmiStack.of(block.asItem().getDefaultInstance());
    }
}
