package io.github.lounode.ae2cs.common.me.part;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 谐振样板编码终端 Part — 面板形式，使用自定义菜单类型。
 * 继承 PatternEncodingTerminalPart 获得完整 NBT / 掉落物 / logic 逻辑。
 */
public class ResonantTemplateCodingTerminalPart extends PatternEncodingTerminalPart
                                                implements IResonantTemplateCodingTerminalHost {

    private static final String TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID = "PullRecipeInputsToRealGrid";
    private static final String TAG_PULLED_ANVIL_MODE = "PulledAnvilMode";
    private static final String TAG_ENCODE_RESONATING_PATTERN = "EncodeResonatingPattern";
    private static final String TAG_PROCESSING_INGREDIENT_TRANSFER_MODE = "ProcessingIngredientTransferMode";
    private static final String TAG_PULLED_CRAFTING_INPUTS = "PulledCraftingInputs";
    private static final String TAG_PULLED_PROCESSING_INPUTS = "PulledProcessingInputs";
    private static final String TAG_PULLED_SMITHING_INPUTS = "PulledSmithingInputs";
    private static final String TAG_PULLED_STONECUTTING_INPUTS = "PulledStonecuttingInputs";
    private static final String TAG_PULLED_ANVIL_INPUTS = "PulledAnvilInputs";

    @PartModels
    public static final ResourceLocation MODEL_BASE_RES = AE2CrystalScience.makeId("part/resonant_template_coding_terminal_base");
    @PartModels
    public static final ResourceLocation MODEL_OFF = AE2CrystalScience.makeId("part/resonant_template_coding_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AE2CrystalScience.makeId("part/resonant_template_coding_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE_RES, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE_RES, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE_RES, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private boolean pullRecipeInputsToRealGrid;
    private boolean pulledAnvilMode;
    private boolean encodeResonatingPattern;
    private ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode processingIngredientTransferMode = ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode.MERGE;
    private final AppEngInternalInventory pulledCraftingInputInv = new AppEngInternalInventory(this, 9);
    private final AppEngInternalInventory pulledProcessingInputInv = new AppEngInternalInventory(this, AEProcessingPattern.MAX_INPUT_SLOTS);
    private final AppEngInternalInventory pulledSmithingInputInv = new AppEngInternalInventory(this, 3);
    private final AppEngInternalInventory pulledStonecuttingInputInv = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory pulledAnvilInputInv = new AppEngInternalInventory(this, 2);

    public ResonantTemplateCodingTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.pullRecipeInputsToRealGrid = data.getBoolean(TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID);
        this.pulledAnvilMode = data.getBoolean(TAG_PULLED_ANVIL_MODE);
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

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean(TAG_PULL_RECIPE_INPUTS_TO_REAL_GRID, this.pullRecipeInputsToRealGrid);
        data.putBoolean(TAG_PULLED_ANVIL_MODE, this.pulledAnvilMode);
        data.putBoolean(TAG_ENCODE_RESONATING_PATTERN, this.encodeResonatingPattern);
        data.putString(TAG_PROCESSING_INGREDIENT_TRANSFER_MODE, this.processingIngredientTransferMode.name());
        this.pulledCraftingInputInv.writeToNBT(data, TAG_PULLED_CRAFTING_INPUTS, registries);
        this.pulledProcessingInputInv.writeToNBT(data, TAG_PULLED_PROCESSING_INPUTS, registries);
        this.pulledSmithingInputInv.writeToNBT(data, TAG_PULLED_SMITHING_INPUTS, registries);
        this.pulledStonecuttingInputInv.writeToNBT(data, TAG_PULLED_STONECUTTING_INPUTS, registries);
        this.pulledAnvilInputInv.writeToNBT(data, TAG_PULLED_ANVIL_INPUTS, registries);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        addInventoryDrops(drops, this.pulledCraftingInputInv);
        addInventoryDrops(drops, this.pulledProcessingInputInv);
        addInventoryDrops(drops, this.pulledSmithingInputInv);
        addInventoryDrops(drops, this.pulledStonecuttingInputInv);
        addInventoryDrops(drops, this.pulledAnvilInputInv);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.pulledCraftingInputInv.clear();
        this.pulledProcessingInputInv.clear();
        this.pulledSmithingInputInv.clear();
        this.pulledStonecuttingInputInv.clear();
        this.pulledAnvilInputInv.clear();
    }

    @Override
    public MenuType<?> getMenuType(Player p) {
        return AECSMenus.RESONANT_TEMPLATE_CODING_TERM_MENU.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
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
        this.saveChanges();
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
        this.saveChanges();
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
        this.saveChanges();
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
        this.saveChanges();
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
        this.saveChanges();
    }

    public void saveChanges() {
        this.getHost().markForSave();
    }

    private static void addInventoryDrops(List<ItemStack> drops, AppEngInternalInventory inv) {
        for (ItemStack stack : inv) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
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
