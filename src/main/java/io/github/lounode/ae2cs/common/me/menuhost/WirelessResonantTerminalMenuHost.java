package io.github.lounode.ae2cs.common.me.menuhost;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.WirelessResonantTerminalItem;
import io.github.lounode.ae2cs.common.me.part.IResonantTemplateCodingTerminalHost;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;

import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

public class WirelessResonantTerminalMenuHost
                                              extends WirelessTerminalMenuHost<WirelessResonantTerminalItem>
                                              implements IPatternTerminalLogicHost, IPatternTerminalMenuHost, IResonantTemplateCodingTerminalHost,
                                              InternalInventoryHost {

    private static final String TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID = "PullRecipeInputsToRealGrid";
    private static final String TAG_PULLED_ANVIL_MODE = "PulledAnvilMode";
    private static final String TAG_ENCODE_RESONATING_PATTERN = "EncodeResonatingPattern";
    private static final String TAG_PROCESSING_INGREDIENT_TRANSFER_MODE = "ProcessingIngredientTransferMode";
    private static final String TAG_PULLED_CRAFTING_INPUTS = "PulledCraftingInputs";
    private static final String TAG_PULLED_PROCESSING_INPUTS = "PulledProcessingInputs";
    private static final String TAG_PULLED_SMITHING_INPUTS = "PulledSmithingInputs";
    private static final String TAG_PULLED_STONECUTTING_INPUTS = "PulledStonecuttingInputs";
    private static final String TAG_PULLED_ANVIL_INPUTS = "PulledAnvilInputs";

    private final PatternEncodingLogic logic = new PatternEncodingLogic(this);
    private final AppEngInternalInventory pulledCraftingInputInv = new AppEngInternalInventory(this, 9);
    private final AppEngInternalInventory pulledProcessingInputInv = new AppEngInternalInventory(this, AEProcessingPattern.MAX_INPUT_SLOTS);
    private final AppEngInternalInventory pulledSmithingInputInv = new AppEngInternalInventory(this, 3);
    private final AppEngInternalInventory pulledStonecuttingInputInv = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory pulledAnvilInputInv = new AppEngInternalInventory(this, 2);
    private boolean pullRecipeInputsToRealGrid;
    private boolean pulledAnvilMode;
    private boolean encodeResonatingPattern;
    private ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode processingIngredientTransferMode = ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE;
    private boolean loading = true;

    public WirelessResonantTerminalMenuHost(WirelessResonantTerminalItem item, Player player,
                                            ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        readFromItem();
        this.loading = false;
    }

    @Override
    public PatternEncodingLogic getLogic() {
        return this.logic;
    }

    @Override
    public Level getLevel() {
        return getPlayer().level();
    }

    @Override
    public void markForSave() {
        if (this.loading || getPlayer().level().isClientSide()) {
            return;
        }

        ItemStack stack = getItemStack();
        if (!stack.is(getItem())) {
            return;
        }

        HolderLookup.Provider registries = getPlayer().level().registryAccess();
        CompoundTag data = new CompoundTag();
        this.logic.writeToNBT(data, registries);
        data.putBoolean(TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID, this.pullRecipeInputsToRealGrid);
        data.putBoolean(TAG_PULLED_ANVIL_MODE, this.pulledAnvilMode);
        data.putBoolean(TAG_ENCODE_RESONATING_PATTERN, this.encodeResonatingPattern);
        data.putString(TAG_PROCESSING_INGREDIENT_TRANSFER_MODE, this.processingIngredientTransferMode.name());
        this.pulledCraftingInputInv.writeToNBT(data, TAG_PULLED_CRAFTING_INPUTS, registries);
        this.pulledProcessingInputInv.writeToNBT(data, TAG_PULLED_PROCESSING_INPUTS, registries);
        this.pulledSmithingInputInv.writeToNBT(data, TAG_PULLED_SMITHING_INPUTS, registries);
        this.pulledStonecuttingInputInv.writeToNBT(data, TAG_PULLED_STONECUTTING_INPUTS, registries);
        this.pulledAnvilInputInv.writeToNBT(data, TAG_PULLED_ANVIL_INPUTS, registries);
        stack.set(AECSDataComponents.WIRELESS_RESONANT_TERMINAL_DATA, data);
    }

    @Override
    public boolean isPullRecipeInputsToRealGrid() {
        return this.pullRecipeInputsToRealGrid;
    }

    @Override
    public void setPullRecipeInputsToRealGrid(boolean pullRecipeInputsToRealGrid) {
        if (this.pullRecipeInputsToRealGrid == pullRecipeInputsToRealGrid) {
            return;
        }

        this.pullRecipeInputsToRealGrid = pullRecipeInputsToRealGrid;
        if (!pullRecipeInputsToRealGrid) {
            this.pulledAnvilMode = false;
        }
        markForSave();
    }

    @Override
    public boolean isPulledAnvilMode() {
        return this.pulledAnvilMode;
    }

    @Override
    public void setPulledAnvilMode(boolean pulledAnvilMode) {
        pulledAnvilMode = pulledAnvilMode && this.pullRecipeInputsToRealGrid;
        if (this.pulledAnvilMode == pulledAnvilMode) {
            return;
        }

        this.pulledAnvilMode = pulledAnvilMode;
        markForSave();
    }

    @Override
    public boolean isEncodeResonatingPattern() {
        return this.encodeResonatingPattern;
    }

    @Override
    public void setEncodeResonatingPattern(boolean encodeResonatingPattern) {
        if (this.encodeResonatingPattern == encodeResonatingPattern) {
            return;
        }

        this.encodeResonatingPattern = encodeResonatingPattern;
        markForSave();
    }

    @Override
    public ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode getProcessingIngredientTransferMode() {
        return this.processingIngredientTransferMode;
    }

    @Override
    public void setProcessingIngredientTransferMode(
                                                    ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode processingIngredientTransferMode) {
        if (processingIngredientTransferMode == null) {
            processingIngredientTransferMode = ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE;
        }
        if (this.processingIngredientTransferMode == processingIngredientTransferMode) {
            return;
        }

        this.processingIngredientTransferMode = processingIngredientTransferMode;
        markForSave();
    }

    @Override
    public AppEngInternalInventory getPulledCraftingInputInv() {
        return this.pulledCraftingInputInv;
    }

    @Override
    public AppEngInternalInventory getPulledProcessingInputInv() {
        return this.pulledProcessingInputInv;
    }

    @Override
    public AppEngInternalInventory getPulledSmithingInputInv() {
        return this.pulledSmithingInputInv;
    }

    @Override
    public AppEngInternalInventory getPulledStonecuttingInputInv() {
        return this.pulledStonecuttingInputInv;
    }

    @Override
    public AppEngInternalInventory getPulledAnvilInputInv() {
        return this.pulledAnvilInputInv;
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        markForSave();
    }

    private void readFromItem() {
        CompoundTag data = getItemStack().get(AECSDataComponents.WIRELESS_RESONANT_TERMINAL_DATA);
        if (data == null) {
            return;
        }

        HolderLookup.Provider registries = getPlayer().level().registryAccess();
        this.logic.readFromNBT(data, registries);
        this.pullRecipeInputsToRealGrid = data.getBoolean(TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID);
        this.pulledAnvilMode = this.pullRecipeInputsToRealGrid && data.getBoolean(TAG_PULLED_ANVIL_MODE);
        this.encodeResonatingPattern = data.getBoolean(TAG_ENCODE_RESONATING_PATTERN);
        this.processingIngredientTransferMode = readProcessingIngredientTransferMode(data);
        this.pulledCraftingInputInv.clear();
        this.pulledProcessingInputInv.clear();
        this.pulledSmithingInputInv.clear();
        this.pulledStonecuttingInputInv.clear();
        this.pulledAnvilInputInv.clear();
        this.pulledCraftingInputInv.readFromNBT(data, TAG_PULLED_CRAFTING_INPUTS, registries);
        this.pulledProcessingInputInv.readFromNBT(data, TAG_PULLED_PROCESSING_INPUTS, registries);
        this.pulledSmithingInputInv.readFromNBT(data, TAG_PULLED_SMITHING_INPUTS, registries);
        this.pulledStonecuttingInputInv.readFromNBT(data, TAG_PULLED_STONECUTTING_INPUTS, registries);
        this.pulledAnvilInputInv.readFromNBT(data, TAG_PULLED_ANVIL_INPUTS, registries);
    }

    private static ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode readProcessingIngredientTransferMode(
                                                                                                                        CompoundTag data) {
        if (!data.contains(TAG_PROCESSING_INGREDIENT_TRANSFER_MODE)) {
            return ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE;
        }

        try {
            return ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.valueOf(
                    data.getString(TAG_PROCESSING_INGREDIENT_TRANSFER_MODE));
        } catch (IllegalArgumentException ignored) {
            return ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE;
        }
    }
}
