package io.github.lounode.ae2cs.integration.emi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import appeng.core.localization.ItemModText;
import appeng.core.AEConfig;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import io.github.lounode.ae2cs.integration.ResonantPatternEncodingTransferHelper;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public class ResonantEmiEncodePatternHandler implements StandardRecipeHandler<ResonantTemplateCodingTermMenu> {
    @Override
    public List<Slot> getInputSources(ResonantTemplateCodingTermMenu menu) {
        var slots = new ArrayList<Slot>();
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_INVENTORY));
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_HOTBAR));
        slots.addAll(menu.getSlots(SlotSemantics.CRAFTING_GRID));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(ResonantTemplateCodingTermMenu menu) {
        return menu.getSlots(SlotSemantics.CRAFTING_GRID);
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ResonantTemplateCodingTermMenu> screen) {
        var stacks = new ArrayList<EmiStack>(getInputSources(screen.getMenu()).stream()
                .map(Slot::getItem)
                .map(EmiStack::of)
                .toList());
        ResonantTemplateCodingTermMenu menu = screen.getMenu();
        IClientRepo repo = menu.getClientRepo();
        if (menu.pullProcessingRecipeInputs && AEConfig.instance().isExposeNetworkInventoryToEmi() && repo != null) {
            for (GridInventoryEntry entry : repo.getAllEntries()) {
                if (entry.getStoredAmount() <= 0 || entry.getWhat() == null) {
                    continue;
                }

                EmiStack stack = EmiStackHelper.toEmiStack(new GenericStack(entry.getWhat(), entry.getStoredAmount()));
                if (stack != null) {
                    stacks.add(stack);
                }
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public List<Slot> getCraftingSlots(EmiRecipe recipe, ResonantTemplateCodingTermMenu menu) {
        RecipeHolder<?> holder = getRecipeHolder(menu, recipe);
        Recipe<?> backingRecipe = holder != null ? holder.value() : null;
        if (menu.pullProcessingRecipeInputs) {
            if (isCraftingRecipe(backingRecipe, recipe)) {
                return getPulledCraftingSlots(backingRecipe, recipe, menu);
            }
            return Arrays.asList(menu.getPulledProcessingInputSlots());
        }
        return getCraftingSlots(menu);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ResonantTemplateCodingTermMenu> context) {
        if (context.getType() != EmiCraftContext.Type.FILL_BUTTON) {
            return false;
        }

        RecipeHolder<?> holder = getRecipeHolder(context.getScreenHandler(), recipe);
        Recipe<?> backingRecipe = holder != null ? holder.value() : null;
        if (context.getScreenHandler().pullProcessingRecipeInputs
                && ResonantPatternEncodingTransferHelper.isNonCraftingRealGridRecipe(holder)) {
            return true;
        }
        return !isCraftingRecipe(backingRecipe, recipe) || fitsIn3x3Grid(holder);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ResonantTemplateCodingTermMenu> context) {
        if (!canCraft(recipe, context)) {
            return false;
        }

        ResonantTemplateCodingTermMenu menu = context.getScreenHandler();
        RecipeHolder<?> holder = getRecipeHolder(menu, recipe);
        Recipe<?> backingRecipe = holder != null ? holder.value() : null;

        if (menu.pullProcessingRecipeInputs
                && ResonantPatternEncodingTransferHelper.isNonCraftingRealGridRecipe(holder)) {
            ResonantPatternEncodingTransferHelper.encodeCraftingLikeRecipeToRealGrid(menu, holder,
                    EmiStackHelper.ofInputs(recipe));
        } else if (isCraftingRecipe(backingRecipe, recipe)) {
            if (menu.pullProcessingRecipeInputs) {
                ResonantPatternEncodingTransferHelper.transferCraftingRecipeToRealGrid(menu, holder, stack -> true,
                        AbstractContainerScreen.hasControlDown());
            } else {
                ResonantPatternEncodingTransferHelper.encodeCraftingRecipe(menu, holder,
                        getGuiIngredientsForCrafting(recipe), stack -> true);
            }
        } else {
            List<List<GenericStack>> inputs = EmiStackHelper.ofInputs(recipe);
            if (!ResonantPatternEncodingTransferHelper.encodeCraftingLikeRecipeToRealGrid(menu, holder, inputs)) {
                ResonantPatternEncodingTransferHelper.encodeProcessingRecipe(menu, inputs, EmiStackHelper.ofOutputs(recipe),
                        AbstractContainerScreen.hasControlDown());
            }
        }

        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe,
            EmiCraftContext<ResonantTemplateCodingTermMenu> context) {
        ResonantTemplateCodingTermMenu menu = context.getScreenHandler();
        ProcessingTransferPreview preview = menu.pullProcessingRecipeInputs
                ? getProcessingTransferPreview(menu, recipe.getInputs())
                : getEncodingPreview(menu, recipe.getInputs());
        if (!menu.pullProcessingRecipeInputs && !preview.anyCraftable()) {
            return StandardRecipeHandler.super.getTooltip(recipe, context);
        }
        if (menu.pullProcessingRecipeInputs && !preview.anyMissingOrCraftable()) {
            return StandardRecipeHandler.super.getTooltip(recipe, context);
        }

        var lines = new ArrayList<Component>();
        if (menu.pullProcessingRecipeInputs) {
            lines.addAll(TransferHelper.createCraftingTooltip(toMissingIngredientSlots(preview), false, false));
        } else {
            lines.addAll(TransferHelper.createEncodingTooltip(preview.anyCraftable(), false));
        }
        if (menu.pullProcessingRecipeInputs && preview.anyMissing()) {
            lines.add(ItemModText.MISSING_ITEMS.text().withStyle(ChatFormatting.RED));
        }

        return lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<ResonantTemplateCodingTermMenu> context, List<Widget> widgets,
            GuiGraphics draw) {
        StandardRecipeHandler.super.render(recipe, context, widgets, draw);

        ResonantTemplateCodingTermMenu menu = context.getScreenHandler();
        ProcessingTransferPreview preview = menu.pullProcessingRecipeInputs
                ? getProcessingTransferPreview(menu, recipe.getInputs())
                : getEncodingPreview(menu, recipe.getInputs());
        if ((!menu.pullProcessingRecipeInputs && !preview.anyCraftable())
                || (menu.pullProcessingRecipeInputs && !preview.anyMissingOrCraftable())) {
            return;
        }

        for (Widget widget : widgets) {
            if (!(widget instanceof SlotWidget slot) || slot.getRecipe() != null) {
                continue;
            }

            int inputIndex = getRecipeInputIndex(recipe, slot);
            boolean missing = menu.pullProcessingRecipeInputs && preview.missingSlots.contains(inputIndex);
            boolean craftable = preview.craftableSlots.contains(inputIndex);
            if (!missing && !craftable) {
                continue;
            }

            PoseStack poseStack = draw.pose();
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 400.0F);
            Bounds bounds = slot.getBounds();
            draw.fill(bounds.x() + 1, bounds.y() + 1, bounds.right() - 1, bounds.bottom() - 1,
                    missing ? TransferHelper.RED_SLOT_HIGHLIGHT_COLOR : TransferHelper.BLUE_SLOT_HIGHLIGHT_COLOR);
            poseStack.popPose();
        }
    }

    private static ProcessingTransferPreview getProcessingTransferPreview(ResonantTemplateCodingTermMenu menu,
            List<EmiIngredient> ingredients) {
        IClientRepo repo = menu.getClientRepo();
        Map<AEKey, Long> availableAmounts = new HashMap<>();
        Set<AEKey> craftableKeys = repo == null
                ? Collections.emptySet()
                : repo.getAllEntries().stream()
                    .peek(entry -> {
                        if (entry.getWhat() != null && entry.getStoredAmount() > 0) {
                            availableAmounts.merge(entry.getWhat(), entry.getStoredAmount(), Long::sum);
                        }
                    })
                    .filter(entry -> entry.getWhat() != null && entry.isCraftable())
                    .map(GridInventoryEntry::getWhat)
                    .collect(Collectors.toSet());

        var missingSlots = new HashSet<Integer>();
        var craftableSlots = new HashSet<Integer>();
        for (int i = 0; i < ingredients.size(); i++) {
            List<GenericStack> candidates = ingredients.get(i).getEmiStacks().stream()
                    .map(EmiStackHelper::toGenericStack)
                    .filter(ResonantEmiEncodePatternHandler::isPresentIngredient)
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
                craftableSlots.add(i);
            } else {
                missingSlots.add(i);
            }
        }

        return new ProcessingTransferPreview(missingSlots, craftableSlots);
    }

    private static ProcessingTransferPreview getEncodingPreview(ResonantTemplateCodingTermMenu menu,
            List<EmiIngredient> ingredients) {
        IClientRepo repo = menu.getClientRepo();
        Set<AEKey> craftableKeys = repo == null
                ? Collections.emptySet()
                : repo.getAllEntries().stream()
                    .filter(entry -> entry.getWhat() != null && entry.isCraftable())
                    .map(GridInventoryEntry::getWhat)
                    .collect(Collectors.toSet());

        var craftableSlots = new HashSet<Integer>();
        for (int i = 0; i < ingredients.size(); i++) {
            boolean hasCraftableCandidate = ingredients.get(i).getEmiStacks().stream()
                    .map(EmiStackHelper::toGenericStack)
                    .filter(ResonantEmiEncodePatternHandler::isPresentIngredient)
                    .anyMatch(stack -> craftableKeys.contains(stack.what()));
            if (hasCraftableCandidate) {
                craftableSlots.add(i);
            }
        }

        return new ProcessingTransferPreview(Collections.emptySet(), craftableSlots);
    }

    private static boolean isCraftingRecipe(Recipe<?> recipe, EmiRecipe emiRecipe) {
        return EncodingHelper.isSupportedCraftingRecipe(recipe)
                || emiRecipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING);
    }

    private static List<Slot> getPulledCraftingSlots(Recipe<?> recipe, EmiRecipe emiRecipe,
            ResonantTemplateCodingTermMenu menu) {
        if (recipe != null && recipe.getType() == RecipeType.SMITHING) {
            return Arrays.asList(menu.getPulledSmithingInputSlots());
        }
        if (recipe != null && recipe.getType() == RecipeType.STONECUTTING) {
            return List.of(menu.getPulledStonecuttingInputSlot());
        }
        if (emiRecipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING)) {
            return Arrays.asList(menu.getPulledCraftingInputSlots());
        }
        return Arrays.asList(menu.getPulledCraftingInputSlots());
    }

    private static int getRecipeInputIndex(EmiRecipe recipe, SlotWidget slot) {
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            if (slot.getStack() == recipe.getInputs().get(i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isPresentIngredient(GenericStack stack) {
        return stack != null && stack.what() != null && stack.amount() > 0;
    }

    private static appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots toMissingIngredientSlots(
            ProcessingTransferPreview preview) {
        return new appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots(
                preview.missingSlots(),
                preview.craftableSlots());
    }

    private record ProcessingTransferPreview(Set<Integer> missingSlots, Set<Integer> craftableSlots) {
        private boolean anyMissingOrCraftable() {
            return anyMissing() || anyCraftable();
        }

        private boolean anyMissing() {
            return !this.missingSlots.isEmpty();
        }

        private boolean anyCraftable() {
            return !this.craftableSlots.isEmpty();
        }
    }

    private static boolean fitsIn3x3Grid(ResonantTemplateCodingTermMenu menu, EmiRecipe recipe) {
        return fitsIn3x3Grid(getRecipeHolder(menu, recipe));
    }

    private static boolean fitsIn3x3Grid(RecipeHolder<?> holder) {
        return holder == null || holder.value().canCraftInDimensions(3, 3);
    }

    private static RecipeHolder<?> getRecipeHolder(ResonantTemplateCodingTermMenu menu, EmiRecipe recipe) {
        if (recipe.getBackingRecipe() != null) {
            return recipe.getBackingRecipe();
        }

        ResourceLocation id = recipe.getId();
        if (id != null) {
            return menu.getPlayer().level().getRecipeManager().byKey(id).orElse(null);
        }

        return null;
    }

    private static List<List<GenericStack>> getGuiIngredientsForCrafting(EmiRecipe emiRecipe) {
        var result = new ArrayList<List<GenericStack>>(9);
        for (int i = 0; i < 9; i++) {
            var stacks = new ArrayList<GenericStack>();
            if (i < emiRecipe.getInputs().size()) {
                EmiIngredient ingredient = emiRecipe.getInputs().get(i);
                for (EmiStack emiStack : ingredient.getEmiStacks()) {
                    GenericStack genericStack = EmiStackHelper.toGenericStack(emiStack);
                    if (genericStack != null && genericStack.what() instanceof AEItemKey) {
                        stacks.add(genericStack);
                    }
                }
            }
            result.add(stacks);
        }
        return result;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }
}
