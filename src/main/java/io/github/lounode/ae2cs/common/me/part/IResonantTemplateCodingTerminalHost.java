package io.github.lounode.ae2cs.common.me.part;

import appeng.util.inv.AppEngInternalInventory;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;

public interface IResonantTemplateCodingTerminalHost {
    boolean isPullRecipeInputsToRealGrid();

    void setPullRecipeInputsToRealGrid(boolean pullRecipeInputsToRealGrid);

    boolean isPulledAnvilMode();

    void setPulledAnvilMode(boolean pulledAnvilMode);

    boolean isEncodeResonatingPattern();

    void setEncodeResonatingPattern(boolean encodeResonatingPattern);

    ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode getProcessingIngredientTransferMode();

    void setProcessingIngredientTransferMode(
            ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode processingIngredientTransferMode);

    AppEngInternalInventory getPulledCraftingInputInv();

    AppEngInternalInventory getPulledProcessingInputInv();

    AppEngInternalInventory getPulledSmithingInputInv();

    AppEngInternalInventory getPulledStonecuttingInputInv();

    AppEngInternalInventory getPulledAnvilInputInv();
}
