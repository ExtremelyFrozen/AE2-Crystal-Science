package io.github.lounode.ae2cs.integration;

import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AELog;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.util.CraftingRecipeUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class ResonantPatternEncodingTransferHelper {

    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator.comparing(GridInventoryEntry::isCraftable)
            .thenComparing(ResonantPatternEncodingTransferHelper::isUndamaged)
            .thenComparing(GridInventoryEntry::getStoredAmount);

    private ResonantPatternEncodingTransferHelper() {}

    public static void encodeCraftingRecipe(ResonantTemplateCodingTermMenu menu, RecipeHolder<?> recipe,
                                            List<List<GenericStack>> genericIngredients, Predicate<ItemStack> visiblePredicate) {
        if (!menu.pullProcessingRecipeInputs) {
            EncodingHelper.encodeCraftingRecipe(menu, recipe, genericIngredients, visiblePredicate);
            return;
        }

        EncodingMode mode = getEncodingMode(recipe);
        menu.prepareTransferMode(mode);
        if (mode == EncodingMode.STONECUTTING) {
            menu.setStonecuttingRecipeId(recipe.id());
        }

        Map<AEKey, Integer> prioritizedNetworkInv = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        NonNullList<ItemStack> encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length,
                ItemStack.EMPTY);
        if (recipe != null) {
            NonNullList<Ingredient> ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe.value());
            for (int slot = 0; slot < ingredients3x3.size(); slot++) {
                Ingredient ingredient = ingredients3x3.get(slot);
                if (ingredient.isEmpty()) {
                    continue;
                }

                Optional<ItemStack> bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream()
                        .filter(entry -> entry.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .map(AEItemKey.class::cast)
                        .map(AEItemKey::toStack);
                ItemStack bestIngredient = bestNetworkIngredient.orElseGet(() -> {
                    for (ItemStack stack : ingredient.getItems()) {
                        if (visiblePredicate.test(stack)) {
                            return stack;
                        }
                    }
                    return ingredient.getItems()[0];
                });
                encodedInputs.set(slot, bestIngredient);
            }
        } else {
            for (int slot = 0; slot < genericIngredients.size() && slot < encodedInputs.size(); slot++) {
                List<GenericStack> genericIngredient = genericIngredients.get(slot);
                if (genericIngredient.isEmpty()) {
                    continue;
                }

                AEKey bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient).what();
                if (bestIngredient instanceof AEItemKey itemKey) {
                    encodedInputs.set(slot, itemKey.toStack());
                } else {
                    encodedInputs.set(slot, GenericStack.wrapInItemStack(bestIngredient, 1));
                }
            }
        }

        for (int i = 0; i < encodedInputs.size(); i++) {
            sendSetFilter(menu.getCraftingGridSlots()[i], encodedInputs.get(i));
        }
        for (FakeSlot outputSlot : menu.getProcessingOutputSlots()) {
            sendSetFilter(outputSlot, ItemStack.EMPTY);
        }

        menu.pullTransferToGrid(mode);
    }

    public static void transferCraftingRecipeToRealGrid(ResonantTemplateCodingTermMenu menu, RecipeHolder<?> recipe,
                                                        Predicate<ItemStack> visiblePredicate, boolean craftMissing) {
        ResourceLocation recipeId = recipe != null ? recipe.id() : null;
        if (recipeId != null && menu.getPlayer().level().getRecipeManager().byKey(recipeId).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }

        NonNullList<ItemStack> templateItems = findGoodTemplateItems(menu, recipe, visiblePredicate);
        ServerboundPacket packet = new FillCraftingGridFromRecipePacket(recipeId, templateItems, craftMissing);
        PacketDistributor.sendToServer((CustomPacketPayload) packet);
    }

    public static boolean encodeCraftingLikeRecipeToRealGrid(ResonantTemplateCodingTermMenu menu,
                                                             RecipeHolder<?> recipe, List<List<GenericStack>> genericIngredients) {
        if (!menu.pullProcessingRecipeInputs || recipe == null) {
            return false;
        }

        EncodingMode mode = getEncodingMode(recipe);
        if (mode == EncodingMode.CRAFTING) {
            return false;
        }

        menu.prepareTransferMode(mode);
        if (mode == EncodingMode.STONECUTTING) {
            menu.setStonecuttingRecipeId(recipe.id());
        }

        Map<AEKey, Integer> prioritizedNetworkInv = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        Slot[] encodedSlots = menu.getEncodingInputSlotsForMode(mode);
        for (int slot = 0; slot < encodedSlots.length; slot++) {
            ItemStack encodedInput = ItemStack.EMPTY;
            if (slot < genericIngredients.size()) {
                List<GenericStack> genericIngredient = genericIngredients.get(slot);
                if (!genericIngredient.isEmpty()) {
                    GenericStack bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient);
                    if (bestIngredient != null) {
                        encodedInput = GenericStack.wrapInItemStack(bestIngredient);
                    }
                }
            }

            if (encodedSlots[slot] instanceof FakeSlot fakeSlot) {
                sendSetFilter(fakeSlot, encodedInput);
            }
        }

        menu.pullTransferToGrid(mode);
        return true;
    }

    public static boolean isNonCraftingRealGridRecipe(RecipeHolder<?> recipe) {
        return recipe != null && getEncodingMode(recipe) != EncodingMode.CRAFTING;
    }

    private static NonNullList<ItemStack> findGoodTemplateItems(ResonantTemplateCodingTermMenu menu,
                                                                RecipeHolder<?> recipe, Predicate<ItemStack> visiblePredicate) {
        Map<AEKey, Integer> prioritizedNetworkInv = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        NonNullList<ItemStack> templateItems = NonNullList.withSize(menu.getCraftingGridSlots().length,
                ItemStack.EMPTY);
        if (recipe == null) {
            return templateItems;
        }

        NonNullList<Ingredient> ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe.value());
        for (int slot = 0; slot < ingredients3x3.size() && slot < templateItems.size(); slot++) {
            Ingredient ingredient = ingredients3x3.get(slot);
            if (ingredient.isEmpty()) {
                continue;
            }

            Optional<ItemStack> bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream()
                    .filter(entry -> entry.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .map(AEItemKey.class::cast)
                    .map(AEItemKey::toStack);
            ItemStack bestIngredient = bestNetworkIngredient.orElseGet(() -> {
                for (ItemStack stack : ingredient.getItems()) {
                    if (visiblePredicate.test(stack)) {
                        return stack;
                    }
                }
                return ingredient.getItems()[0];
            });
            templateItems.set(slot, bestIngredient);
        }
        return templateItems;
    }

    public static void encodeProcessingRecipe(ResonantTemplateCodingTermMenu menu,
                                              List<List<GenericStack>> genericIngredients, List<GenericStack> genericResults) {
        encodeProcessingRecipe(menu, genericIngredients, genericResults, false);
    }

    public static void encodeProcessingRecipe(ResonantTemplateCodingTermMenu menu,
                                              List<List<GenericStack>> genericIngredients, List<GenericStack> genericResults, boolean craftMissing) {
        if (!menu.pullProcessingRecipeInputs) {
            menu.setMode(EncodingMode.PROCESSING);
        } else {
            menu.prepareTransferMode(EncodingMode.PROCESSING);
        }
        Map<AEKey, Integer> ingredientPriorities = menu.pullProcessingRecipeInputs ? EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR) : Map.of();

        var transferMode = menu.pullProcessingRecipeInputs ? ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE : menu.processingIngredientTransferMode;
        List<GenericStack> inputs = switch (transferMode) {
            case MERGE -> mergeIngredients(genericIngredients, ingredientPriorities);
            case PARTIAL_SPLIT -> partiallySplitIngredients(genericIngredients, ingredientPriorities);
            case FULL_SPLIT -> fullySplitIngredients(genericIngredients, ingredientPriorities);
        };

        encodeStacksIntoSlots(inputs, menu.getProcessingInputSlots());
        encodeBestMatchingStacksIntoSlotsWithoutMerging(genericResults.stream().map(List::of).toList(),
                ingredientPriorities, menu.getProcessingOutputSlots());
        if (menu.pullProcessingRecipeInputs) {
            if (craftMissing) {
                menu.pullProcessingTransferToGridAndRequestAutoCraft();
            } else {
                menu.pullTransferToGrid(EncodingMode.PROCESSING);
            }
        }
    }

    private static EncodingMode getEncodingMode(RecipeHolder<?> recipe) {
        if (recipe != null && recipe.value().getType() == RecipeType.STONECUTTING) {
            return EncodingMode.STONECUTTING;
        }
        if (recipe != null && recipe.value().getType() == RecipeType.SMITHING) {
            return EncodingMode.SMITHING_TABLE;
        }
        return EncodingMode.CRAFTING;
    }

    private static Boolean isUndamaged(GridInventoryEntry entry) {
        AEKey key = entry.getWhat();
        return !(key instanceof appeng.api.stacks.AEItemKey itemKey) || !itemKey.isDamaged();
    }

    private static void encodeBestMatchingStacksIntoSlotsWithoutMerging(List<List<GenericStack>> possibleInputsBySlot,
                                                                        Map<AEKey, Integer> ingredientPriorities, FakeSlot[] slots) {
        encodeStacksIntoSlots(fullySplitIngredients(possibleInputsBySlot, ingredientPriorities), slots);
    }

    private static List<GenericStack> fullySplitIngredients(List<List<GenericStack>> possibleInputsBySlot,
                                                            Map<AEKey, Integer> ingredientPriorities) {
        return possibleInputsBySlot.stream()
                .map(possibleIngredients -> possibleIngredients.stream()
                        .filter(ResonantPatternEncodingTransferHelper::isPresentIngredient)
                        .toList())
                .filter(possibleIngredients -> !possibleIngredients.isEmpty())
                .map(possibleIngredients -> findBestIngredient(ingredientPriorities, possibleIngredients))
                .toList();
    }

    private static List<GenericStack> partiallySplitIngredients(List<List<GenericStack>> possibleInputsBySlot,
                                                                Map<AEKey, Integer> ingredientPriorities) {
        List<GenericStack> fullySplit = fullySplitIngredients(possibleInputsBySlot, ingredientPriorities);
        if (fullySplit.isEmpty()) {
            return List.of();
        }

        ArrayList<GenericStack> result = new ArrayList<>();
        GenericStack current = fullySplit.getFirst();
        for (int i = 1; i < fullySplit.size(); i++) {
            GenericStack next = fullySplit.get(i);
            if (next.what().equals(current.what())) {
                current = new GenericStack(current.what(), current.amount() + next.amount());
            } else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }

    private static List<GenericStack> mergeIngredients(List<List<GenericStack>> possibleInputsBySlot,
                                                       Map<AEKey, Integer> ingredientPriorities) {
        ArrayList<GenericStack> result = new ArrayList<>();
        for (GenericStack stack : fullySplitIngredients(possibleInputsBySlot, ingredientPriorities)) {
            addOrMerge(result, stack);
        }
        return result;
    }

    private static void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        for (int i = 0; i < stacks.size(); i++) {
            GenericStack existingStack = stacks.get(i);
            if (!Objects.equals(existingStack.what(), newStack.what())) {
                continue;
            }

            long mergedAmount = saturatedAdd(existingStack.amount(), newStack.amount());
            stacks.set(i, new GenericStack(existingStack.what(), mergedAmount));
            return;
        }
        stacks.add(newStack);
    }

    private static long saturatedAdd(long a, long b) {
        long result = a + b;
        if (((a ^ result) & (b ^ result)) < 0) {
            return Long.MAX_VALUE;
        }
        return result;
    }

    private static void encodeStacksIntoSlots(List<GenericStack> stacks, FakeSlot[] slots) {
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = i < stacks.size() ? GenericStack.wrapInItemStack(stacks.get(i)) : ItemStack.EMPTY;

            sendSetFilter(slots[i], stack);
        }
    }

    private static void sendSetFilter(FakeSlot slot, ItemStack stack) {
        PacketDistributor.sendToServer((CustomPacketPayload) new InventoryActionPacket(
                InventoryAction.SET_FILTER, slot.index, stack));
    }

    private static GenericStack findBestIngredient(Map<AEKey, Integer> ingredientPriorities,
                                                   List<GenericStack> possibleIngredients) {
        return possibleIngredients.stream()
                .max(Comparator.comparingInt(stack -> ingredientPriorities.getOrDefault(stack.what(), Integer.MIN_VALUE)))
                .orElseThrow();
    }

    private static boolean isPresentIngredient(GenericStack stack) {
        return stack != null && stack.what() != null && stack.amount() > 0;
    }
}
