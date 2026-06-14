package io.github.lounode.ae2cs.common.menu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import appeng.helpers.InventoryAction;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.part.IResonantTemplateCodingTerminalHost;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 谐振样板编码终端菜单 — 继承原版编码逻辑，处理样板模式使用 4×4 可视输入网格。
 * 继承 PatternEncodingTermMenu 获得完整编码逻辑。
 */
public class ResonantTemplateCodingTermMenu extends PatternEncodingTermMenu {
    private static final java.util.Comparator<GridInventoryEntry> ENTRY_COMPARATOR =
            java.util.Comparator.comparing(GridInventoryEntry::isCraftable)
                    .thenComparing(ResonantTemplateCodingTermMenu::isUndamaged)
                    .thenComparing(GridInventoryEntry::getStoredAmount);
    private static final String ACTION_SET_RESONATING_PATTERN_ENCODING = "setResonatingPatternEncoding";
    private static final String ACTION_SET_PULL_PROCESSING_RECIPE_INPUTS = "setPullProcessingRecipeInputs";
    private static final String ACTION_PULL_ENCODED_INPUTS_TO_GRID = "pullEncodedInputsToGrid";
    private static final String ACTION_CLEAR_PULLED_INPUTS_TO_NETWORK = "clearPulledInputsToNetwork";
    private static final String ACTION_CLEAR_PULLED_INPUTS_TO_PLAYER = "clearPulledInputsToPlayer";
    private static final String ACTION_SET_PULLED_STONECUTTING_RECIPE_ID = "setPulledStonecuttingRecipeId";
    private static final String ACTION_PREPARE_TRANSFER_MODE = "prepareTransferMode";
    private static final String ACTION_PULL_TRANSFER_TO_GRID = "pullTransferToGrid";
    private static final String ACTION_PULL_PROCESSING_TRANSFER_TO_GRID = "pullProcessingTransferToGrid";
    private static final String ACTION_SET_PROCESSING_INGREDIENT_TRANSFER_MODE = "setProcessingIngredientTransferMode";
    private static final String ACTION_CYCLE_PROCESSING_INGREDIENT_TRANSFER_MODE = "cycleProcessingIngredientTransferMode";
    private static final String ACTION_SET_PULLED_ANVIL_MODE = "setPulledAnvilMode";
    private static final String ACTION_SET_PULLED_ANVIL_ITEM_NAME = "setPulledAnvilItemName";
    private static final String ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_NETWORK = "clearPulledAnvilInputsToNetwork";
    private static final String ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_PLAYER = "clearPulledAnvilInputsToPlayer";
    private static final Field ENCODING_LOGIC_FIELD = getEncodingLogicField();
    private static final Field STONECUTTING_INPUT_SLOT_FIELD = getSlotField("stonecuttingInputSlot");
    public static final SlotSemantic PULLED_CRAFTING_INPUTS =
            SlotSemantics.register("AE2CS_PULLED_CRAFTING_INPUTS", false);
    public static final SlotSemantic PULLED_CRAFTING_RESULT =
            SlotSemantics.register("AE2CS_PULLED_CRAFTING_RESULT", false);
    public static final SlotSemantic PULLED_PROCESSING_INPUTS =
            SlotSemantics.register("AE2CS_PULLED_PROCESSING_INPUTS", false);
    public static final SlotSemantic PULLED_SMITHING_TABLE_INPUTS =
            SlotSemantics.register("AE2CS_PULLED_SMITHING_TABLE_INPUTS", false);
    public static final SlotSemantic PULLED_SMITHING_TABLE_RESULT =
            SlotSemantics.register("AE2CS_PULLED_SMITHING_TABLE_RESULT", false);
    public static final SlotSemantic PULLED_STONECUTTING_INPUT =
            SlotSemantics.register("AE2CS_PULLED_STONECUTTING_INPUT", false);
    public static final SlotSemantic PULLED_STONECUTTING_RESULT =
            SlotSemantics.register("AE2CS_PULLED_STONECUTTING_RESULT", false);
    public static final SlotSemantic PULLED_ANVIL_INPUTS =
            SlotSemantics.register("AE2CS_PULLED_ANVIL_INPUTS", false);
    public static final SlotSemantic PULLED_ANVIL_RESULT =
            SlotSemantics.register("AE2CS_PULLED_ANVIL_RESULT", false);

    private final AppEngInternalInventory pulledCraftingInputInv;
    private final AppEngSlot[] pulledCraftingInputSlots;
    private final SimpleContainer pulledCraftingResultInv = new SimpleContainer(1);
    private final Slot pulledCraftingResultSlot;
    private final AppEngInternalInventory pulledProcessingInputInv;
    private final AppEngSlot[] pulledProcessingInputSlots;
    private final AppEngInternalInventory pulledSmithingInputInv;
    private final AppEngSlot[] pulledSmithingInputSlots;
    private final SimpleContainer pulledSmithingResultInv = new SimpleContainer(1);
    private final Slot pulledSmithingResultSlot;
    private final AppEngInternalInventory pulledStonecuttingInputInv;
    private final AppEngSlot pulledStonecuttingInputSlot;
    private final SimpleContainer pulledStonecuttingResultInv = new SimpleContainer(1);
    private final Slot pulledStonecuttingResultSlot;
    private final AppEngInternalInventory pulledAnvilInputInv;
    private final AppEngSlot[] pulledAnvilInputSlots;
    private final SimpleContainer pulledAnvilResultInv = new SimpleContainer(1);
    private final Slot pulledAnvilResultSlot;
    private final IResonantTemplateCodingTerminalHost resonantHost;
    private long lastPulledSmithingSoundTime = -1;
    private long lastPulledStonecuttingSoundTime = -1;
    private long lastPulledAnvilSoundTime = -1;
    private boolean suppressPulledInputSync = false;
    private ItemStack lastPulledStonecuttingInputForRecipes = ItemStack.EMPTY;

    @GuiSync(93)
    public boolean encodeResonatingPattern = false;
    @GuiSync(92)
    public ProcessingIngredientTransferMode processingIngredientTransferMode = ProcessingIngredientTransferMode.MERGE;
    @GuiSync(91)
    public boolean pullProcessingRecipeInputs = false;
    @GuiSync(90)
    public int pulledAnvilCost = 0;
    @GuiSync(89)
    public boolean pulledAnvilMode = false;
    @GuiSync(88)
    public String pulledAnvilItemName = "";

    public ResonantTemplateCodingTermMenu(int id, Inventory ip, IPatternTerminalMenuHost host) {
        this(AECSMenus.RESONANT_TEMPLATE_CODING_TERM_MENU.get(), id, ip, host, true);
    }

