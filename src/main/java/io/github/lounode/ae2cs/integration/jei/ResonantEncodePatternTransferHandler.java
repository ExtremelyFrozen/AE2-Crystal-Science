package io.github.lounode.ae2cs.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;
import io.github.lounode.ae2cs.integration.ResonantPatternEncodingTransferHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.runtime.IIngredientVisibility;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import tamaized.ae2jeiintegration.integration.modules.jei.GenericEntryStackHelper;

public class ResonantEncodePatternTransferHandler
        implements IUniversalRecipeTransferHandler<ResonantTemplateCodingTermMenu> {
    private final MenuType<ResonantTemplateCodingTermMenu> menuType;
    private final IRecipeTransferHandlerHelper helper;
    private final IIngredientVisibility ingredientVisibility;

    public ResonantEncodePatternTransferHandler(MenuType<ResonantTemplateCodingTermMenu> menuType,
            IRecipeTransferHandlerHelper helper, IIngredientVisibility ingredientVisibility) {
        this.menuType = menuType;
        this.helper = helper;
        this.ingredientVisibility = ingredientVisibility;
    }

    @Override
    public Class<? extends ResonantTemplateCodingTermMenu> getContainerClass() {
        return ResonantTemplateCodingTermMenu.class;
    }

    @Override
    public Optional<MenuType<ResonantTemplateCodingTermMenu>> getMenuType() {
        return Optional.of(this.menuType);
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(ResonantTemplateCodingTermMenu menu, Object recipeBase,
            IRecipeSlotsView slotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        Recipe<?> recipe = null;
        RecipeHolder<?> recipeHolder = null;
        if (recipeBase instanceof RecipeHolder<?> holder) {
            recipeHolder = holder;
            recipe = holder.value();
        }

        boolean craftingRecipe = EncodingHelper.isSupportedCraftingRecipe(recipe);
        if (craftingRecipe && !recipe.canCraftInDimensions(3, 3)) {
            return this.helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe) {
                ResonantPatternEncodingTransferHelper.encodeCraftingRecipe(menu, recipeHolder,
                        getGuiIngredientsForCrafting(slotsView), this::isIngredientVisible);
            } else {
                ResonantPatternEncodingTransferHelper.encodeProcessingRecipe(menu, GenericEntryStackHelper.ofInputs(slotsView),
                        GenericEntryStackHelper.ofOutputs(slotsView));
            }
        } else {
            boolean moveItems = menu.pullProcessingRecipeInputs;
            TransferPreview preview = moveItems
                    ? findTransferPreview(menu, slotsView)
                    : findEncodingPreview(menu, slotsView);
            return new ErrorRenderer(preview, moveItems);
        }

        return null;
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        return this.ingredientVisibility.isIngredientVisible(VanillaTypes.ITEM_STACK, itemStack);
    }

    private static List<List<GenericStack>> getGuiIngredientsForCrafting(IRecipeSlotsView recipeLayout) {
        List<IRecipeSlotView> recipeSlots = recipeLayout.getSlotViews(RecipeIngredientRole.INPUT);
        var result = new ArrayList<List<GenericStack>>(9);
        for (int i = 0; i < 9; i++) {
            if (i < recipeSlots.size()) {
                IRecipeSlotView slot = recipeSlots.get(i);
                result.add(slot.getIngredients((IIngredientType<ItemStack>) VanillaTypes.ITEM_STACK)
                        .map(GenericStack::fromItemStack)
                        .filter(Objects::nonNull)
                        .toList());
            } else {
                result.add(Collections.emptyList());
            }
        }
        return result;
    }

    private static TransferPreview findTransferPreview(ResonantTemplateCodingTermMenu menu,
            IRecipeSlotsView slotsView) {
        IClientRepo repo = menu.getClientRepo();
        if (repo == null) {
            return new TransferPreview(List.of(), List.of());
        }

        Map<AEKey, Long> availableAmounts = new HashMap<>();
        Set<AEKey> craftableKeys = repo.getAllEntries().stream()
                .peek(entry -> {
                    if (entry.getWhat() != null && entry.getStoredAmount() > 0) {
                        availableAmounts.merge(entry.getWhat(), entry.getStoredAmount(), Long::sum);
                    }
                })
                .filter(entry -> entry.getWhat() != null && entry.isCraftable())
                .map(GridInventoryEntry::getWhat)
                .collect(Collectors.toSet());

        var missingSlots = new ArrayList<IRecipeSlotView>();
        var craftableSlots = new ArrayList<IRecipeSlotView>();
        for (IRecipeSlotView slotView : slotsView.getSlotViews(RecipeIngredientRole.INPUT)) {
            List<GenericStack> candidates = slotView.getAllIngredients()
                    .map(GenericEntryStackHelper::ingredientToStack)
                    .filter(ResonantEncodePatternTransferHandler::isPresentIngredient)
                    .toList();
            if (candidates.isEmpty()) {
                continue;
            }

            GenericStack availableCandidate = candidates.stream()
                    .filter(stack -> availableAmounts.getOrDefault(stack.what(), 0L) >= stack.amount())
                    .findFirst()
                    .orElse(null);
            if (availableCandidate != null) {
                availableAmounts.merge(availableCandidate.what(), -availableCandidate.amount(), Long::sum);
            } else if (candidates.stream().anyMatch(stack -> craftableKeys.contains(stack.what()))) {
                craftableSlots.add(slotView);
            } else {
                missingSlots.add(slotView);
            }
        }

        return new TransferPreview(missingSlots, craftableSlots);
    }

    private static TransferPreview findEncodingPreview(ResonantTemplateCodingTermMenu menu,
            IRecipeSlotsView slotsView) {
        IClientRepo repo = menu.getClientRepo();
        if (repo == null) {
            return new TransferPreview(List.of(), List.of());
        }

        Set<AEKey> craftableKeys = repo.getAllEntries().stream()
                .filter(entry -> entry.getWhat() != null && entry.isCraftable())
                .map(GridInventoryEntry::getWhat)
                .collect(Collectors.toSet());

        var craftableSlots = new ArrayList<IRecipeSlotView>();
        for (IRecipeSlotView slotView : slotsView.getSlotViews(RecipeIngredientRole.INPUT)) {
            boolean hasCraftableCandidate = slotView.getAllIngredients()
                    .map(GenericEntryStackHelper::ingredientToStack)
                    .filter(ResonantEncodePatternTransferHandler::isPresentIngredient)
                    .anyMatch(stack -> craftableKeys.contains(stack.what()));
            if (hasCraftableCandidate) {
                craftableSlots.add(slotView);
            }
        }

        return new TransferPreview(List.of(), craftableSlots);
    }

    private static boolean isPresentIngredient(GenericStack stack) {
        return stack != null && stack.what() != null && stack.amount() > 0;
    }

    private record TransferPreview(List<IRecipeSlotView> missingSlots, List<IRecipeSlotView> craftableSlots) {
        private boolean anyMissing() {
            return !this.missingSlots.isEmpty();
        }

        private boolean anyCraftable() {
            return !this.craftableSlots.isEmpty();
        }
    }

    private record ErrorRenderer(TransferPreview preview, boolean moveItems) implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public int getButtonHighlightColor() {
            return this.moveItems && this.preview.anyCraftable()
                    ? TransferHelper.BLUE_PLUS_BUTTON_COLOR
                    : 0;
        }

        @Override
        public void showError(GuiGraphics guiGraphics, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView,
                int recipeX, int recipeY) {
            if (!this.moveItems && !this.preview.anyCraftable()) {
                return;
            }

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(recipeX, recipeY, 0.0F);
            if (this.moveItems) {
                for (IRecipeSlotView slotView : this.preview.missingSlots) {
                    slotView.drawHighlight(guiGraphics, TransferHelper.RED_SLOT_HIGHLIGHT_COLOR);
                }
            }
            for (IRecipeSlotView slotView : this.preview.craftableSlots) {
                slotView.drawHighlight(guiGraphics, TransferHelper.BLUE_SLOT_HIGHLIGHT_COLOR);
            }
            poseStack.popPose();
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            var lines = new ArrayList<FormattedText>();
            if (this.moveItems) {
                lines.addAll(TransferHelper.createCraftingTooltip(toMissingIngredientSlots(this.preview), false, true));
            } else {
                lines.addAll(TransferHelper.createEncodingTooltip(this.preview.anyCraftable(), true));
            }
            tooltip.addAll(lines);
        }
    }

    private static appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots toMissingIngredientSlots(
            TransferPreview preview) {
        return new appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots(
                preview.anyMissing() ? Set.of(0) : Set.of(),
                preview.anyCraftable() ? Set.of(0) : Set.of());
    }
}
