package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.client.gui.icon.AECSBlitter;
import io.github.lounode.ae2cs.client.gui.widgets.AdvancedProgressBar;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import appeng.menu.interfaces.IProgressProvider;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntropyVariationReactionChamberRecipeCategory implements IRecipeCategory<RecipeHolder<EntropyRecipe>> {

    private static final int BACKGROUND_HEIGHT = 62;
    private static final int RECIPE_HEIGHT = BACKGROUND_HEIGHT;
    private static final float MODE_TEXT_SCALE = 0.75f;
    private static final int MODE_TEXT_Y = 50;
    private static final ResourceLocation AE2_EMI_TEXTURE = ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/jei.png");

    public static final RecipeType<RecipeHolder<EntropyRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(
            AE2CrystalScience.makeId("entropy_variation_reaction_chamber"));

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final AdvancedProgressBar workingProgressBar;

    private static final int ANIM_DURATION_MS = 3_000;
    private long animStartMs = -1L;

    public EntropyVariationReactionChamberRecipeCategory(IJeiHelpers jeiHelper) {
        var guiHelper = jeiHelper.getGuiHelper();
        this.background = guiHelper.drawableBuilder(AE2CrystalScience.makeId(
                "textures/gui/recipe/entropy_variation_reaction_chamber.png"), 0, 0, 162, 62)
                .setTextureSize(162, 62)
                .build();
        this.icon = guiHelper.createDrawableItemLike(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK);

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
    public @NotNull RecipeType<RecipeHolder<EntropyRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return RECIPE_HEIGHT;
    }

    @Override
    public void draw(@NotNull RecipeHolder<EntropyRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        workingProgressBar.renderWidget(guiGraphics, (int) mouseX, (int) mouseY, 0.0f);
        drawModeText(guiGraphics, recipe.value().getMode());
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.ae2cs.entropy_variation_reaction_chamber");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RecipeHolder<EntropyRecipe> holder,
                          @NotNull IFocusGroup focuses) {
        var recipe = holder.value();
        recipe.getInput().fluid().map(EntropyRecipe.FluidInput::fluid).filter(fluid -> fluid != Fluids.EMPTY)
                .ifPresent(fluid -> builder.addInputSlot(1, 1).setFluidRenderer(16_000, true, 18, 60)
                        .addFluidStack(fluid, 1000));
        recipe.getInput().block().ifPresent(block -> builder.addInputSlot(39, 22)
                .addItemStack(block.block().asItem().getDefaultInstance()));

        List<ItemStack> itemOutputs = new ArrayList<>(4);
        recipe.getOutput().block().filter(block -> !block.block().defaultBlockState().isAir())
                .ifPresent(block -> itemOutputs.add(block.block().asItem().getDefaultInstance()));
        recipe.getDrops().stream().limit(4 - itemOutputs.size()).forEach(itemOutputs::add);

        recipe.getOutput().fluid().map(EntropyRecipe.FluidOutput::fluid).filter(fluid -> fluid != Fluids.EMPTY)
                .ifPresent(fluid -> builder.addOutputSlot(143, 1).setFluidRenderer(16_000, true, 18, 60)
                        .addFluidStack(fluid, 1000));

        int outputIndex = 0;
        for (int i = 0; i < 4; i++) {
            var slot = builder.addOutputSlot(91 + (i % 2) * 18, 13 + (i / 2) * 18);
            if (outputIndex < itemOutputs.size()) {
                slot.addItemStack(itemOutputs.get(outputIndex++));
            }
        }
    }

    private int getAnimMsInCycle() {
        long now = Util.getMillis();
        if (animStartMs < 0L) {
            animStartMs = now;
        }
        return (int) ((now - animStartMs) % ANIM_DURATION_MS);
    }

    private static Component getModeText(EntropyMode mode) {
        return switch (mode) {
            case HEAT -> Component.translatable("ae2cs.recipe.entropy_variation_reaction_chamber.mode.increase", 1600);
            case COOL -> Component.translatable("ae2cs.recipe.entropy_variation_reaction_chamber.mode.decrease", 1600);
        };
    }

    private void drawModeText(GuiGraphics guiGraphics, EntropyMode mode) {
        var font = Minecraft.getInstance().font;
        Component text = getModeText(mode);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(MODE_TEXT_SCALE, MODE_TEXT_SCALE, 1.0f);
        int x = (int) (getWidth() / (2.0f * MODE_TEXT_SCALE) - font.width(text) / 2.0f);
        int y = (int) (MODE_TEXT_Y / MODE_TEXT_SCALE);
        int iconU = mode == EntropyMode.HEAT ? 0 : 6;
        guiGraphics.blit(AE2_EMI_TEXTURE, x - 9, y + 1, iconU, 68, 6, 6, 256, 256);
        guiGraphics.drawString(font, text, x, y, 0x7E7E7E, false);
        guiGraphics.pose().popPose();
    }
}