    public ResonantTemplateCodingTermMenu(MenuType<?> menuType, int id, Inventory ip,
            IPatternTerminalMenuHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.resonantHost = host instanceof IResonantTemplateCodingTerminalHost typedHost ? typedHost : null;
        this.pullProcessingRecipeInputs = this.resonantHost != null
                && this.resonantHost.isPullRecipeInputsToRealGrid();
        this.pulledAnvilMode = this.pullProcessingRecipeInputs && this.resonantHost != null
                && this.resonantHost.isPulledAnvilMode();
        this.pulledCraftingInputInv = this.resonantHost != null
                ? this.resonantHost.getPulledCraftingInputInv()
                : new AppEngInternalInventory(this.getCraftingGridSlots().length);
        this.pulledCraftingInputSlots = new AppEngSlot[this.getCraftingGridSlots().length];
        for (int i = 0; i < this.pulledCraftingInputSlots.length; i++) {
            AppEngSlot slot = new PulledInputSlot(this.pulledCraftingInputInv, i, EncodingMode.CRAFTING);
            slot.setActive(false);
            this.pulledCraftingInputSlots[i] = slot;
            this.addSlot(slot, PULLED_CRAFTING_INPUTS);
        }
        this.pulledCraftingResultSlot = this.addSlot(new PulledResultSlot(this.pulledCraftingResultInv, 0,
                EncodingMode.CRAFTING), PULLED_CRAFTING_RESULT);

        this.pulledProcessingInputInv = this.resonantHost != null
                ? this.resonantHost.getPulledProcessingInputInv()
                : new AppEngInternalInventory(this.getProcessingInputSlots().length);
        this.pulledProcessingInputSlots = new AppEngSlot[this.getProcessingInputSlots().length];
        for (int i = 0; i < this.pulledProcessingInputSlots.length; i++) {
            AppEngSlot slot = new PulledInputSlot(this.pulledProcessingInputInv, i, EncodingMode.PROCESSING);
            slot.setActive(false);
            this.pulledProcessingInputSlots[i] = slot;
            this.addSlot(slot, PULLED_PROCESSING_INPUTS);
        }

        this.pulledSmithingInputInv = this.resonantHost != null
                ? this.resonantHost.getPulledSmithingInputInv()
                : new AppEngInternalInventory(3);
        this.pulledSmithingInputSlots = new AppEngSlot[3];
        for (int i = 0; i < this.pulledSmithingInputSlots.length; i++) {
            AppEngSlot slot = new PulledSmithingInputSlot(this.pulledSmithingInputInv, i);
            slot.setActive(false);
            this.pulledSmithingInputSlots[i] = slot;
            this.addSlot(slot, PULLED_SMITHING_TABLE_INPUTS);
        }
        this.pulledSmithingResultSlot = this.addSlot(new PulledResultSlot(this.pulledSmithingResultInv, 0,
                EncodingMode.SMITHING_TABLE), PULLED_SMITHING_TABLE_RESULT);

        this.pulledStonecuttingInputInv = this.resonantHost != null
                ? this.resonantHost.getPulledStonecuttingInputInv()
                : new AppEngInternalInventory(1);
        this.pulledStonecuttingInputSlot = new PulledInputSlot(this.pulledStonecuttingInputInv, 0,
                EncodingMode.STONECUTTING);
        this.pulledStonecuttingInputSlot.setActive(false);
        this.addSlot(this.pulledStonecuttingInputSlot, PULLED_STONECUTTING_INPUT);
        this.pulledStonecuttingResultSlot = this.addSlot(new PulledResultSlot(this.pulledStonecuttingResultInv, 0,
                EncodingMode.STONECUTTING), PULLED_STONECUTTING_RESULT);

        this.pulledAnvilInputInv = this.resonantHost != null
                ? this.resonantHost.getPulledAnvilInputInv()
                : new AppEngInternalInventory(2);
        this.pulledAnvilInputSlots = new AppEngSlot[2];
        for (int i = 0; i < this.pulledAnvilInputSlots.length; i++) {
            AppEngSlot slot = new PulledAnvilInputSlot(this.pulledAnvilInputInv, i);
            slot.setActive(false);
            this.pulledAnvilInputSlots[i] = slot;
            this.addSlot(slot, PULLED_ANVIL_INPUTS);
        }
        this.pulledAnvilResultSlot = this.addSlot(new PulledAnvilResultSlot(this.pulledAnvilResultInv, 0),
                PULLED_ANVIL_RESULT);

        this.registerClientAction(ACTION_SET_RESONATING_PATTERN_ENCODING, Boolean.class,
                this::setEncodeResonatingPattern);
        this.registerClientAction(ACTION_SET_PULL_PROCESSING_RECIPE_INPUTS, Boolean.class,
                this::setPullProcessingRecipeInputs);
        this.registerClientAction(ACTION_PULL_ENCODED_INPUTS_TO_GRID, this::pullEncodedInputsToGrid);
        this.registerClientAction(ACTION_CLEAR_PULLED_INPUTS_TO_NETWORK, this::clearPulledInputsToNetwork);
        this.registerClientAction(ACTION_CLEAR_PULLED_INPUTS_TO_PLAYER, this::clearPulledInputsToPlayer);
        this.registerClientAction(ACTION_SET_PULLED_STONECUTTING_RECIPE_ID, ResourceLocation.class,
                this::setStonecuttingRecipeId);
        this.registerClientAction(ACTION_PREPARE_TRANSFER_MODE, EncodingMode.class, this::prepareTransferMode);
        this.registerClientAction(ACTION_PULL_TRANSFER_TO_GRID, EncodingMode.class, this::pullTransferToGrid);
        this.registerClientAction(ACTION_PULL_PROCESSING_TRANSFER_TO_GRID, this::pullProcessingTransferToGrid);
        this.registerClientAction(ACTION_SET_PROCESSING_INGREDIENT_TRANSFER_MODE,
                ProcessingIngredientTransferMode.class, this::setProcessingIngredientTransferMode);
        this.registerClientAction(ACTION_CYCLE_PROCESSING_INGREDIENT_TRANSFER_MODE,
                this::cycleProcessingIngredientTransferMode);
        this.registerClientAction(ACTION_SET_PULLED_ANVIL_MODE, Boolean.class, this::setPulledAnvilMode);
        this.registerClientAction(ACTION_SET_PULLED_ANVIL_ITEM_NAME, String.class, this::setPulledAnvilItemName);
        this.registerClientAction(ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_NETWORK, this::clearPulledAnvilInputsToNetwork);
        this.registerClientAction(ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_PLAYER, this::clearPulledAnvilInputsToPlayer);
        if (this.isServerSide()) {
            syncCurrentPulledInputsToEncodedSlots();
            this.pulledAnvilItemName = this.pulledAnvilMode ? getPulledAnvilInputName() : "";
            updatePulledResult();
        }
    }

    @Override
    public void encode() {
        super.encode();

        if (this.isServerSide() && this.encodeResonatingPattern && this.getMode() == EncodingMode.PROCESSING) {
            convertEncodedOutputToResonatingPattern();
        }
    }

