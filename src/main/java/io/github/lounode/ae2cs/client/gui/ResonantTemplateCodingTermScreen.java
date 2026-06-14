package io.github.lounode.ae2cs.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import appeng.api.config.ActionItems;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Tooltip;
import appeng.client.gui.AEBaseScreen;
import appeng.client.Point;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.Icon;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.StonecuttingEncodingPanel;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.parts.encoding.EncodingMode;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.ae2cs.common.init.client.AECSKeyMappings;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu.ProcessingIngredientTransferMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

/**
 * 谐振样板编码终端界面 - 固定尺寸 195x268，处理样板模式使用 4x4 输入网格带滑块。
 * 布局坐标来自 assets/ae2/screens/resonant_template_coding_terminal.json。
 */
public class ResonantTemplateCodingTermScreen extends PatternEncodingTermScreen<ResonantTemplateCodingTermMenu> {
    private static final int PROCESSING_INPUT_COLUMNS = 4;
    private static final int VISIBLE_PROCESSING_INPUT_ROWS = 4;
    private static final int VISIBLE_PROCESSING_OUTPUT_ROWS = 4;
    private static final int SLOT_SPACING = 18;
    private static final String PROCESSING_MODE_PANEL_ID = "modePanel1";
    private static final String PROCESSING_SCROLLBAR_ID = "processingPatternModeScrollbar";
    private static final String STONECUTTING_MODE_PANEL_ID = "modePanel3";
    private static final String STONECUTTING_RECIPE_LIST_ID = "stonecuttingRecipeList";
    private static final String ANVIL_NAME_FIELD_ID = "pulledAnvilName";
    private static final String ANVIL_COST_TEXT_ID = "pulledAnvilCost";
    private static final String STYLE_PATH = "/screens/resonant_template_coding_terminal.json";
    private static final String PATTERN_ENCODING_MODE_TEXT = "样板编码模式";
    private static final String CRAFTING_TERMINAL_MODE_TEXT = "合成终端模式";
    private static final String CRAFTING_MODE_PANEL_BACKGROUND_ID = "resonantCraftingModePanel";
    private static final String SMITHING_MODE_PANEL_BACKGROUND_ID = "resonantSmithingModePanel";
    private static final String SMITHING_PULLED_MODE_PANEL_BACKGROUND_ID = "resonantSmithingPulledModePanel";
    private static final String STONECUTTING_VIRTUAL_MODE_PANEL_BACKGROUND_ID = "resonantStonecuttingVirtualModePanel";
    private static final String STONECUTTING_PULLED_MODE_PANEL_BACKGROUND_ID = "resonantStonecuttingPulledModePanel";
    private static final String ANVIL_PULLED_MODE_PANEL_BACKGROUND_ID = "resonantAnvilPulledModePanel";
    private static final ResourceLocation TERMINAL_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2cs", "textures/gui/terminal_icon.png");
    private static final List<ResourceLocation> EMPTY_SMITHING_TEMPLATE_SLOT_ICONS = List.of(
            ResourceLocation.withDefaultNamespace("item/empty_slot_smithing_template_armor_trim"),
            ResourceLocation.withDefaultNamespace("item/empty_slot_smithing_template_netherite_upgrade"));
    private static final Component MISSING_SMITHING_TEMPLATE_TOOLTIP =
            Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Blitter STONECUTTING_RECIPE_SLOT =
            Blitter.texture("guis/pattern_modes.png").src(124, 140, 20, 22);
    private static final Blitter STONECUTTING_RECIPE_SLOT_SELECTED =
            Blitter.texture("guis/pattern_modes.png").src(124, 162, 20, 22);
    private static final Blitter STONECUTTING_RECIPE_SLOT_HOVER =
            Blitter.texture("guis/pattern_modes.png").src(124, 184, 20, 22);
    private static final Blitter VIRTUAL_SLOT_MODE_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(0, 16, 16, 16);
    private static final Blitter REAL_SLOT_MODE_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(16, 16, 16, 16);
    private static final Blitter NORMAL_PATTERN_ENCODING_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(32, 16, 16, 16);
    private static final Blitter RESONATING_PATTERN_ENCODING_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(48, 16, 16, 16);
    private static final Blitter PROCESSING_TRANSFER_MERGE_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(0, 40, 8, 8);
    private static final Blitter PROCESSING_TRANSFER_PARTIAL_SPLIT_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(0, 48, 8, 8);
    private static final Blitter PROCESSING_TRANSFER_FULL_SPLIT_ICON =
            Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64).src(0, 56, 8, 8);

    private static final Field SCREEN_STYLE_FIELD = getField(AEBaseScreen.class, "style");
    private static final Field WIDGET_CONTAINER_STYLE_FIELD = getField(WidgetContainer.class, "style");
    private static final Field COMPOSITE_WIDGETS_FIELD = getField(WidgetContainer.class, "compositeWidgets");
    private static final Field WIDGETS_FIELD = getField(WidgetContainer.class, "widgets");
    private static final Method POPULATE_SCREEN_METHOD = getPopulateScreenMethod();

    private final ToggleButton encodeResonatingPatternButton;
    private final TerminalSlotModeButton pullProcessingRecipeInputsButton;
    private final Map<EncodingMode, ActionButton> pullEncodedInputsToGridButtons = new EnumMap<>(EncodingMode.class);
    private final Map<EncodingMode, ActionButton> clearPulledInputsToPlayerButtons = new EnumMap<>(EncodingMode.class);
    private final ActionButton clearPulledAnvilInputsToNetworkButton;
    private final ActionButton clearPulledAnvilInputsToPlayerButton;
    private final Map<String, Component> defaultModeTabMessages = new HashMap<>();
    private final ProcessingIngredientTransferModeButton splitProcessingIngredientsButton;
    private final AETextField pulledAnvilNameField;
    private Scrollbar processingPatternModeScrollbar;
    private Scrollbar stonecuttingPatternModeScrollbar;
    private PulledDisplayMode pulledDisplayMode = PulledDisplayMode.FOLLOW_ENCODING_MODE;
    private ScreenStyle liveStyle;
    private boolean updatingPulledAnvilName;
    private boolean focusPulledAnvilNameField;
    private Boolean pendingPulledAnvilMode;
    private String lastPulledAnvilMenuName = "";

    public ResonantTemplateCodingTermScreen(ResonantTemplateCodingTermMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.encodeResonatingPatternButton = new ResonatingPatternEncodingButton(
                menu::setEncodeResonatingPattern);
        this.encodeResonatingPatternButton.setHalfSize(true);
        this.encodeResonatingPatternButton.setDisableBackground(true);
        this.encodeResonatingPatternButton.setTooltipOn(List.of(
                Component.translatable("ae2cs.menu.resonant_template_coding_terminal.encode_resonating_pattern.on"),
                Component.translatable("ae2cs.menu.resonant_template_coding_terminal.encode_resonating_pattern.on_desc")));
        this.encodeResonatingPatternButton.setTooltipOff(List.of(
                Component.translatable("ae2cs.menu.resonant_template_coding_terminal.encode_resonating_pattern.off"),
                Component.translatable("ae2cs.menu.resonant_template_coding_terminal.encode_resonating_pattern.off_desc")));
        this.widgets.add("encodeResonatingPattern", this.encodeResonatingPatternButton);

        this.widgets.add(CRAFTING_MODE_PANEL_BACKGROUND_ID, new ModePanelBackground(CRAFTING_MODE_PANEL_BACKGROUND_ID,
                "modePanel0", () -> this.menu.getMode() == EncodingMode.CRAFTING));
        this.widgets.add(SMITHING_MODE_PANEL_BACKGROUND_ID, new ModePanelBackground(SMITHING_MODE_PANEL_BACKGROUND_ID,
                "modePanel2", () -> this.menu.getMode() == EncodingMode.SMITHING_TABLE
                        && !this.menu.pullProcessingRecipeInputs));
        this.widgets.add(SMITHING_PULLED_MODE_PANEL_BACKGROUND_ID,
                new ModePanelBackground(SMITHING_PULLED_MODE_PANEL_BACKGROUND_ID, "modePanel2",
                        () -> this.menu.getMode() == EncodingMode.SMITHING_TABLE
                                && this.menu.pullProcessingRecipeInputs && !isPulledAnvilModeSelected()));
        this.widgets.add(ANVIL_PULLED_MODE_PANEL_BACKGROUND_ID,
                new ModePanelBackground(ANVIL_PULLED_MODE_PANEL_BACKGROUND_ID, "modePanel4",
                        () -> this.menu.pullProcessingRecipeInputs && isPulledAnvilModeSelected()));
        this.pulledAnvilNameField = this.widgets.addTextField(ANVIL_NAME_FIELD_ID);
        this.pulledAnvilNameField.setMaxLength(50);
        this.pulledAnvilNameField.setResponder(this::onPulledAnvilNameChanged);
        this.pulledAnvilNameField.setVisible(false);
        this.pulledAnvilNameField.setEditable(false);
        this.pullProcessingRecipeInputsButton = new TerminalSlotModeButton(
                menu::setPullProcessingRecipeInputs);
        this.addToLeftToolbar(this.pullProcessingRecipeInputsButton);

        addPulledModeButtons(EncodingMode.CRAFTING, "Crafting");
        addPulledModeButtons(EncodingMode.PROCESSING, "Processing");
        addPulledModeButtons(EncodingMode.SMITHING_TABLE, "Smithing");
        addPulledModeButtons(EncodingMode.STONECUTTING, "Stonecutting");
        this.clearPulledAnvilInputsToNetworkButton = new ActionButton(ActionItems.S_STASH,
                this.menu::clearPulledAnvilInputsToNetwork);
        this.clearPulledAnvilInputsToNetworkButton.setHalfSize(true);
        this.clearPulledAnvilInputsToNetworkButton.setDisableBackground(true);
        this.clearPulledAnvilInputsToNetworkButton.setVisibility(false);
        this.widgets.add("clearPulledAnvilInputsToNetwork", this.clearPulledAnvilInputsToNetworkButton);
        this.clearPulledAnvilInputsToPlayerButton = new ActionButton(ActionItems.S_STASH_TO_PLAYER_INV,
                this.menu::clearPulledAnvilInputsToPlayer);
        this.clearPulledAnvilInputsToPlayerButton.setHalfSize(true);
        this.clearPulledAnvilInputsToPlayerButton.setDisableBackground(true);
        this.clearPulledAnvilInputsToPlayerButton.setVisibility(false);
        this.widgets.add("clearPulledAnvilInputsToPlayer", this.clearPulledAnvilInputsToPlayerButton);

        this.splitProcessingIngredientsButton = new ProcessingIngredientTransferModeButton(
                menu::cycleProcessingIngredientTransferMode);
        this.splitProcessingIngredientsButton.setHalfSize(true);
        this.splitProcessingIngredientsButton.setDisableBackground(true);
        this.widgets.add("splitProcessingIngredients", this.splitProcessingIngredientsButton);

        this.liveStyle = style;
        captureDefaultModeTabMessages();
        replaceModeTabButtons();
        replaceVanillaProcessingPanel();
        replaceVanillaStonecuttingPanel();
    }

    @Override
    public ScreenStyle getStyle() {
        return this.liveStyle != null ? this.liveStyle : super.getStyle();
    }

    @Override
    protected void updateBeforeRender() {
        reloadLiveStyle();
        super.updateBeforeRender();
        this.encodeResonatingPatternButton.setState(this.menu.encodeResonatingPattern);
        this.encodeResonatingPatternButton.setVisibility(this.menu.getMode() == EncodingMode.PROCESSING);
        this.pullProcessingRecipeInputsButton.setState(this.menu.pullProcessingRecipeInputs);
        this.pullProcessingRecipeInputsButton.setVisibility(true);
        if ((!this.menu.pullProcessingRecipeInputs || !this.menu.pulledAnvilMode) && isPulledAnvilModeSelected()) {
            this.pulledDisplayMode = PulledDisplayMode.FOLLOW_ENCODING_MODE;
        }
        if (this.pendingPulledAnvilMode != null && this.menu.pulledAnvilMode == this.pendingPulledAnvilMode) {
            this.pendingPulledAnvilMode = null;
        }
        if (this.pendingPulledAnvilMode == null
                && this.menu.pullProcessingRecipeInputs && this.menu.pulledAnvilMode && !isPulledAnvilModeSelected()) {
            this.pulledDisplayMode = PulledDisplayMode.ANVIL;
        }
        this.splitProcessingIngredientsButton.setMode(this.menu.processingIngredientTransferMode);
        this.splitProcessingIngredientsButton.setVisibility(this.menu.getMode() == EncodingMode.PROCESSING
                && !isPulledAnvilModeSelected());
        setTextContent("crafting_grid_title", this.menu.pullProcessingRecipeInputs
                ? Component.literal("合成终端")
                : Component.translatable("gui.ae2.PatternEncoding"));
        updateModeTabTooltips();
        updateModeTabButtons();
        updatePulledModeButtons();
        updateModeSpecificWidgets();
        updatePulledAnvilNameField();
        layoutPulledModeSlots();
        if (this.menu.getMode() != EncodingMode.PROCESSING || isPulledAnvilModeSelected()) {
            setProcessingTooltipAreasVisible(false, 0);
            setPulledProcessingInputSlotsVisible(false, 0);
        }
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        if (!isPulledAnvilModeSelected() || this.menu.getPulledAnvilResultSlot().getItem().isEmpty()) {
            return;
        }

        Component costText = this.menu.isPulledAnvilTooExpensive()
                ? Component.translatable("container.repair.expensive")
                : Component.translatable("container.repair.cost", this.menu.pulledAnvilCost);
        Rect2i bounds = getStyledWidgetBounds(ANVIL_COST_TEXT_ID);
        int color = shouldRenderPulledAnvilCostAsError() ? 0xFF6060 : 0x80FF20;
        guiGraphics.drawString(this.font, costText, bounds.getX(), bounds.getY(), color, true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (AECSKeyMappings.TOGGLE_RESONANT_TEMPLATE_CODING_SLOT_MODE.matches(keyCode, scanCode)) {
            this.menu.setPullProcessingRecipeInputs(!this.menu.pullProcessingRecipeInputs);
            if (this.menu.pullProcessingRecipeInputs && isPulledAnvilModeSelected()) {
                this.pulledDisplayMode = PulledDisplayMode.FOLLOW_ENCODING_MODE;
                this.pendingPulledAnvilMode = false;
                this.menu.setPulledAnvilMode(false);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean shouldRenderPulledAnvilCostAsError() {
        var player = Minecraft.getInstance().player;
        return this.menu.isPulledAnvilTooExpensive()
                || player != null && !player.getAbilities().instabuild
                        && player.experienceLevel < this.menu.pulledAnvilCost;
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlot(guiGraphics, slot);
        if (shouldShowCraftableIndicatorForPulledSlot(slot)) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 100.0f);
            StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, slot.x - 11, slot.y - 11, "+", false);
            poseStack.popPose();
        }
        renderEmptyPulledSmithingSlotIcon(guiGraphics, slot);
    }

    private boolean shouldShowCraftableIndicatorForPulledSlot(Slot slot) {
        SlotSemantic semantic = this.menu.getSlotSemantic(slot);
        if (semantic != ResonantTemplateCodingTermMenu.PULLED_CRAFTING_INPUTS
                && semantic != ResonantTemplateCodingTermMenu.PULLED_PROCESSING_INPUTS
                && semantic != ResonantTemplateCodingTermMenu.PULLED_SMITHING_TABLE_INPUTS
                && semantic != ResonantTemplateCodingTermMenu.PULLED_STONECUTTING_INPUT) {
            return false;
        }

        GenericStack slotContent = GenericStack.fromItemStack(slot.getItem());
        return slotContent != null && this.repo.isCraftable(slotContent.what());
    }

    private void renderEmptyPulledSmithingSlotIcon(GuiGraphics guiGraphics, Slot slot) {
        if (!this.menu.pullProcessingRecipeInputs || this.menu.getMode() != EncodingMode.SMITHING_TABLE
                || slot.hasItem()) {
            return;
        }

        ResourceLocation icon = null;
        AppEngSlot[] pulledSlots = this.menu.getPulledSmithingInputSlots();
        if (pulledSlots.length > 0 && slot == pulledSlots[0]) {
            icon = getCyclingSlotIcon(EMPTY_SMITHING_TEMPLATE_SLOT_ICONS);
        } else if (pulledSlots.length > 1 && slot == pulledSlots[1]) {
            icon = getPulledSmithingTemplateItem()
                    .map(SmithingTemplateItem::getBaseSlotEmptyIcons)
                    .map(this::getCyclingSlotIcon)
                    .orElse(null);
        } else if (pulledSlots.length > 2 && slot == pulledSlots[2]) {
            icon = getPulledSmithingTemplateItem()
                    .map(SmithingTemplateItem::getAdditionalSlotEmptyIcons)
                    .map(this::getCyclingSlotIcon)
                    .orElse(null);
        }

        if (icon != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(icon);
            guiGraphics.blit(slot.x, slot.y, 0, 16, 16, sprite, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private java.util.Optional<SmithingTemplateItem> getPulledSmithingTemplateItem() {
        AppEngSlot[] pulledSlots = this.menu.getPulledSmithingInputSlots();
        if (pulledSlots.length == 0) {
            return java.util.Optional.empty();
        }

        ItemStack templateStack = pulledSlots[0].getItem();
        return !templateStack.isEmpty() && templateStack.getItem() instanceof SmithingTemplateItem smithingTemplateItem
                ? java.util.Optional.of(smithingTemplateItem)
                : java.util.Optional.empty();
    }

    private ResourceLocation getCyclingSlotIcon(List<ResourceLocation> icons) {
        if (icons.isEmpty()) {
            return null;
        }

        int index = (int) ((System.currentTimeMillis() / 1000L) % icons.size());
        return icons.get(index);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        Component smithingTooltip = getPulledSmithingOnboardingTooltip();
        if (smithingTooltip != null) {
            guiGraphics.renderTooltip(this.font, this.font.split(smithingTooltip, 115), x, y);
        }
    }

    private Component getPulledSmithingOnboardingTooltip() {
        if (!this.menu.pullProcessingRecipeInputs || this.menu.getMode() != EncodingMode.SMITHING_TABLE
                || this.hoveredSlot == null || this.hoveredSlot.hasItem()) {
            return null;
        }

        AppEngSlot[] pulledSlots = this.menu.getPulledSmithingInputSlots();
        if (pulledSlots.length > 0 && this.hoveredSlot == pulledSlots[0]
                && getPulledSmithingTemplateItem().isEmpty()) {
            return MISSING_SMITHING_TEMPLATE_TOOLTIP;
        }

        return getPulledSmithingTemplateItem()
                .map(template -> {
                    if (pulledSlots.length > 1 && this.hoveredSlot == pulledSlots[1]) {
                        return template.getBaseSlotDescription();
                    }
                    if (pulledSlots.length > 2 && this.hoveredSlot == pulledSlots[2]) {
                        return template.getAdditionSlotDescription();
                    }
                    return null;
                })
                .orElse(null);
    }

    private void reloadLiveStyle() {
        try {
            ScreenStyle reloadedStyle = StyleManager.loadStyleDoc(STYLE_PATH);
            if (reloadedStyle != this.liveStyle) {
                this.liveStyle = reloadedStyle;
                applyLiveStyle();
            }
        } catch (RuntimeException ignored) {
            this.liveStyle = super.getStyle();
        }
    }

    private void applyLiveStyle() {
        try {
            SCREEN_STYLE_FIELD.set(this, this.liveStyle);
            WIDGET_CONTAINER_STYLE_FIELD.set(this.widgets, this.liveStyle);
            repositionStyledSlots();
            repopulateWidgetsFromLiveStyle();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to apply resonant template live style", e);
        }
    }

    private void repositionStyledSlots() {
        for (String semanticId : this.liveStyle.getSlots().keySet()) {
            SlotSemantic semantic = SlotSemantics.getOrThrow(semanticId);
            if (semantic != SlotSemantics.PROCESSING_INPUTS && semantic != SlotSemantics.PROCESSING_OUTPUTS) {
                this.repositionSlots(semantic);
            }
        }
    }

    private void repopulateWidgetsFromLiveStyle() throws ReflectiveOperationException {
        POPULATE_SCREEN_METHOD.invoke(this.widgets, (Consumer<AbstractWidget>) widget -> {
        }, new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight), this);
    }

    private void layoutProcessingSlots(int scrollRows) {
        layoutProcessingInputsAsFourColumns(scrollRows);
        layoutProcessingOutputs(scrollRows);
    }

    private void layoutPulledModeSlots() {
        boolean pulled = this.menu.pullProcessingRecipeInputs;
        EncodingMode mode = this.menu.getMode();

        restorePullableVirtualSlots(mode);
        if (pulled && isPulledAnvilModeSelected()) {
            hideAllNonAnvilModeSlots();
        }

        boolean showPulledSlots = pulled && !isPulledAnvilModeSelected();

        layoutPulledSlots(this.menu.getCraftingGridSlots(), this.menu.getPulledCraftingInputSlots(),
                showPulledSlots && mode == EncodingMode.CRAFTING);
        layoutPulledSlot(this.menu.getSlots(SlotSemantics.CRAFTING_RESULT).getFirst(),
                this.menu.getPulledCraftingResultSlot(), showPulledSlots && mode == EncodingMode.CRAFTING);
        layoutPulledSlots(new FakeSlot[] {
                this.menu.getSmithingTableTemplateSlot(),
                this.menu.getSmithingTableBaseSlot(),
                this.menu.getSmithingTableAdditionSlot()
        }, this.menu.getPulledSmithingInputSlots(), showPulledSlots && mode == EncodingMode.SMITHING_TABLE);
        var smithingResultSlots = this.menu.getSlots(SlotSemantics.SMITHING_TABLE_RESULT);
        if (!smithingResultSlots.isEmpty()) {
            layoutPulledSlot(smithingResultSlots.getFirst(), this.menu.getPulledSmithingResultSlot(),
                    showPulledSlots && mode == EncodingMode.SMITHING_TABLE);
        } else {
            moveSlotOffscreen(this.menu.getPulledSmithingResultSlot());
        }
        layoutPulledSlotAtWidget("pulledStonecuttingInput", this.menu.getPulledStonecuttingInputSlot(),
                showPulledSlots && mode == EncodingMode.STONECUTTING);
        layoutPulledSlotAtWidget("pulledStonecuttingResult", this.menu.getPulledStonecuttingResultSlot(),
                showPulledSlots && mode == EncodingMode.STONECUTTING);
        AppEngSlot[] anvilInputs = this.menu.getPulledAnvilInputSlots();
        layoutPulledSlotAtWidget("pulledAnvilInput0", anvilInputs[0], pulled && isPulledAnvilModeSelected());
        layoutPulledSlotAtWidget("pulledAnvilInput1", anvilInputs[1], pulled && isPulledAnvilModeSelected());
        layoutPulledSlotAtWidget("pulledAnvilResult", this.menu.getPulledAnvilResultSlot(),
                pulled && isPulledAnvilModeSelected());

        if (mode == EncodingMode.CRAFTING) {
            this.setSlotsHidden(SlotSemantics.CRAFTING_GRID, pulled);
            this.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, pulled);
        } else if (mode == EncodingMode.SMITHING_TABLE) {
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, pulled);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, pulled);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, pulled);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, pulled);
        } else if (mode == EncodingMode.STONECUTTING) {
            this.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, pulled);
        }
    }

    private void hideAllNonAnvilModeSlots() {
        this.setSlotsHidden(SlotSemantics.CRAFTING_GRID, true);
        this.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, true);
        this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, true);
        this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, true);
        this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, true);
        this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, true);
        this.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, true);

        hideProcessingSlots();
    }

    private void hideProcessingSlots() {
        for (FakeSlot slot : this.menu.getProcessingInputSlots()) {
            moveSlotOffscreen(slot);
            slot.setActive(false);
        }
        for (FakeSlot slot : this.menu.getProcessingOutputSlots()) {
            moveSlotOffscreen(slot);
            slot.setActive(false);
        }
        for (AppEngSlot slot : this.menu.getPulledProcessingInputSlots()) {
            moveSlotOffscreen(slot);
            slot.setActive(false);
        }
        setProcessingTooltipAreasVisible(false, 0);
    }

    private void restorePullableVirtualSlots(EncodingMode mode) {
        if (mode == EncodingMode.CRAFTING) {
            this.setSlotsHidden(SlotSemantics.CRAFTING_GRID, false);
            this.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, false);
        } else if (mode == EncodingMode.SMITHING_TABLE) {
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, false);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, false);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, false);
            this.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, false);
        } else if (mode == EncodingMode.STONECUTTING) {
            this.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, false);
        }
    }

    private void layoutPulledSlots(FakeSlot[] sourceSlots, AppEngSlot[] pulledSlots, boolean visible) {
        for (int i = 0; i < sourceSlots.length && i < pulledSlots.length; i++) {
            layoutPulledSlot(sourceSlots[i], pulledSlots[i], visible);
        }
    }

    private void layoutPulledSlot(net.minecraft.world.inventory.Slot sourceSlot,
            net.minecraft.world.inventory.Slot pulledSlot, boolean visible) {
        if (visible) {
            pulledSlot.x = sourceSlot.x;
            pulledSlot.y = sourceSlot.y;
        } else {
            moveSlotOffscreen(pulledSlot);
        }
        if (pulledSlot instanceof AppEngSlot appEngSlot) {
            appEngSlot.setActive(visible);
        }
    }

    private void moveSlotOffscreen(net.minecraft.world.inventory.Slot slot) {
        slot.x = -10000;
        slot.y = -10000;
    }

    private void layoutPulledSlotAtWidget(String widgetId, net.minecraft.world.inventory.Slot slot, boolean visible) {
        if (!visible) {
            moveSlotOffscreen(slot);
            return;
        }

        Rect2i bounds = getStyledWidgetBounds(widgetId);
        slot.x = bounds.getX();
        slot.y = bounds.getY();
        if (slot instanceof AppEngSlot appEngSlot) {
            appEngSlot.setActive(true);
        }
    }

    private void updatePulledModeButtons() {
        boolean pulled = this.menu.pullProcessingRecipeInputs;
        EncodingMode mode = this.menu.getMode();

        this.pullEncodedInputsToGridButtons.forEach((buttonMode, button) ->
                button.setVisibility(pulled && buttonMode == mode && !isPulledAnvilModeSelected()));
        this.clearPulledInputsToPlayerButtons.forEach((buttonMode, button) ->
                button.setVisibility(pulled && buttonMode == mode && !isPulledAnvilModeSelected()));
        this.clearPulledAnvilInputsToNetworkButton.setVisibility(pulled && isPulledAnvilModeSelected());
        this.clearPulledAnvilInputsToPlayerButton.setVisibility(pulled && isPulledAnvilModeSelected());

        if (pulled && mode == EncodingMode.CRAFTING) {
            setWidgetVisible("craftingSubstitutions", false);
            setWidgetVisible("craftingFluidSubstitutions", false);
            setWidgetVisible("craftingClearPattern", false);
        } else if (pulled && mode == EncodingMode.SMITHING_TABLE) {
            setWidgetVisible("smithingTableSubstitutions", false);
            setWidgetVisible("smithingTableClearPattern", false);
        } else if (pulled && mode == EncodingMode.PROCESSING) {
            setWidgetVisible("processingClearPattern", false);
            setWidgetVisible("splitProcessingIngredients", false);
        }
    }

    private void addPulledModeButtons(EncodingMode mode, String suffix) {
        ActionButton pullButton = new ActionButton(ActionItems.S_STASH, this.menu::clearPulledInputsToNetwork);
        pullButton.setHalfSize(true);
        pullButton.setDisableBackground(true);
        pullButton.setVisibility(false);
        this.widgets.add("pullEncodedInputsToGrid" + suffix, pullButton);
        this.pullEncodedInputsToGridButtons.put(mode, pullButton);

        ActionButton clearButton = new ActionButton(ActionItems.S_STASH_TO_PLAYER_INV,
                this.menu::clearPulledInputsToPlayer);
        clearButton.setHalfSize(true);
        clearButton.setDisableBackground(true);
        clearButton.setVisibility(false);
        this.widgets.add("clearPulledInputsToPlayer" + suffix, clearButton);
        this.clearPulledInputsToPlayerButtons.put(mode, clearButton);
    }

    private void captureDefaultModeTabMessages() {
        for (int i = 0; i < 4; i++) {
            String widgetId = "modeTabButton" + i;
            TabButton tabButton = getModeTabButton(widgetId);
            if (tabButton != null) {
                this.defaultModeTabMessages.put(widgetId, tabButton.getMessage());
            }
        }
    }

    private void updateModeTabTooltips() {
        for (int i = 0; i < 4; i++) {
            String widgetId = "modeTabButton" + i;
            TabButton tabButton = getModeTabButton(widgetId);
            if (tabButton == null) {
                continue;
            }

            Component message = this.menu.pullProcessingRecipeInputs
                    ? getPulledModeTabMessage(i)
                    : this.defaultModeTabMessages.get(widgetId);
            if (message != null) {
                tabButton.setMessage(message);
            }
        }
        TabButton anvilTab = getModeTabButton("modeTabButton4");
        if (anvilTab != null) {
            anvilTab.setMessage(Component.literal("铁砧"));
        }
    }

    private Component getPulledModeTabMessage(int modeIndex) {
        return switch (modeIndex) {
            case 0 -> Component.literal("工作台");
            case 1 -> Component.literal("处理配方");
            case 2 -> Component.literal("锻造台");
            case 3 -> Component.literal("切石机");
            default -> throw new IllegalArgumentException("Unknown mode tab index " + modeIndex);
        };
    }

    private void updateModeTabButtons() {
        for (int i = 0; i < 4; i++) {
            TabButton tabButton = getModeTabButton("modeTabButton" + i);
            if (tabButton == null) {
                continue;
            }
            tabButton.visible = true;
            tabButton.active = true;
            tabButton.setSelected(!isPulledAnvilModeSelected() && getModeIndex(this.menu.getMode()) == i);
        }

        TabButton anvilTab = getModeTabButton("modeTabButton4");
        if (anvilTab != null) {
            boolean visible = this.menu.pullProcessingRecipeInputs;
            anvilTab.visible = visible;
            anvilTab.active = visible;
            anvilTab.setSelected(isPulledAnvilModeSelected());
        }
    }

    private boolean isPulledAnvilModeSelected() {
        return this.pulledDisplayMode == PulledDisplayMode.ANVIL;
    }

    private static int getModeIndex(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> 0;
            case PROCESSING -> 1;
            case SMITHING_TABLE -> 2;
            case STONECUTTING -> 3;
        };
    }

    @SuppressWarnings("unchecked")
    private void replaceModeTabButtons() {
        try {
            Map<String, AbstractWidget> widgetMap = (Map<String, AbstractWidget>) WIDGETS_FIELD.get(this.widgets);
            for (int i = 0; i < 4; i++) {
                String widgetId = "modeTabButton" + i;
                TabButton original = getModeTabButton(widgetId);
                if (original == null) {
                    continue;
                }
                int modeIndex = i;
                ResonantModeTabButton replacement = new ResonantModeTabButton(modeIndex,
                        getDefaultModeTabIcon(modeIndex), original.getMessage(), button -> {
                    this.pulledDisplayMode = PulledDisplayMode.FOLLOW_ENCODING_MODE;
                    this.pendingPulledAnvilMode = false;
                    this.menu.setPulledAnvilMode(false);
                    if (this.menu.pullProcessingRecipeInputs) {
                        this.menu.prepareTransferMode(getEncodingMode(modeIndex));
                    } else {
                        this.menu.setMode(getEncodingMode(modeIndex));
                    }
                });
                replacement.setStyle(original.getStyle());
                replacement.setDisableBackground(original.isDisableBackground());
                widgetMap.put(widgetId, replacement);
            }

            ResonantModeTabButton anvilTab = new ResonantModeTabButton(4, Icon.CRAFT_HAMMER,
                    Component.literal("铁砧"), button -> {
                if (this.menu.pullProcessingRecipeInputs) {
                    this.pulledDisplayMode = PulledDisplayMode.ANVIL;
                    this.pendingPulledAnvilMode = true;
                    this.menu.setPulledAnvilMode(true);
                }
            });
            anvilTab.setStyle(TabButton.Style.HORIZONTAL);
            anvilTab.visible = false;
            anvilTab.active = false;
            this.widgets.add("modeTabButton4", anvilTab);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to replace resonant template mode tab buttons", e);
        }
    }

    private static EncodingMode getEncodingMode(int modeIndex) {
        return switch (modeIndex) {
            case 0 -> EncodingMode.CRAFTING;
            case 1 -> EncodingMode.PROCESSING;
            case 2 -> EncodingMode.SMITHING_TABLE;
            case 3 -> EncodingMode.STONECUTTING;
            default -> throw new IllegalArgumentException("Unknown mode tab index " + modeIndex);
        };
    }

    private static Icon getDefaultModeTabIcon(int modeIndex) {
        return switch (modeIndex) {
            case 0 -> Icon.TAB_CRAFTING;
            case 1 -> Icon.TAB_PROCESSING;
            case 2 -> Icon.TAB_SMITHING;
            case 3 -> Icon.TAB_STONECUTTING;
            default -> Icon.CRAFT_HAMMER;
        };
    }

    private void updateModeSpecificWidgets() {
        boolean anvil = isPulledAnvilModeSelected();
        setScrollbarVisible(this.processingPatternModeScrollbar,
                !anvil && this.menu.getMode() == EncodingMode.PROCESSING);
        setScrollbarVisible(this.stonecuttingPatternModeScrollbar,
                !anvil && this.menu.getMode() == EncodingMode.STONECUTTING);
    }

    private static void setScrollbarVisible(Scrollbar scrollbar, boolean visible) {
        if (scrollbar != null) {
            scrollbar.setVisible(visible);
        }
    }

    private void updatePulledAnvilNameField() {
        boolean visible = this.menu.pullProcessingRecipeInputs && isPulledAnvilModeSelected();
        this.pulledAnvilNameField.setVisible(visible);
        this.pulledAnvilNameField.setEditable(visible);
        if (!visible) {
            this.pulledAnvilNameField.setFocused(false);
            this.lastPulledAnvilMenuName = "";
            return;
        }

        Rect2i bounds = getStyledWidgetBounds(ANVIL_NAME_FIELD_ID);
        this.pulledAnvilNameField.setX(this.leftPos + bounds.getX() + 2);
        this.pulledAnvilNameField.setY(this.topPos + bounds.getY() + 1);
        this.pulledAnvilNameField.setWidth(bounds.getWidth() - 4);
        this.pulledAnvilNameField.setHeight(bounds.getHeight());

        String menuName = this.menu.getPulledAnvilItemName();
        if (menuName != null && !menuName.equals(this.lastPulledAnvilMenuName)) {
            this.updatingPulledAnvilName = true;
            this.pulledAnvilNameField.setValue(menuName);
            this.updatingPulledAnvilName = false;
            if (this.lastPulledAnvilMenuName.isEmpty() && !menuName.isEmpty()) {
                this.focusPulledAnvilNameField = true;
            }
            this.lastPulledAnvilMenuName = menuName;
        }

        if (this.focusPulledAnvilNameField) {
            this.setFocused(this.pulledAnvilNameField);
            this.pulledAnvilNameField.setFocused(true);
            this.pulledAnvilNameField.selectAll();
            this.focusPulledAnvilNameField = false;
        }
    }

    private void onPulledAnvilNameChanged(String value) {
        if (!this.updatingPulledAnvilName && this.menu.pullProcessingRecipeInputs && isPulledAnvilModeSelected()) {
            this.menu.setPulledAnvilItemName(value);
        }
    }

    @SuppressWarnings("unchecked")
    private TabButton getModeTabButton(String widgetId) {
        try {
            Map<String, AbstractWidget> widgetMap = (Map<String, AbstractWidget>) WIDGETS_FIELD.get(this.widgets);
            AbstractWidget widget = widgetMap.get(widgetId);
            return widget instanceof TabButton tabButton ? tabButton : null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to find mode tab button " + widgetId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setWidgetVisible(String widgetId, boolean visible) {
        try {
            Map<String, AbstractWidget> widgetMap = (Map<String, AbstractWidget>) WIDGETS_FIELD.get(this.widgets);
            AbstractWidget widget = widgetMap.get(widgetId);
            if (widget != null) {
                widget.visible = visible;
                widget.active = visible;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to update widget visibility for " + widgetId, e);
        }
    }

    private void layoutProcessingInputsAsFourColumns(int scrollRows) {
        FakeSlot[] slots = this.menu.getProcessingInputSlots();
        AppEngSlot[] pulledSlots = this.menu.getPulledProcessingInputSlots();
        Point inputPos = getStyledSlotPosition(SlotSemantics.PROCESSING_INPUTS);
        boolean usePulledSlots = this.menu.pullProcessingRecipeInputs;

        for (int i = 0; i < slots.length; i++) {
            FakeSlot slot = slots[i];
            AppEngSlot pulledSlot = pulledSlots[i];
            int row = i / PROCESSING_INPUT_COLUMNS;
            int column = i % PROCESSING_INPUT_COLUMNS;
            int visibleRow = row - scrollRows;
            boolean visible = visibleRow >= 0 && visibleRow < VISIBLE_PROCESSING_INPUT_ROWS;
            int x = inputPos.getX() + column * SLOT_SPACING;
            int y = inputPos.getY() + visibleRow * SLOT_SPACING;

            slot.x = x;
            slot.y = y;
            slot.setActive(!usePulledSlots && visible);
            pulledSlot.x = x;
            pulledSlot.y = y;
            pulledSlot.setActive(usePulledSlots && visible);
        }
    }

    private void layoutProcessingOutputs(int scrollRows) {
        FakeSlot[] slots = this.menu.getProcessingOutputSlots();
        Point outputPos = getStyledSlotPosition(SlotSemantics.PROCESSING_OUTPUTS);

        for (int i = 0; i < slots.length; i++) {
            FakeSlot slot = slots[i];
            int visibleRow = i - scrollRows;

            slot.x = outputPos.getX();
            slot.y = outputPos.getY() + visibleRow * SLOT_SPACING;
            slot.setActive(visibleRow >= 0 && visibleRow < VISIBLE_PROCESSING_OUTPUT_ROWS);
        }
    }

    private int getMaxFirstVisibleProcessingInputRow() {
        return Math.max(0, Math.ceilDiv(this.menu.getProcessingInputSlots().length, PROCESSING_INPUT_COLUMNS)
                - VISIBLE_PROCESSING_INPUT_ROWS);
    }

    private Point getStyledSlotPosition(SlotSemantic semantic) {
        return this.getStyle().getSlots()
                .get(semantic.id())
                .resolve(new Rect2i(0, 0, this.imageWidth, this.imageHeight));
    }

    private Rect2i getStyledWidgetBounds(String widgetId) {
        WidgetStyle widgetStyle = this.getStyle().getWidget(widgetId);
        Point position = widgetStyle.resolve(new Rect2i(0, 0, this.imageWidth, this.imageHeight));
        return new Rect2i(position.getX(), position.getY(), widgetStyle.getWidth(), widgetStyle.getHeight());
    }

    @SuppressWarnings("unchecked")
    private void replaceVanillaProcessingPanel() {
        try {
            Map<String, ICompositeWidget> compositeWidgets =
                    (Map<String, ICompositeWidget>) COMPOSITE_WIDGETS_FIELD.get(this.widgets);
            ICompositeWidget processingPanel = compositeWidgets.get(PROCESSING_MODE_PANEL_ID);
            if (!(processingPanel instanceof ProcessingEncodingPanel)) {
                return;
            }

            Field scrollbarField = ProcessingEncodingPanel.class.getDeclaredField("scrollbar");
            scrollbarField.setAccessible(true);

            this.processingPatternModeScrollbar = (Scrollbar) scrollbarField.get(processingPanel);
            compositeWidgets.put(PROCESSING_MODE_PANEL_ID,
                    new ResonantProcessingPanel(this.processingPatternModeScrollbar));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to replace resonant template processing panel", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceVanillaStonecuttingPanel() {
        try {
            Map<String, ICompositeWidget> compositeWidgets =
                    (Map<String, ICompositeWidget>) COMPOSITE_WIDGETS_FIELD.get(this.widgets);
            ICompositeWidget stonecuttingPanel = compositeWidgets.get(STONECUTTING_MODE_PANEL_ID);
            if (!(stonecuttingPanel instanceof StonecuttingEncodingPanel)) {
                return;
            }

            Field scrollbarField = StonecuttingEncodingPanel.class.getDeclaredField("scrollbar");
            scrollbarField.setAccessible(true);

            this.stonecuttingPatternModeScrollbar = (Scrollbar) scrollbarField.get(stonecuttingPanel);
            compositeWidgets.put(STONECUTTING_MODE_PANEL_ID,
                    new ResonantStonecuttingPanel(this.stonecuttingPatternModeScrollbar));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to replace resonant template stonecutting panel", e);
        }
    }

    private static Field getField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to find field " + owner.getName() + "#" + name, e);
        }
    }

    private static Method getPopulateScreenMethod() {
        try {
            Method method = WidgetContainer.class.getDeclaredMethod("populateScreen", Consumer.class, Rect2i.class,
                    AEBaseScreen.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find WidgetContainer#populateScreen", e);
        }
    }

    private class ModePanelBackground implements ICompositeWidget {
        private final String imageId;
        private final String positionWidgetId;
        private final BooleanSupplier visible;
        private int width;
        private int height;

        private ModePanelBackground(String imageId, String positionWidgetId, BooleanSupplier visible) {
            this.imageId = imageId;
            this.positionWidgetId = positionWidgetId;
            this.visible = visible;
        }

        @Override
        public boolean isVisible() {
            return this.visible.getAsBoolean();
        }

        @Override
        public void setPosition(Point position) {
        }

        @Override
        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public Rect2i getBounds() {
            Rect2i positionBounds = getStyledWidgetBounds(this.positionWidgetId);
            return new Rect2i(positionBounds.getX(), positionBounds.getY(), this.width, this.height);
        }

        @Override
        public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
            Rect2i panelBounds = getBounds();
            Point drawPosition = new Point(panelBounds.getX(), panelBounds.getY()).move(bounds.getX(), bounds.getY());
            Blitter background = getStyle().getImage(this.imageId);
            background.dest(drawPosition.getX(), drawPosition.getY(), this.width, this.height).blit(guiGraphics);
        }
    }

    private static class ResonatingPatternEncodingButton extends ToggleButton {
        private boolean state;

        private ResonatingPatternEncodingButton(Listener listener) {
            super(Icon.S_PROCESSOR, Icon.S_CRAFT, listener);
        }

        @Override
        public void setState(boolean isOn) {
            super.setState(isOn);
            this.state = isOn;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
            if (!this.visible) {
                return;
            }

            Blitter icon = (this.state ? RESONATING_PATTERN_ENCODING_ICON : NORMAL_PATTERN_ENCODING_ICON).copy();
            if (!this.active) {
                icon.opacity(0.5f);
            }
            icon.dest(this.getX(), this.getY()).zOffset(20).blit(guiGraphics);
        }

        @Override
        public Rect2i getTooltipArea() {
            return new Rect2i(this.getX(), this.getY(), 16, 16);
        }
    }

    private static class TerminalSlotModeButton extends IconButton {
        private final ToggleButton.Listener listener;
        private boolean state;

        private TerminalSlotModeButton(ToggleButton.Listener listener) {
            super(null);
            this.listener = listener;
        }

        private void setState(boolean state) {
            this.state = state;
        }

        @Override
        public void onPress() {
            this.listener.onChange(!this.state);
        }

        @Override
        protected Icon getIcon() {
            return null;
        }

        @Override
        public List<Component> getTooltipMessage() {
            String targetMode = this.state ? PATTERN_ENCODING_MODE_TEXT : CRAFTING_TERMINAL_MODE_TEXT;
            return List.of(
                    Component.literal("点击切换：" + targetMode),
                    Component.literal("按下快捷键 ")
                            .append(getSlotModeKeyName().withStyle(ChatFormatting.BLUE))
                            .append(Component.literal(" 可切换")));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
            if (!this.visible) {
                return;
            }

            int yOffset = this.isHovered() ? 1 : 0;
            Icon backgroundIcon = this.isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                    : this.isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS
                    : Icon.TOOLBAR_BUTTON_BACKGROUND;
            int bgX = this.getX() - 1;
            int bgY = this.getY() + yOffset;
            int bgWidth = 18;
            int bgHeight = 20;
            backgroundIcon.getBlitter().dest(bgX, bgY, bgWidth, bgHeight).zOffset(2).blit(guiGraphics);

            Blitter icon = (this.state ? REAL_SLOT_MODE_ICON : VIRTUAL_SLOT_MODE_ICON).copy();
            if (!this.active) {
                icon.opacity(0.5f);
            }
            int iconWidth = icon.getSrcWidth();
            int iconHeight = icon.getSrcHeight();
            int iconX = bgX + (bgWidth - iconWidth) / 2;
            int iconY = bgY + (bgHeight - iconHeight) / 2;
            icon.dest(iconX, iconY).zOffset(3).blit(guiGraphics);
        }

        private static MutableComponent getSlotModeKeyName() {
            if (AECSKeyMappings.TOGGLE_RESONANT_TEMPLATE_CODING_SLOT_MODE.getKey() == InputConstants.UNKNOWN) {
                return Component.literal(I18n.get("ae2cs.menu.resonant_template_coding_terminal.hotkey_unbound"));
            }
            return AECSKeyMappings.TOGGLE_RESONANT_TEMPLATE_CODING_SLOT_MODE.getTranslatedKeyMessage().copy();
        }
    }

    private class ResonantModeTabButton extends TabButton {
        private final int iconIndex;

        private ResonantModeTabButton(int iconIndex, Icon fallbackIcon, Component message, OnPress onPress) {
            super(fallbackIcon, message, onPress);
            this.iconIndex = iconIndex;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
            if (!this.visible) {
                return;
            }
            if (!menu.pullProcessingRecipeInputs) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partial);
                return;
            }

            Icon backdrop = switch (this.getStyle()) {
                case CORNER -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS
                        : Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
                case BOX -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_FOCUS : Icon.TAB_BUTTON_BACKGROUND;
                case HORIZONTAL -> {
                    if (this.isFocused()) {
                        yield Icon.HORIZONTAL_TAB_FOCUS;
                    } else if (this.isSelected()) {
                        yield Icon.HORIZONTAL_TAB_SELECTED;
                    }
                    yield Icon.HORIZONTAL_TAB;
                }
            };
            if (!this.isDisableBackground()) {
                backdrop.getBlitter().dest(this.getX(), this.getY()).blit(guiGraphics);
            }

            Blitter icon = Blitter.texture(TERMINAL_ICON_TEXTURE, 80, 64)
                    .src(this.iconIndex * 16, 0, 16, 16);
            icon.dest(this.getX() + 3, this.getY() + 2).zOffset(3).blit(guiGraphics);
        }
    }

    private static class ProcessingIngredientTransferModeButton extends IconButton {
        private ProcessingIngredientTransferMode mode = ProcessingIngredientTransferMode.MERGE;

        private ProcessingIngredientTransferModeButton(Runnable onPress) {
            super(button -> onPress.run());
        }

        private void setMode(ProcessingIngredientTransferMode mode) {
            this.mode = mode;
        }

        @Override
        protected Icon getIcon() {
            return null;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
            if (!this.visible) {
                return;
            }

            Blitter icon = switch (this.mode) {
                case MERGE -> PROCESSING_TRANSFER_MERGE_ICON.copy();
                case PARTIAL_SPLIT -> PROCESSING_TRANSFER_PARTIAL_SPLIT_ICON.copy();
                case FULL_SPLIT -> PROCESSING_TRANSFER_FULL_SPLIT_ICON.copy();
            };
            if (!this.active) {
                icon.opacity(0.5f);
            }
            icon.dest(this.getX(), this.getY()).zOffset(20).blit(guiGraphics);
        }

        @Override
        public List<Component> getTooltipMessage() {
            return switch (this.mode) {
                case MERGE -> List.of(
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.merge"),
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.merge_desc"));
                case PARTIAL_SPLIT -> List.of(
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.partial_split"),
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.partial_split_desc"));
                case FULL_SPLIT -> List.of(
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.full_split"),
                        Component.translatable(
                                "ae2cs.menu.resonant_template_coding_terminal.processing_ingredient_transfer_mode.full_split_desc"));
            };
        }
    }

    private class ResonantProcessingPanel implements ICompositeWidget {
        private final appeng.client.gui.widgets.Scrollbar scrollbar;

        private ResonantProcessingPanel(appeng.client.gui.widgets.Scrollbar scrollbar) {
            this.scrollbar = scrollbar;
            this.scrollbar.setRange(0, getMaxFirstVisibleProcessingInputRow(), 3);
            this.scrollbar.setCaptureMouseWheel(false);
        }

        @Override
        public boolean isVisible() {
            return menu.getMode() == EncodingMode.PROCESSING && !isPulledAnvilModeSelected();
        }

        @Override
        public void setPosition(Point position) {
        }

        @Override
        public void setSize(int width, int height) {
        }

        @Override
        public Rect2i getBounds() {
            return getStyledWidgetBounds(PROCESSING_MODE_PANEL_ID);
        }

        @Override
        public void updateBeforeRender() {
            layoutScrollbarFromStyle();

            int maxScroll = getMaxFirstVisibleProcessingInputRow();
            this.scrollbar.setRange(0, maxScroll, 3);

            int scrollRows = Math.min(this.scrollbar.getCurrentScroll(), maxScroll);
            layoutProcessingSlots(scrollRows);
            setProcessingTooltipAreasVisible(true, scrollRows);
        }

        @Override
        public boolean onMouseWheel(Point mousePos, double delta) {
            return this.scrollbar.onMouseWheel(mousePos, delta);
        }

        private void layoutScrollbarFromStyle() {
            Rect2i bounds = getStyledWidgetBounds(PROCESSING_SCROLLBAR_ID);
            this.scrollbar.setPosition(new Point(bounds.getX(), bounds.getY()));
            this.scrollbar.setSize(bounds.getWidth(), bounds.getHeight());
        }

    }

    private class ResonantStonecuttingPanel implements ICompositeWidget {
        private static final int RECIPE_COLUMNS = 4;
        private static final int VISIBLE_RECIPE_ROWS = 3;

        private final Scrollbar scrollbar;

        private ResonantStonecuttingPanel(Scrollbar scrollbar) {
            this.scrollbar = scrollbar;
            this.scrollbar.setRange(0, 0, 4);
            this.scrollbar.setCaptureMouseWheel(false);
        }

        @Override
        public boolean isVisible() {
            return menu.getMode() == EncodingMode.STONECUTTING && !isPulledAnvilModeSelected();
        }

        @Override
        public void setPosition(Point position) {
        }

        @Override
        public void setSize(int width, int height) {
        }

        @Override
        public Rect2i getBounds() {
            Rect2i panelBounds = getStyledWidgetBounds(STONECUTTING_MODE_PANEL_ID);
            WidgetStyle backgroundStyle = getStyle().getWidget(STONECUTTING_VIRTUAL_MODE_PANEL_BACKGROUND_ID);
            return new Rect2i(panelBounds.getX(), panelBounds.getY(),
                    backgroundStyle.getWidth(), backgroundStyle.getHeight());
        }

        @Override
        public void updateBeforeRender() {
            int totalRows = (menu.getStonecuttingRecipes().size() + RECIPE_COLUMNS - 1) / RECIPE_COLUMNS;
            this.scrollbar.setRange(0, totalRows - VISIBLE_RECIPE_ROWS, VISIBLE_RECIPE_ROWS);
        }

        @Override
        public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
            Rect2i panelBounds = getBounds();
            Point drawPosition = new Point(panelBounds.getX(), panelBounds.getY()).move(bounds.getX(), bounds.getY());
            String imageId = menu.pullProcessingRecipeInputs
                    ? STONECUTTING_PULLED_MODE_PANEL_BACKGROUND_ID
                    : STONECUTTING_VIRTUAL_MODE_PANEL_BACKGROUND_ID;
            getStyle().getImage(imageId)
                    .dest(drawPosition.getX(), drawPosition.getY(), panelBounds.getWidth(), panelBounds.getHeight())
                    .blit(guiGraphics);
            drawRecipes(guiGraphics, bounds, mouse);
        }

        private void drawRecipes(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
            List<RecipeHolder<StonecutterRecipe>> recipes = menu.getStonecuttingRecipes();
            int startIndex = this.scrollbar.getCurrentScroll() * RECIPE_COLUMNS;
            int endIndex = startIndex + RECIPE_COLUMNS * VISIBLE_RECIPE_ROWS;
            ResourceLocation selectedRecipe = menu.getStonecuttingRecipeId();

            for (int i = startIndex; i < endIndex && i < recipes.size(); i++) {
                Rect2i recipeBounds = getRecipeBounds(i - startIndex);
                RecipeHolder<StonecutterRecipe> recipe = recipes.get(i);
                boolean selected = selectedRecipe != null && selectedRecipe.equals(recipe.id());
                Blitter slotBackground = selected ? STONECUTTING_RECIPE_SLOT_SELECTED
                        : mouse.isIn(recipeBounds) ? STONECUTTING_RECIPE_SLOT_HOVER
                        : STONECUTTING_RECIPE_SLOT;
                int renderX = bounds.getX() + recipeBounds.getX();
                int renderY = bounds.getY() + recipeBounds.getY();
                slotBackground.dest(renderX, renderY).blit(guiGraphics);

                ItemStack resultItem = recipe.value().getResultItem(getRegistryAccess());
                int itemYOffset = selected || mouse.isIn(recipeBounds) ? 3 : 2;
                guiGraphics.renderItem(resultItem, renderX + 2, renderY + itemYOffset);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, renderX + 2,
                        renderY + itemYOffset);
            }
        }

        private HolderLookup.Provider getRegistryAccess() {
            Level level = Objects.requireNonNull(Minecraft.getInstance().level);
            return level.registryAccess();
        }

        @Override
        public boolean onMouseDown(Point mousePos, int button) {
            RecipeHolder<StonecutterRecipe> recipe = getRecipeAt(mousePos);
            if (recipe != null) {
                menu.setStonecuttingRecipeId(recipe.id());
                Minecraft.getInstance().getSoundManager().play((SoundInstance) SimpleSoundInstance.forUI(
                        (SoundEvent) SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
                return true;
            }
            return false;
        }

        @Override
        public Tooltip getTooltip(int mouseX, int mouseY) {
            RecipeHolder<StonecutterRecipe> recipe = getRecipeAt(new Point(mouseX, mouseY));
            if (recipe == null) {
                return null;
            }

            ItemStack resultItem = recipe.value().getResultItem(getRegistryAccess());
            return new Tooltip(getTooltipFromContainerItem(resultItem));
        }

        private RecipeHolder<StonecutterRecipe> getRecipeAt(Point point) {
            List<RecipeHolder<StonecutterRecipe>> recipes = menu.getStonecuttingRecipes();
            if (recipes.isEmpty()) {
                return null;
            }

            int startIndex = this.scrollbar.getCurrentScroll() * RECIPE_COLUMNS;
            int endIndex = startIndex + RECIPE_COLUMNS * VISIBLE_RECIPE_ROWS;
            for (int i = startIndex; i < endIndex && i < recipes.size(); i++) {
                if (point.isIn(getRecipeBounds(i - startIndex))) {
                    return recipes.get(i);
                }
            }
            return null;
        }

        private Rect2i getRecipeBounds(int index) {
            Rect2i listBounds = getStyledWidgetBounds(STONECUTTING_RECIPE_LIST_ID);
            int column = index % RECIPE_COLUMNS;
            int row = index / RECIPE_COLUMNS;
            int slotX = listBounds.getX() + column * STONECUTTING_RECIPE_SLOT.getSrcWidth();
            int slotY = listBounds.getY() + row * STONECUTTING_RECIPE_SLOT.getSrcHeight();
            return new Rect2i(slotX, slotY, STONECUTTING_RECIPE_SLOT.getSrcWidth(),
                    STONECUTTING_RECIPE_SLOT.getSrcHeight());
        }

        @Override
        public boolean onMouseWheel(Point mousePos, double delta) {
            return this.scrollbar.onMouseWheel(mousePos, delta);
        }
    }

    private void setProcessingTooltipAreasVisible(boolean visible, int scrollRows) {
        this.widgets.setTooltipAreaEnabled("processing-primary-output", visible && scrollRows == 0);
        this.widgets.setTooltipAreaEnabled("processing-optional-output1", visible && scrollRows > 0);
        this.widgets.setTooltipAreaEnabled("processing-optional-output2", visible);
        this.widgets.setTooltipAreaEnabled("processing-optional-output3", visible);
        this.widgets.setTooltipAreaEnabled("processing-optional-output4", visible);
    }

    private void setPulledProcessingInputSlotsVisible(boolean visible, int scrollRows) {
        Point inputPos = getStyledSlotPosition(SlotSemantics.PROCESSING_INPUTS);
        AppEngSlot[] pulledSlots = this.menu.getPulledProcessingInputSlots();
        for (int i = 0; i < pulledSlots.length; i++) {
            AppEngSlot slot = pulledSlots[i];
            int row = i / PROCESSING_INPUT_COLUMNS;
            int column = i % PROCESSING_INPUT_COLUMNS;
            int visibleRow = row - scrollRows;

            slot.x = inputPos.getX() + column * SLOT_SPACING;
            slot.y = inputPos.getY() + visibleRow * SLOT_SPACING;
            slot.setActive(visible && visibleRow >= 0 && visibleRow < VISIBLE_PROCESSING_INPUT_ROWS);
        }
    }

    private enum PulledDisplayMode {
        FOLLOW_ENCODING_MODE,
        ANVIL
    }
}
