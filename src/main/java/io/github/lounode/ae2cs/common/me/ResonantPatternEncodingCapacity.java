package io.github.lounode.ae2cs.common.me;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;

import java.lang.reflect.Field;

/**
 * Expands AE2's pattern encoding inventories for the resonant terminal.
 */
public final class ResonantPatternEncodingCapacity {

    public static final int PROCESSING_INPUT_SLOTS = 144;
    public static final int PROCESSING_OUTPUT_SLOTS = 36;

    private static final Field ENCODED_INPUT_INV_FIELD = getField("encodedInputInv");
    private static final Field ENCODED_OUTPUT_INV_FIELD = getField("encodedOutputInv");

    private ResonantPatternEncodingCapacity() {}

    public static void expand(PatternEncodingLogic logic) {
        if (logic.getEncodedInputInv().size() == PROCESSING_INPUT_SLOTS && logic.getEncodedOutputInv().size() == PROCESSING_OUTPUT_SLOTS) {
            return;
        }

        ConfigInventory inputs = ConfigInventory.configStacks(PROCESSING_INPUT_SLOTS)
                .changeListener(() -> onEncodedInputChanged(logic))
                .allowOverstacking(true)
                .build();
        ConfigInventory outputs = ConfigInventory.configStacks(PROCESSING_OUTPUT_SLOTS)
                .changeListener(logic::saveChanges)
                .allowOverstacking(true)
                .build();

        try {
            ENCODED_INPUT_INV_FIELD.set(logic, inputs);
            ENCODED_OUTPUT_INV_FIELD.set(logic, outputs);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to expand resonant pattern encoding inventories", e);
        }
    }

    private static void onEncodedInputChanged(PatternEncodingLogic logic) {
        if (logic.getMode() != EncodingMode.PROCESSING) {
            ConfigInventory inventory = logic.getEncodedInputInv();
            inventory.beginBatch();
            try {
                for (int slot = 0; slot < inventory.size(); slot++) {
                    GenericStack stack = inventory.getStack(slot);
                    if (stack == null) {
                        continue;
                    }
                    if (!AEItemKey.is(stack.what())) {
                        inventory.setStack(slot, null);
                    } else if (stack.amount() != 1) {
                        inventory.setStack(slot, new GenericStack(stack.what(), 1));
                    }
                }
            } finally {
                inventory.endBatch();
            }
        }
        logic.saveChanges();
    }

    private static Field getField(String name) {
        try {
            Field field = PatternEncodingLogic.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