    public void setEncodeResonatingPattern(boolean encodeResonatingPattern) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_RESONATING_PATTERN_ENCODING, encodeResonatingPattern);
        } else {
            this.encodeResonatingPattern = encodeResonatingPattern;
        }
    }

    public void setPullProcessingRecipeInputs(boolean pullProcessingRecipeInputs) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_PULL_PROCESSING_RECIPE_INPUTS, pullProcessingRecipeInputs);
        } else {
            this.pullProcessingRecipeInputs = pullProcessingRecipeInputs;
            if (this.resonantHost != null) {
                this.resonantHost.setPullRecipeInputsToRealGrid(pullProcessingRecipeInputs);
            }
            if (!pullProcessingRecipeInputs) {
                this.pulledAnvilMode = false;
                this.pulledAnvilItemName = "";
                if (this.resonantHost != null) {
                    this.resonantHost.setPulledAnvilMode(false);
                }
                clearPulledInputsToPlayer(this.getPlayer());
            } else {
                pullEncodedInputsToGrid();
            }
        }
    }

    @Override
    public void setMode(EncodingMode mode) {
        if (this.isServerSide() && mode != this.getMode()) {
            if (this.pullProcessingRecipeInputs) {
                if (this.pulledAnvilMode) {
                    setPulledAnvilMode(false);
                } else {
                    clearCurrentPulledModeInputsToPlayer(this.getPlayer());
                }
            } else {
                clearPulledInputsToPlayer(this.getPlayer());
            }
        }
        super.setMode(mode);
    }

    public AppEngSlot[] getPulledCraftingInputSlots() {
        return this.pulledCraftingInputSlots;
    }

    public Slot getPulledCraftingResultSlot() {
        return this.pulledCraftingResultSlot;
    }

    public AppEngSlot[] getPulledProcessingInputSlots() {
        return this.pulledProcessingInputSlots;
    }

    public AppEngSlot[] getPulledSmithingInputSlots() {
        return this.pulledSmithingInputSlots;
    }

    public Slot getPulledSmithingResultSlot() {
        return this.pulledSmithingResultSlot;
    }

    public AppEngSlot getPulledStonecuttingInputSlot() {
        return this.pulledStonecuttingInputSlot;
    }

    public Slot getPulledStonecuttingResultSlot() {
        return this.pulledStonecuttingResultSlot;
    }

    public AppEngSlot[] getPulledAnvilInputSlots() {
        return this.pulledAnvilInputSlots;
    }

    public Slot getPulledAnvilResultSlot() {
        return this.pulledAnvilResultSlot;
    }

    public String getPulledAnvilItemName() {
        return this.pulledAnvilItemName;
    }

    public boolean isPulledAnvilTooExpensive() {
        Player player = this.getPlayer();
        return this.pulledAnvilCost >= 40 && !player.getAbilities().instabuild;
    }

    public void setPulledAnvilMode(boolean pulledAnvilMode) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_PULLED_ANVIL_MODE, pulledAnvilMode);
            return;
        }

        boolean nextMode = pulledAnvilMode && this.pullProcessingRecipeInputs;
        if (this.pulledAnvilMode == nextMode) {
            return;
        }

        if (nextMode) {
            clearCurrentPulledModeInputsToPlayer(this.getPlayer());
        } else {
            clearPulledAnvilInputsToPlayer(this.getPlayer());
        }
        this.pulledAnvilMode = nextMode;
        this.pulledAnvilItemName = nextMode ? getPulledAnvilInputName() : "";
        if (this.resonantHost != null) {
            this.resonantHost.setPulledAnvilMode(nextMode);
        }
        updatePulledResult();
    }

    public void setPulledAnvilItemName(String itemName) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_PULLED_ANVIL_ITEM_NAME, itemName);
            return;
        }

        if (!this.pullProcessingRecipeInputs || !this.pulledAnvilMode) {
            return;
        }

        this.pulledAnvilItemName = itemName == null ? "" : itemName;
        updatePulledResult();
    }

    public void pullEncodedInputsToGrid() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_PULL_ENCODED_INPUTS_TO_GRID);
            return;
        }

        if (!this.pullProcessingRecipeInputs || !this.canInteractWithGrid()) {
            return;
        }

        this.suppressPulledInputSync = true;
        try {
            clearPulledInputsToPlayer(this.getPlayer());
        } finally {
            this.suppressPulledInputSync = false;
        }

        switch (this.getMode()) {
            case CRAFTING -> pullEncodedItemsToSlots(EncodingMode.CRAFTING);
            case PROCESSING -> pullEncodedItemsToSlots(EncodingMode.PROCESSING);
            case SMITHING_TABLE -> pullEncodedItemsToSlots(EncodingMode.SMITHING_TABLE);
            case STONECUTTING -> pullEncodedItemsToSlots(EncodingMode.STONECUTTING);
        }
        updatePulledResult();
    }

    public void clearPulledInputsToNetwork() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR_PULLED_INPUTS_TO_NETWORK);
            return;
        }

        clearCurrentPulledModeInputsToNetwork(this.getPlayer());
    }

    public void clearPulledInputsToPlayer() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR_PULLED_INPUTS_TO_PLAYER);
            return;
        }

        clearCurrentPulledModeInputsToPlayer(this.getPlayer());
    }

    public void clearPulledAnvilInputsToNetwork() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_NETWORK);
            return;
        }

        clearInventoryToNetwork(this.pulledAnvilInputInv, this.getPlayer());
        this.pulledAnvilItemName = "";
        this.pulledAnvilCost = 0;
        updatePulledResult();
    }

    public void clearPulledAnvilInputsToPlayer() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR_PULLED_ANVIL_INPUTS_TO_PLAYER);
            return;
        }

        clearPulledAnvilInputsToPlayer(this.getPlayer());
    }

    public void pullProcessingTransferToGrid() {
        pullTransferToGrid(EncodingMode.PROCESSING);
    }

    public void prepareTransferMode(EncodingMode targetMode) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_PREPARE_TRANSFER_MODE, targetMode);
            return;
        }

        if (!this.pullProcessingRecipeInputs) {
            return;
        }

        setPulledAnvilMode(false);
        switchPulledTransferMode(targetMode);
    }

    public void pullTransferToGrid(EncodingMode targetMode) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_PULL_TRANSFER_TO_GRID, targetMode);
            return;
        }

        if (!this.pullProcessingRecipeInputs || !this.canInteractWithGrid()) {
            return;
        }

        setPulledAnvilMode(false);
        switchPulledTransferMode(targetMode);

        clearPulledModeInputsToNetwork(targetMode, this.getPlayer());
        pullEncodedItemsToSlots(targetMode);
        if (targetMode == EncodingMode.STONECUTTING) {
            refreshStonecuttingRecipesFromCurrentInput();
        }
        updatePulledResult();
    }

    private void switchPulledTransferMode(EncodingMode targetMode) {
        EncodingMode previousMode = this.getMode();
        if (previousMode == targetMode) {
            return;
        }

        clearPulledModeInputsToNetwork(previousMode, this.getPlayer());
        if (previousMode == EncodingMode.STONECUTTING) {
            this.getStonecuttingRecipes().clear();
            setStonecuttingRecipeId(null);
        }
        this.getEncodingLogic().setMode(targetMode);
        super.setMode(targetMode);
        updatePulledResult();
    }

    @Override
    public void setStonecuttingRecipeId(ResourceLocation id) {
        if (this.isClientSide()) {
            this.stonecuttingRecipeId = id;
            this.sendClientAction(ACTION_SET_PULLED_STONECUTTING_RECIPE_ID, id);
        } else {
            this.stonecuttingRecipeId = id;
            this.getEncodingLogic().setStonecuttingRecipeId(id);
            updatePulledResult();
        }
    }

    public void transferCraftingRecipeToRealGrid(RecipeHolder<?> recipe, List<List<GenericStack>> genericIngredients) {
        if (!this.pullProcessingRecipeInputs) {
            return;
        }

        if (this.isClientSide()) {
            return;
        }

        EncodingMode mode = getEncodingModeForRecipe(recipe);
        this.setMode(mode);
        Slot[] encodedSlots = getEncodedInputSlotsForMode(mode);
        AppEngSlot[] targetSlots = getPulledInputSlotsForMode(mode);
        List<GenericStack> selectedInputs = selectCraftingInputs(recipe, genericIngredients, encodedSlots.length);

        setEncodedAndPulledSlots(selectedInputs, encodedSlots, targetSlots);
        updatePulledResult();
    }

    private EncodingMode getEncodingModeForRecipe(RecipeHolder<?> recipe) {
        if (recipe != null && recipe.value().getType() == RecipeType.STONECUTTING) {
            this.setStonecuttingRecipeId(recipe.id());
            return EncodingMode.STONECUTTING;
        }
        if (recipe != null && recipe.value().getType() == RecipeType.SMITHING) {
            return EncodingMode.SMITHING_TABLE;
        }
        return EncodingMode.CRAFTING;
    }

    private List<GenericStack> selectCraftingInputs(RecipeHolder<?> recipe, List<List<GenericStack>> genericIngredients,
            int slotCount) {
        Map<appeng.api.stacks.AEKey, Integer> priorities = this.getClientRepo() != null
                ? EncodingHelper.getIngredientPriorities(this, ENTRY_COMPARATOR)
                : Map.of();
        var result = new ArrayList<GenericStack>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            List<GenericStack> candidates = i < genericIngredients.size() ? genericIngredients.get(i) : List.of();
            result.add(findBestIngredient(priorities, candidates));
        }
        return result;
    }

    private GenericStack findBestIngredient(Map<appeng.api.stacks.AEKey, Integer> priorities,
            List<GenericStack> candidates) {
        return candidates.stream()
                .filter(stack -> stack != null && stack.what() != null && stack.amount() > 0)
                .max(java.util.Comparator.comparingInt(stack -> priorities.getOrDefault(stack.what(), Integer.MIN_VALUE)))
                .orElse(null);
    }

    private void setEncodedAndPulledSlots(List<GenericStack> inputs, Slot[] encodedSlots, AppEngSlot[] targetSlots) {
        this.suppressPulledInputSync = true;
        try {
            clearPulledInputsToPlayer(this.getPlayer());
            for (int i = 0; i < encodedSlots.length && i < targetSlots.length; i++) {
                GenericStack input = i < inputs.size() ? inputs.get(i) : null;
                ItemStack encodedStack = input == null ? ItemStack.EMPTY : GenericStack.wrapInItemStack(input);
                encodedSlots[i].set(encodedStack);

                if (input == null || !(input.what() instanceof AEItemKey itemKey)) {
                    continue;
                }

                int maxAmount = Math.min(targetSlots[i].getMaxStackSize(itemKey.toStack()), itemKey.getMaxStackSize());
                int amount = (int) Math.min(input.amount(), maxAmount);
                if (amount <= 0) {
                    continue;
                }

                long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, itemKey, amount,
                        this.getActionSource(), Actionable.MODULATE);
                if (extracted > 0) {
                    targetSlots[i].set(itemKey.toStack((int) extracted));
                } else {
                    encodedSlots[i].set(ItemStack.EMPTY);
                }
            }
        } finally {
            this.suppressPulledInputSync = false;
        }
    }

    private void pullEncodedItemsToSlots(EncodingMode mode) {
        AppEngSlot[] targetSlots = getPulledInputSlotsForMode(mode);
        Slot[] encodedSlots = getEncodedInputSlotsForMode(mode);
        for (int i = 0; i < targetSlots.length; i++) {
            GenericStack encodedStack = getEncodedInputStack(mode, i);
            if (encodedStack == null || !(encodedStack.what() instanceof AEItemKey itemKey) || encodedStack.amount() <= 0) {
                continue;
            }

            AppEngSlot targetSlot = targetSlots[i];
            int maxAmount = Math.min(targetSlot.getMaxStackSize(itemKey.toStack()), itemKey.getMaxStackSize());
            int amount = (int) Math.min(encodedStack.amount(), maxAmount);
            if (amount <= 0) {
                continue;
            }

            long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, itemKey, amount,
                    this.getActionSource(), Actionable.MODULATE);
            if (extracted > 0) {
                targetSlot.set(itemKey.toStack((int) extracted));
            } else {
                encodedSlots[i].set(ItemStack.EMPTY);
            }
        }
    }

    private GenericStack getEncodedInputStack(int slot) {
        return this.getEncodingLogic().getEncodedInputInv().getStack(slot);
    }

    private GenericStack getEncodedInputStack(EncodingMode mode, int slot) {
        return this.getEncodingLogic().getEncodedInputInv().getStack(slot);
    }

    private Slot[] getEncodedInputSlotsForMode(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> this.getCraftingGridSlots();
            case PROCESSING -> this.getProcessingInputSlots();
            case SMITHING_TABLE -> new Slot[] {
                    this.getSmithingTableTemplateSlot(),
                    this.getSmithingTableBaseSlot(),
                    this.getSmithingTableAdditionSlot()
            };
            case STONECUTTING -> new Slot[] { this.getStonecuttingInputSlotReflective() };
        };
    }

    private AppEngSlot[] getPulledInputSlotsForMode(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> this.pulledCraftingInputSlots;
            case PROCESSING -> this.pulledProcessingInputSlots;
            case SMITHING_TABLE -> this.pulledSmithingInputSlots;
            case STONECUTTING -> new AppEngSlot[] { this.pulledStonecuttingInputSlot };
        };
    }

    @Override
    public void onSlotChange(Slot s) {
        if (this.isServerSide()) {
            if (syncEncodedInputFromPulledSlot(s) && !this.suppressPulledInputSync) {
                updatePulledResult();
            }
        }
        if (this.isServerSide() && this.isEncodedPatternSlot(s)) {
            exitPulledAnvilModeForLoadedPattern(s.getItem());
            this.loadResonatingPatternIntoEncodingArea(s.getItem());
        }
        super.onSlotChange(s);
    }

    @Override
    public void broadcastChanges() {
        refreshPulledStonecuttingRecipesIfInputChanged();
        super.broadcastChanges();
    }

    @Override
    public void clear() {
        if (this.isClientSide()) {
            super.clear();
            return;
        }

        super.clear();
        clearPulledInputsToPlayer(this.getPlayer());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && this.resonantHost == null) {
            clearPulledInputsToPlayer(player);
        }
    }

    public void setProcessingIngredientTransferMode(ProcessingIngredientTransferMode mode) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_PROCESSING_INGREDIENT_TRANSFER_MODE, mode);
        } else {
            this.processingIngredientTransferMode = mode;
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.getSlot(slotId);
            if (slot instanceof PulledAnvilResultSlot resultSlot && resultSlot.isActive()) {
                if (!this.isClientSide()) {
                    resultSlot.doClick(clickType == ClickType.QUICK_MOVE
                            ? InventoryAction.CRAFT_SHIFT
                            : InventoryAction.CRAFT_ITEM, player);
                }
                return;
            }
            if (slot instanceof PulledResultSlot resultSlot && resultSlot.isActive()) {
                if (!this.isClientSide()) {
                    resultSlot.doClick(clickType == ClickType.QUICK_MOVE
                            ? InventoryAction.CRAFT_SHIFT
                            : InventoryAction.CRAFT_ITEM, player);
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        if (idx >= 0 && idx < this.slots.size()) {
            Slot slot = this.getSlot(idx);
            if (slot instanceof PulledAnvilResultSlot resultSlot && resultSlot.isActive()) {
                if (!this.isClientSide()) {
                    resultSlot.doClick(InventoryAction.CRAFT_SHIFT, player);
                }
                return ItemStack.EMPTY;
            }
            if (slot instanceof PulledResultSlot resultSlot && resultSlot.isActive()) {
                if (!this.isClientSide()) {
                    resultSlot.doClick(InventoryAction.CRAFT_SHIFT, player);
                }
                return ItemStack.EMPTY;
            }
        }
        return super.quickMoveStack(player, idx);
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        if (slot >= 0 && slot < this.slots.size()) {
            Slot clickedSlot = this.getSlot(slot);
            if (clickedSlot instanceof PulledAnvilResultSlot resultSlot && resultSlot.isActive()) {
                switch (action) {
                    case CRAFT_SHIFT, CRAFT_ALL, CRAFT_ITEM, CRAFT_STACK -> {
                        resultSlot.doClick(action, player);
                        return;
                    }
                    default -> {
                    }
                }
            }
            if (clickedSlot instanceof PulledResultSlot resultSlot && resultSlot.isActive()) {
                switch (action) {
                    case CRAFT_SHIFT, CRAFT_ALL, CRAFT_ITEM, CRAFT_STACK -> {
                        resultSlot.doClick(action, player);
                        return;
                    }
                    default -> {
                    }
                }
            }
        }
        super.doAction(player, action, slot, id);
    }

    public void cycleProcessingIngredientTransferMode() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CYCLE_PROCESSING_INGREDIENT_TRANSFER_MODE);
        } else {
            this.processingIngredientTransferMode = this.processingIngredientTransferMode.next();
        }
    }

    private void convertEncodedOutputToResonatingPattern() {
        Slot encodedPatternSlot = this.getSlots(SlotSemantics.ENCODED_PATTERN).getFirst();
        ItemStack encodedPattern = encodedPatternSlot.getItem();
        ItemStack resonatingPattern = ResonatingPatternDetails.encode(encodedPattern);

        if (!resonatingPattern.isEmpty()) {
            encodedPatternSlot.set(resonatingPattern);
        }
    }

    private boolean isEncodedPatternSlot(Slot slot) {
        return this.getSlots(SlotSemantics.ENCODED_PATTERN).contains(slot);
    }

    private void exitPulledAnvilModeForLoadedPattern(ItemStack stack) {
        if (this.pullProcessingRecipeInputs && this.pulledAnvilMode && PatternDetailsHelper.isEncodedPattern(stack)) {
            setPulledAnvilMode(false);
        }
    }

    private void loadResonatingPatternIntoEncodingArea(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        IPatternDetails details = PatternDetailsHelper.decodePattern(stack, this.getPlayerInventory().player.level());
        if (!(details instanceof ResonatingPatternDetails resonatingPattern)) {
            return;
        }

        PatternEncodingLogic encodingLogic = this.getEncodingLogic();
        encodingLogic.setMode(EncodingMode.PROCESSING);
        fillInventoryFromSparseStacks(encodingLogic.getEncodedInputInv(), resonatingPattern.getSparseInputs());
        fillInventoryFromSparseStacks(encodingLogic.getEncodedOutputInv(), resonatingPattern.getSparseOutputs());

        this.mode = EncodingMode.PROCESSING;
        this.encodeResonatingPattern = true;
    }

    private PatternEncodingLogic getEncodingLogic() {
        try {
            return (PatternEncodingLogic) ENCODING_LOGIC_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access pattern encoding logic", e);
        }
    }

    private boolean syncEncodedInputFromPulledSlot(Slot slot) {
        if (this.suppressPulledInputSync) {
            return false;
        }

        if (syncEncodedSlot(slot, this.pulledCraftingInputSlots, this.getCraftingGridSlots())) {
            return true;
        }
        if (syncEncodedSlot(slot, this.pulledProcessingInputSlots, this.getProcessingInputSlots())) {
            return true;
        }
        if (syncEncodedSlot(slot, this.pulledSmithingInputSlots, new Slot[] {
                this.getSmithingTableTemplateSlot(),
                this.getSmithingTableBaseSlot(),
                this.getSmithingTableAdditionSlot()
        })) {
            return true;
        }
        return syncEncodedSlot(slot, new AppEngSlot[] { this.pulledStonecuttingInputSlot },
                new Slot[] { this.getStonecuttingInputSlotReflective() });
    }

    private void syncCurrentPulledInputsToEncodedSlots() {
        if (!this.pullProcessingRecipeInputs || this.pulledAnvilMode) {
            return;
        }

        switch (this.getMode()) {
            case CRAFTING -> syncPulledSlotsToEncodedSlots(this.pulledCraftingInputSlots, this.getCraftingGridSlots());
            case PROCESSING -> syncPulledSlotsToEncodedSlots(this.pulledProcessingInputSlots,
                    this.getProcessingInputSlots());
            case SMITHING_TABLE -> syncPulledSlotsToEncodedSlots(this.pulledSmithingInputSlots, new Slot[] {
                    this.getSmithingTableTemplateSlot(),
                    this.getSmithingTableBaseSlot(),
                    this.getSmithingTableAdditionSlot()
            });
            case STONECUTTING -> {
                syncPulledSlotsToEncodedSlots(new AppEngSlot[] { this.pulledStonecuttingInputSlot },
                        new Slot[] { this.getStonecuttingInputSlotReflective() });
                refreshStonecuttingRecipesFromCurrentInput();
            }
        }
    }

    private void syncPulledSlotsToEncodedSlots(AppEngSlot[] realSlots, Slot[] encodedSlots) {
        for (int i = 0; i < realSlots.length && i < encodedSlots.length; i++) {
            ItemStack stack = realSlots[i].getItem();
            encodedSlots[i].set(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private Slot getStonecuttingInputSlotReflective() {
        try {
            return (Slot) STONECUTTING_INPUT_SLOT_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access PatternEncodingTermMenu#stonecuttingInputSlot", e);
        }
    }

    private boolean syncEncodedSlot(Slot changedSlot, AppEngSlot[] realSlots, Slot[] encodedSlots) {
        for (int i = 0; i < realSlots.length && i < encodedSlots.length; i++) {
            if (realSlots[i] == changedSlot) {
                ItemStack stack = realSlots[i].getItem();
                encodedSlots[i].set(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                if (changedSlot == this.pulledStonecuttingInputSlot) {
                    refreshStonecuttingRecipesFromCurrentInput();
                }
                return true;
            }
        }
        return false;
    }

    private void updatePulledResult() {
        this.pulledCraftingResultInv.setItem(0,
                !this.pulledAnvilMode && this.getMode() == EncodingMode.CRAFTING ? getPulledCraftingResult()
                        : ItemStack.EMPTY);
        this.pulledSmithingResultInv.setItem(0,
                !this.pulledAnvilMode && this.getMode() == EncodingMode.SMITHING_TABLE ? getPulledSmithingResult()
                        : ItemStack.EMPTY);
        this.pulledStonecuttingResultInv.setItem(0,
                !this.pulledAnvilMode && this.getMode() == EncodingMode.STONECUTTING ? getPulledStonecuttingResult()
                        : ItemStack.EMPTY);
        this.pulledAnvilResultInv.setItem(0,
                this.pulledAnvilMode ? getPulledAnvilResult() : ItemStack.EMPTY);
    }

    private ItemStack getPulledCraftingResult() {
        CraftingInput input = createPulledCraftingInput();
        Level level = this.getPlayer().level();
        RecipeHolder<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level)
                .orElse(null);
        return recipe == null ? ItemStack.EMPTY : recipe.value().assemble(input, level.registryAccess());
    }

    private CraftingInput createPulledCraftingInput() {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < this.pulledCraftingInputSlots.length && i < items.size(); i++) {
            items.set(i, this.pulledCraftingInputSlots[i].getItem());
        }
        return CraftingInput.of(3, 3, items);
    }

    private ItemStack getPulledSmithingResult() {
        SmithingRecipeInput input = createPulledSmithingInput();
        Level level = this.getPlayer().level();
        RecipeHolder<SmithingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMITHING, input, level)
                .orElse(null);
        return recipe == null ? ItemStack.EMPTY : recipe.value().assemble(input, level.registryAccess());
    }

    private SmithingRecipeInput createPulledSmithingInput() {
        return new SmithingRecipeInput(
                this.pulledSmithingInputSlots[0].getItem(),
                this.pulledSmithingInputSlots[1].getItem(),
                this.pulledSmithingInputSlots[2].getItem());
    }

    private ItemStack getPulledStonecuttingResult() {
        ItemStack inputStack = this.pulledStonecuttingInputSlot.getItem();
        if (inputStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        SingleRecipeInput input = new SingleRecipeInput(inputStack);
        Level level = this.getPlayer().level();
        RecipeHolder<StonecutterRecipe> recipe = this.getStonecuttingRecipeId() == null
                ? level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, input, level).orElse(null)
                : level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, input, level,
                        this.getStonecuttingRecipeId()).orElse(null);
        return recipe == null ? ItemStack.EMPTY : recipe.value().assemble(input, level.registryAccess());
    }

    private ItemStack getPulledAnvilResult() {
        PulledAnvilResult calculation = calculatePulledAnvilResult();
        this.pulledAnvilCost = calculation.cost();
        return calculation.result();
    }

    private PulledAnvilResult calculatePulledAnvilResult() {
        ItemStack left = this.pulledAnvilInputSlots[0].getItem();
        ItemStack right = this.pulledAnvilInputSlots[1].getItem();
        if (left.isEmpty()) {
            return PulledAnvilResult.EMPTY;
        }

        PulledAnvilDelegateMenu delegate = createPulledAnvilDelegate(left, right);
        delegate.setItemName(this.pulledAnvilItemName);
        delegate.createResult();
        ItemStack result = delegate.getSlot(AnvilMenu.RESULT_SLOT).getItem().copy();
        return new PulledAnvilResult(result, delegate.getCost(), delegate.repairItemCountCost);
    }

    private PulledAnvilDelegateMenu createPulledAnvilDelegate(ItemStack left, ItemStack right) {
        PulledAnvilDelegateMenu delegate = new PulledAnvilDelegateMenu(-1, this.getPlayerInventory());
        delegate.getSlot(AnvilMenu.INPUT_SLOT).set(left.copy());
        delegate.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(right.copy());
        return delegate;
    }

    private String getPulledAnvilInputName() {
        ItemStack input = this.pulledAnvilInputSlots[0].getItem();
        return input.isEmpty() ? "" : input.getHoverName().getString();
    }

    private void refreshStonecuttingRecipesFromCurrentInput() {
        this.getStonecuttingRecipes().clear();

        ItemStack inputStack = this.pullProcessingRecipeInputs
                ? this.pulledStonecuttingInputSlot.getItem()
                : this.getStonecuttingInputSlotReflective().getItem();
        this.lastPulledStonecuttingInputForRecipes = inputStack.copy();
        if (!inputStack.isEmpty()) {
            SingleRecipeInput input = new SingleRecipeInput(inputStack);
            Level level = this.getPlayer().level();
            this.getStonecuttingRecipes().addAll(level.getRecipeManager()
                    .getRecipesFor(RecipeType.STONECUTTING, input, level));
        }

        if (this.stonecuttingRecipeId != null
                && this.getStonecuttingRecipes().stream().noneMatch(recipe -> recipe.id().equals(this.stonecuttingRecipeId))) {
            setStonecuttingRecipeId(null);
        }
        updatePulledResult();
    }

    private void refreshPulledStonecuttingRecipesIfInputChanged() {
        if (!this.isServerSide() || !this.pullProcessingRecipeInputs || this.pulledAnvilMode
                || this.getMode() != EncodingMode.STONECUTTING) {
            return;
        }

        ItemStack inputStack = this.pulledStonecuttingInputSlot.getItem();
        if (!ItemStack.isSameItemSameComponents(inputStack, this.lastPulledStonecuttingInputForRecipes)
                || inputStack.getCount() != this.lastPulledStonecuttingInputForRecipes.getCount()) {
            syncEncodedInputFromPulledSlot(this.pulledStonecuttingInputSlot);
        }
    }

    private ItemStack craftPulledResult(EncodingMode mode, Player player) {
        if (this.isClientSide() || mode != this.getMode()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = switch (mode) {
            case CRAFTING -> craftPulledCraftingResult(player);
            case SMITHING_TABLE -> craftPulledSmithingResult(player);
            case STONECUTTING -> craftPulledStonecuttingResult(player);
            case PROCESSING -> ItemStack.EMPTY;
        };
        if (!result.isEmpty()) {
            playPulledCraftingSound(mode, player);
        }
        updatePulledResult();
        return result;
    }

    private ItemStack craftPulledAnvilResult(Player player) {
        if (this.isClientSide() || !this.pulledAnvilMode) {
            return ItemStack.EMPTY;
        }

        PulledAnvilDelegateMenu delegate = createPulledAnvilDelegate(
                this.pulledAnvilInputSlots[0].getItem(),
                this.pulledAnvilInputSlots[1].getItem());
        delegate.setItemName(this.pulledAnvilItemName);
        delegate.createResult();
        ItemStack result = delegate.getSlot(AnvilMenu.RESULT_SLOT).getItem().copy();
        if (result.isEmpty()) {
            updatePulledResult();
            return ItemStack.EMPTY;
        }
        if (!canPulledAnvilTakeResult(delegate, player, result)) {
            updatePulledResult();
            return ItemStack.EMPTY;
        }

        ItemStack[] desiredStacks = {
                this.pulledAnvilInputSlots[0].getItem().copy(),
                this.pulledAnvilInputSlots[1].getItem().copy()
        };
        int[] desiredCounts = Arrays.stream(desiredStacks)
                .mapToInt(ItemStack::getCount)
                .toArray();

        takePulledAnvilResult(delegate, player, result);

        this.suppressPulledInputSync = true;
        try {
            this.pulledAnvilInputSlots[0].set(delegate.getSlot(AnvilMenu.INPUT_SLOT).getItem().copy());
            this.pulledAnvilInputSlots[1].set(delegate.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem().copy());
            replenishPulledAnvilInputs(desiredStacks, desiredCounts);
        } finally {
            this.suppressPulledInputSync = false;
        }

        this.pulledAnvilItemName = "";
        playPulledAnvilSound(player);
        updatePulledResult();
        return result;
    }

    private static boolean canPulledAnvilTakeResult(PulledAnvilDelegateMenu delegate, Player player, ItemStack result) {
        return delegate.canTakeResult(player, !result.isEmpty());
    }

    private static void takePulledAnvilResult(PulledAnvilDelegateMenu delegate, Player player, ItemStack result) {
        delegate.takeResult(player, result.copy());
    }

    private void replenishPulledAnvilInputs(ItemStack[] desiredStacks, int[] desiredCounts) {
        if (!this.canInteractWithGrid()) {
            return;
        }

        for (int i = 0; i < this.pulledAnvilInputSlots.length && i < desiredStacks.length && i < desiredCounts.length; i++) {
            ItemStack desiredStack = desiredStacks[i];
            if (desiredStack.isEmpty()) {
                continue;
            }

            AppEngSlot targetSlot = this.pulledAnvilInputSlots[i];
            ItemStack currentStack = targetSlot.getItem();
            if (!currentStack.isEmpty() && !ItemStack.isSameItemSameComponents(currentStack, desiredStack)) {
                continue;
            }

            AEItemKey itemKey = AEItemKey.of(desiredStack);
            if (itemKey == null) {
                continue;
            }

            int desiredCount = Math.min(desiredCounts[i],
                    Math.min(targetSlot.getMaxStackSize(desiredStack), itemKey.getMaxStackSize()));
            int missing = desiredCount - currentStack.getCount();
            if (missing <= 0) {
                continue;
            }

            long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, itemKey, missing,
                    this.getActionSource(), Actionable.MODULATE);
            if (extracted <= 0) {
                continue;
            }

            if (currentStack.isEmpty()) {
                targetSlot.set(itemKey.toStack((int) extracted));
            } else {
                currentStack.grow((int) extracted);
                targetSlot.set(currentStack);
            }
        }
    }

    private ItemStack craftPulledCraftingResult(Player player) {
        CraftingInput input = createPulledCraftingInput();
        Level level = player.level();
        RecipeHolder<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level)
                .orElse(null);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = recipe.value().assemble(input, level.registryAccess());
        NonNullList<ItemStack> remaining = recipe.value().getRemainingItems(input);
        consumePulledInputs(EncodingMode.CRAFTING, this.pulledCraftingInputSlots, this.getCraftingGridSlots(),
                remaining, player);
        return result;
    }

    private ItemStack craftPulledSmithingResult(Player player) {
        SmithingRecipeInput input = createPulledSmithingInput();
        Level level = player.level();
        RecipeHolder<SmithingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMITHING, input, level)
                .orElse(null);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = recipe.value().assemble(input, level.registryAccess());
        consumePulledInputs(EncodingMode.SMITHING_TABLE, this.pulledSmithingInputSlots, new Slot[] {
                this.getSmithingTableTemplateSlot(),
                this.getSmithingTableBaseSlot(),
                this.getSmithingTableAdditionSlot()
        }, recipe.value().getRemainingItems(input), player);
        return result;
    }

    private ItemStack craftPulledStonecuttingResult(Player player) {
        ItemStack inputStack = this.pulledStonecuttingInputSlot.getItem();
        if (inputStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        SingleRecipeInput input = new SingleRecipeInput(inputStack);
        Level level = player.level();
        RecipeHolder<StonecutterRecipe> recipe = this.getStonecuttingRecipeId() == null
                ? level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, input, level).orElse(null)
                : level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, input, level,
                        this.getStonecuttingRecipeId()).orElse(null);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = recipe.value().assemble(input, level.registryAccess());
        consumePulledInputs(EncodingMode.STONECUTTING, new AppEngSlot[] { this.pulledStonecuttingInputSlot },
                new Slot[] { this.getStonecuttingInputSlotReflective() }, recipe.value().getRemainingItems(input),
                player);
        return result;
    }

    private void playPulledCraftingSound(EncodingMode mode, Player player) {
        if (mode == EncodingMode.SMITHING_TABLE) {
            long gameTime = player.level().getGameTime();
            if (this.lastPulledSmithingSoundTime != gameTime) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                this.lastPulledSmithingSoundTime = gameTime;
            }
        } else if (mode == EncodingMode.STONECUTTING) {
            long gameTime = player.level().getGameTime();
            if (this.lastPulledStonecuttingSoundTime != gameTime) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                this.lastPulledStonecuttingSoundTime = gameTime;
            }
        }
    }

    private void playPulledAnvilSound(Player player) {
        long gameTime = player.level().getGameTime();
        if (this.lastPulledAnvilSoundTime != gameTime) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            this.lastPulledAnvilSoundTime = gameTime;
        }
    }

    private void consumePulledInputs(EncodingMode mode, AppEngSlot[] slots, Slot[] encodedSlots,
            NonNullList<ItemStack> remaining, Player player) {
        int[] desiredCounts = new int[slots.length];
        for (int i = 0; i < slots.length; i++) {
            desiredCounts[i] = slots[i].getItem().getCount();
        }

        this.suppressPulledInputSync = true;
        try {
            for (int i = 0; i < slots.length; i++) {
                ItemStack input = slots[i].getItem();
                if (!input.isEmpty()) {
                    input.shrink(1);
                    slots[i].set(input.isEmpty() ? ItemStack.EMPTY : input);
                }

                ItemStack remainder = i < remaining.size() ? remaining.get(i) : ItemStack.EMPTY;
                if (!remainder.isEmpty()) {
                    ItemStack overflow = slots[i].getItem().isEmpty() ? slots[i].safeInsert(remainder.copy())
                            : remainder.copy();
                    if (!overflow.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(overflow);
                    }
                }
            }
            replenishPulledInputs(mode, slots, desiredCounts);
        } finally {
            this.suppressPulledInputSync = false;
        }
        Arrays.stream(slots).forEach(this::syncEncodedInputFromPulledSlot);
    }

    private void replenishPulledInputs(EncodingMode mode, AppEngSlot[] targetSlots, int[] desiredCounts) {
        if (!this.canInteractWithGrid()) {
            return;
        }

        for (int i = 0; i < targetSlots.length && i < desiredCounts.length; i++) {
            AppEngSlot targetSlot = targetSlots[i];
            GenericStack encodedStack = getEncodedInputStack(mode, i);
            if (encodedStack == null || !(encodedStack.what() instanceof AEItemKey itemKey)) {
                continue;
            }

            ItemStack currentStack = targetSlot.getItem();
            if (!currentStack.isEmpty() && !itemKey.matches(currentStack)) {
                continue;
            }

            int desiredCount = Math.min(desiredCounts[i],
                    Math.min(targetSlot.getMaxStackSize(itemKey.toStack()), itemKey.getMaxStackSize()));
            int missing = desiredCount - currentStack.getCount();
            if (missing <= 0) {
                continue;
            }

            long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, itemKey, missing,
                    this.getActionSource(), Actionable.MODULATE);
            if (extracted <= 0) {
                continue;
            }

            if (currentStack.isEmpty()) {
                targetSlot.set(itemKey.toStack((int) extracted));
            } else {
                currentStack.grow((int) extracted);
                targetSlot.set(currentStack);
            }
        }
    }

    private void clearPulledInputsToPlayer(Player player) {
        clearInventoryToPlayer(this.pulledCraftingInputInv, player);
        clearInventoryToPlayer(this.pulledProcessingInputInv, player);
        clearInventoryToPlayer(this.pulledSmithingInputInv, player);
        clearInventoryToPlayer(this.pulledStonecuttingInputInv, player);
        clearInventoryToPlayer(this.pulledAnvilInputInv, player);
        updatePulledResult();
    }

    private void clearPulledInputsToNetwork(Player player) {
        clearInventoryToNetwork(this.pulledCraftingInputInv, player);
        clearInventoryToNetwork(this.pulledProcessingInputInv, player);
        clearInventoryToNetwork(this.pulledSmithingInputInv, player);
        clearInventoryToNetwork(this.pulledStonecuttingInputInv, player);
        clearInventoryToNetwork(this.pulledAnvilInputInv, player);
        updatePulledResult();
    }

    private void clearPulledAnvilInputsToPlayer(Player player) {
        clearInventoryToPlayer(this.pulledAnvilInputInv, player);
        this.pulledAnvilItemName = "";
        this.pulledAnvilCost = 0;
        updatePulledResult();
    }

    private void clearCurrentPulledModeInputsToPlayer(Player player) {
        clearEncodedInputSlotsForMode(this.getMode());
        switch (this.getMode()) {
            case CRAFTING -> clearInventoryToPlayer(this.pulledCraftingInputInv, player);
            case PROCESSING -> clearInventoryToPlayer(this.pulledProcessingInputInv, player);
            case SMITHING_TABLE -> clearInventoryToPlayer(this.pulledSmithingInputInv, player);
            case STONECUTTING -> clearInventoryToPlayer(this.pulledStonecuttingInputInv, player);
        }
        updatePulledResult();
    }

    private void clearCurrentPulledModeInputsToNetwork(Player player) {
        clearEncodedInputSlotsForMode(this.getMode());
        clearPulledModeInputsToNetwork(this.getMode(), player);
        updatePulledResult();
    }

    private void clearPulledModeInputsToNetwork(EncodingMode mode, Player player) {
        switch (mode) {
            case CRAFTING -> clearInventoryToNetwork(this.pulledCraftingInputInv, player);
            case PROCESSING -> clearInventoryToNetwork(this.pulledProcessingInputInv, player);
            case SMITHING_TABLE -> clearInventoryToNetwork(this.pulledSmithingInputInv, player);
            case STONECUTTING -> clearInventoryToNetwork(this.pulledStonecuttingInputInv, player);
        }
        updatePulledResult();
    }

    private void clearEncodedInputSlotsForMode(EncodingMode mode) {
        for (Slot slot : getEncodedInputSlotsForMode(mode)) {
            slot.set(ItemStack.EMPTY);
        }
        if (mode == EncodingMode.STONECUTTING) {
            this.getStonecuttingRecipes().clear();
            setStonecuttingRecipeId(null);
        }
    }

    private void clearInventoryToPlayer(AppEngInternalInventory inv, Player player) {
        this.suppressPulledInputSync = true;
        try {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.extractItem(i, inv.getStackInSlot(i).getCount(), false);
                if (!stack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack);
                }
            }
        } finally {
            this.suppressPulledInputSync = false;
        }
    }

    private void clearInventoryToNetwork(AppEngInternalInventory inv, Player player) {
        this.suppressPulledInputSync = true;
        try {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.extractItem(i, inv.getStackInSlot(i).getCount(), false);
                if (stack.isEmpty()) {
                    continue;
                }

                ItemStack remainder = insertStackIntoNetwork(stack);
                if (!remainder.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(remainder);
                }
            }
        } finally {
            this.suppressPulledInputSync = false;
        }
    }

    private ItemStack insertStackIntoNetwork(ItemStack stack) {
        if (!this.canInteractWithGrid()) {
            return stack;
        }

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) {
            return stack;
        }

        long inserted = StorageHelper.poweredInsert(this.energySource, this.storage, key, stack.getCount(),
                this.getActionSource());
        ItemStack remainder = stack.copy();
        remainder.shrink((int) inserted);
        return remainder;
    }

    private class PulledInputSlot extends AppEngSlot {
        private final EncodingMode mode;

        private PulledInputSlot(AppEngInternalInventory inv, int invSlot, EncodingMode mode) {
            super(inv, invSlot);
            this.mode = mode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return pullProcessingRecipeInputs && getMode() == this.mode && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return pullProcessingRecipeInputs && getMode() == this.mode && super.mayPickup(player);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            afterChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack removed = super.remove(amount);
            afterChanged();
            return removed;
        }

        @Override
        public void clearStack() {
            super.clearStack();
            afterChanged();
        }

        private void afterChanged() {
            if (!suppressPulledInputSync && isServerSide()) {
                syncEncodedInputFromPulledSlot(this);
                updatePulledResult();
            }
        }
    }

    private class PulledSmithingInputSlot extends PulledInputSlot {
        private final int smithingSlot;

        private PulledSmithingInputSlot(AppEngInternalInventory inv, int invSlot) {
            super(inv, invSlot, EncodingMode.SMITHING_TABLE);
            this.smithingSlot = invSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && isValidPulledSmithingIngredient(this.smithingSlot, stack);
        }
    }

    private boolean isValidPulledSmithingIngredient(int smithingSlot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        Level level = this.getPlayer().level();
        return level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING).stream()
                .map(RecipeHolder::value)
                .anyMatch(recipe -> switch (smithingSlot) {
                    case 0 -> recipe.isTemplateIngredient(stack);
                    case 1 -> recipe.isBaseIngredient(stack);
                    case 2 -> recipe.isAdditionIngredient(stack);
                    default -> false;
                });
    }

    private class PulledAnvilInputSlot extends AppEngSlot {
        private PulledAnvilInputSlot(AppEngInternalInventory inv, int invSlot) {
            super(inv, invSlot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return pullProcessingRecipeInputs && pulledAnvilMode && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return pullProcessingRecipeInputs && pulledAnvilMode && super.mayPickup(player);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            afterChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack removed = super.remove(amount);
            afterChanged();
            return removed;
        }

        @Override
        public void clearStack() {
            super.clearStack();
            afterChanged();
        }

        private void afterChanged() {
            if (!suppressPulledInputSync && isServerSide()) {
                if (this.getSlotIndex() == 0) {
                    pulledAnvilItemName = getPulledAnvilInputName();
                }
                updatePulledResult();
            }
        }
    }

    private class PulledResultSlot extends Slot {
        private final EncodingMode mode;

        private PulledResultSlot(Container container, int slot, EncodingMode mode) {
            super(container, slot, 0, 0);
            this.mode = mode;
        }

        @Override
        public boolean isActive() {
            return pullProcessingRecipeInputs && getMode() == this.mode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.isActive() && !this.getItem().isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            craftPulledResult(this.mode, player);
            super.onTake(player, stack);
        }

        private void doClick(InventoryAction action, Player player) {
            if (this.getItem().isEmpty() || isClientSide()) {
                return;
            }

            if (action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL) {
                craftToPlayerInventory(action, player);
            } else if (action == InventoryAction.CRAFT_STACK) {
                craftToCarriedStack(player, true);
            } else {
                craftToCarriedStack(player, false);
            }
        }

        private void craftToPlayerInventory(InventoryAction action, Player player) {
            ItemStack itemAtStart = this.getItem().copy();
            if (itemAtStart.isEmpty()) {
                return;
            }

            int howManyPerCraft = itemAtStart.getCount();
            int maxTimesToCraft = action == InventoryAction.CRAFT_SHIFT
                    ? Math.max(1, itemAtStart.getMaxStackSize() / howManyPerCraft)
                    : Math.max(1, itemAtStart.getMaxStackSize() / howManyPerCraft * 36);
            PlayerInternalInventory target = new PlayerInternalInventory(player.getInventory());

            for (int i = 0; i < maxTimesToCraft; i++) {
                ItemStack currentResult = this.getItem().copy();
                if (currentResult.isEmpty() || !ItemStack.isSameItemSameComponents(itemAtStart, currentResult)) {
                    return;
                }
                if (!target.simulateAdd(currentResult).isEmpty()) {
                    return;
                }

                ItemStack crafted = craftPulledResult(this.mode, player);
                if (crafted.isEmpty()) {
                    return;
                }

                ItemStack overflow = target.addItems(crafted);
                if (!overflow.isEmpty()) {
                    player.drop(overflow, false);
                    return;
                }
            }
        }

        private void craftToCarriedStack(Player player, boolean fillStack) {
            int maxTimesToCraft = fillStack ? getMaxCarriedCrafts() : 1;
            for (int i = 0; i < maxTimesToCraft; i++) {
                ItemStack currentResult = this.getItem().copy();
                if (currentResult.isEmpty() || !canAddToCarried(currentResult)) {
                    return;
                }

                ItemStack crafted = craftPulledResult(this.mode, player);
                if (crafted.isEmpty()) {
                    return;
                }
                addToCarried(crafted);
            }
        }

        private int getMaxCarriedCrafts() {
            ItemStack result = this.getItem();
            if (result.isEmpty()) {
                return 0;
            }

            ItemStack carried = getCarried();
            int remainingSpace = carried.isEmpty()
                    ? result.getMaxStackSize()
                    : carried.getMaxStackSize() - carried.getCount();
            return Math.max(0, remainingSpace / result.getCount());
        }

        private boolean canAddToCarried(ItemStack stack) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                return true;
            }
            return ItemStack.isSameItemSameComponents(carried, stack)
                    && carried.getCount() + stack.getCount() <= carried.getMaxStackSize();
        }

        private void addToCarried(ItemStack stack) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                setCarried(stack.copy());
            } else {
                carried.grow(stack.getCount());
                setCarried(carried);
            }
        }
    }

    private class PulledAnvilResultSlot extends Slot {
        private PulledAnvilResultSlot(Container container, int slot) {
            super(container, slot, 0, 0);
        }

        @Override
        public boolean isActive() {
            return pullProcessingRecipeInputs && pulledAnvilMode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.isActive() && !this.getItem().isEmpty()
                    && (player.getAbilities().instabuild || player.experienceLevel >= pulledAnvilCost);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            craftPulledAnvilResult(player);
            super.onTake(player, stack);
        }

        private void doClick(InventoryAction action, Player player) {
            if (this.getItem().isEmpty() || isClientSide() || !mayPickup(player)) {
                return;
            }

            if (action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL) {
                craftToPlayerInventory(player);
            } else {
                craftToCarriedStack(player);
            }
        }

        private void craftToPlayerInventory(Player player) {
            ItemStack currentResult = this.getItem().copy();
            if (currentResult.isEmpty()) {
                return;
            }

            PlayerInternalInventory target = new PlayerInternalInventory(player.getInventory());
            if (!target.simulateAdd(currentResult).isEmpty()) {
                return;
            }

            ItemStack crafted = craftPulledAnvilResult(player);
            if (crafted.isEmpty()) {
                return;
            }

            ItemStack overflow = target.addItems(crafted);
            if (!overflow.isEmpty()) {
                player.drop(overflow, false);
            }
        }

        private void craftToCarriedStack(Player player) {
            ItemStack currentResult = this.getItem().copy();
            if (currentResult.isEmpty() || !canAddToCarried(currentResult)) {
                return;
            }

            ItemStack crafted = craftPulledAnvilResult(player);
            if (!crafted.isEmpty()) {
                addToCarried(crafted);
            }
        }

        private boolean canAddToCarried(ItemStack stack) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                return true;
            }
            return ItemStack.isSameItemSameComponents(carried, stack)
                    && carried.getCount() + stack.getCount() <= carried.getMaxStackSize();
        }

        private void addToCarried(ItemStack stack) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                setCarried(stack.copy());
            } else {
                carried.grow(stack.getCount());
                setCarried(carried);
            }
        }
    }

    private static class PulledAnvilDelegateMenu extends AnvilMenu {
        private PulledAnvilDelegateMenu(int id, Inventory inventory) {
            super(id, inventory, ContainerLevelAccess.NULL);
        }

        private boolean canTakeResult(Player player, boolean hasResult) {
            return super.mayPickup(player, hasResult);
        }

        private void takeResult(Player player, ItemStack result) {
            super.onTake(player, result);
        }
    }

    private static void fillInventoryFromSparseStacks(ConfigInventory inv, List<GenericStack> stacks) {
        inv.beginBatch();
        try {
            for (int i = 0; i < inv.size(); i++) {
                inv.setStack(i, i < stacks.size() ? stacks.get(i) : null);
            }
        } finally {
            inv.endBatch();
        }
    }

    private static Field getEncodingLogicField() {
        return getSlotField("encodingLogic");
    }

    private static Field getSlotField(String name) {
        try {
            Field field = PatternEncodingTermMenu.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to find PatternEncodingTermMenu#" + name, e);
        }
    }

    private static Boolean isUndamaged(GridInventoryEntry entry) {
        return !(entry.getWhat() instanceof AEItemKey itemKey) || !itemKey.isDamaged();
    }

    public enum ProcessingIngredientTransferMode {
        MERGE,
        PARTIAL_SPLIT,
        FULL_SPLIT;

        public ProcessingIngredientTransferMode next() {
            ProcessingIngredientTransferMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    private record PulledAnvilResult(ItemStack result, int cost, int repairItemCountCost) {
        private static final PulledAnvilResult EMPTY = new PulledAnvilResult(ItemStack.EMPTY, 0, 0);
    }
}
