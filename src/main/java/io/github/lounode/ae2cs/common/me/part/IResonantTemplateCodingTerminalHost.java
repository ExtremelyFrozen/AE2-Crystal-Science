package io.github.lounode.ae2cs.common.me.part;

import appeng.util.inv.AppEngInternalInventory;

public interface IResonantTemplateCodingTerminalHost {
    boolean isPullRecipeInputsToRealGrid();

    void setPullRecipeInputsToRealGrid(boolean pullRecipeInputsToRealGrid);

    boolean isPulledAnvilMode();

    void setPulledAnvilMode(boolean pulledAnvilMode);

    AppEngInternalInventory getPulledCraftingInputInv();

    AppEngInternalInventory getPulledProcessingInputInv();

    AppEngInternalInventory getPulledSmithingInputInv();

    AppEngInternalInventory getPulledStonecuttingInputInv();

    AppEngInternalInventory getPulledAnvilInputInv();
}
