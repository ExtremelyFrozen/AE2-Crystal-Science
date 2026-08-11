package io.github.lounode.ae2cs.common.machine.component;

import io.github.lounode.ae2cs.common.block.entity.PulseCentrifugeBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Objects;

@ForEachTest(idPrefix = "side_config.", groups = "side_config")
final class SideConfigComponentTests {

    private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "does_not_simulate_extracting_input_inventory", enabledByDefault = true)
    static void doesNotSimulateExtractingInputInventory(GameTestHelper helper) {
        PulseCentrifugeBlockEntity machine = placeMachine(helper);
        machine.getInputInv().setItemDirect(0, new ItemStack(Items.STONE));

        IItemHandler handler = itemHandler(helper);
        helper.assertTrue(handler.extractItem(0, 1, true).isEmpty(),
                "Input inventory must not be visible to simulated extraction");
        helper.assertTrue(handler.extractItem(0, 1, false).isEmpty(),
                "Input inventory must not be visible to real extraction");
        helper.assertValueEqual(machine.getInputInv().getStackInSlot(0).getCount(), 1,
                "Input inventory count after rejected extraction");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "does_not_simulate_extracting_insert_only_side", enabledByDefault = true)
    static void doesNotSimulateExtractingInsertOnlySide(GameTestHelper helper) {
        PulseCentrifugeBlockEntity machine = placeMachine(helper);
        machine.getOutputInv().setItemDirect(0, new ItemStack(Items.IRON_INGOT));
        machine.getMachineComponents().getService(SideConfigComponent.class).set(Direction.NORTH, SidePolicy.INSERT);

        IItemHandler handler = itemHandler(helper);
        helper.assertTrue(handler.extractItem(1, 1, true).isEmpty(),
                "Insert-only side must not expose output to simulated extraction");
        helper.assertTrue(handler.extractItem(1, 1, false).isEmpty(),
                "Insert-only side must not expose output to real extraction");
        helper.assertValueEqual(machine.getOutputInv().getStackInSlot(0).getCount(), 1,
                "Output inventory count after rejected extraction");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "extracts_output_when_side_allows_extraction", enabledByDefault = true)
    static void extractsOutputWhenSideAllowsExtraction(GameTestHelper helper) {
        PulseCentrifugeBlockEntity machine = placeMachine(helper);
        machine.getOutputInv().setItemDirect(0, new ItemStack(Items.IRON_INGOT));
        machine.getMachineComponents().getService(SideConfigComponent.class).set(Direction.NORTH, SidePolicy.ALL);

        IItemHandler handler = itemHandler(helper);
        ItemStack simulated = handler.extractItem(1, 1, true);
        helper.assertTrue(simulated.is(Items.IRON_INGOT), "Extracting side must simulate the output item");
        helper.assertValueEqual(simulated.getCount(), 1, "Simulated extraction count");
        helper.assertValueEqual(machine.getOutputInv().getStackInSlot(0).getCount(), 1,
                "Simulated extraction must not mutate output inventory");

        ItemStack extracted = handler.extractItem(1, 1, false);
        helper.assertTrue(extracted.is(Items.IRON_INGOT), "Extracting side must return the output item");
        helper.assertValueEqual(extracted.getCount(), 1, "Real extraction count");
        helper.assertTrue(machine.getOutputInv().getStackInSlot(0).isEmpty(),
                "Real extraction must consume the output item");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "extracts_output_when_side_is_extract", enabledByDefault = true)
    static void extractsOutputWhenSideIsExtract(GameTestHelper helper) {
        PulseCentrifugeBlockEntity machine = placeMachine(helper);
        machine.getOutputInv().setItemDirect(0, new ItemStack(Items.IRON_INGOT));
        machine.getMachineComponents().getService(SideConfigComponent.class).set(Direction.NORTH, SidePolicy.EXTRACT);

        IItemHandler handler = itemHandler(helper);
        helper.assertTrue(handler.extractItem(1, 1, true).is(Items.IRON_INGOT),
                "Extract-only side must simulate the output item");
        helper.assertTrue(handler.extractItem(1, 1, false).is(Items.IRON_INGOT),
                "Extract-only side must return the output item");
        helper.assertTrue(machine.getOutputInv().getStackInSlot(0).isEmpty(),
                "Extract-only side must consume the output item");
        helper.succeed();
    }

    private static PulseCentrifugeBlockEntity placeMachine(GameTestHelper helper) {
        helper.setBlock(MACHINE_POS, AECSBlocks.PULSE_CENTRIFUGE_BLOCK.get());
        return (PulseCentrifugeBlockEntity) helper.getBlockEntity(MACHINE_POS);
    }

    private static IItemHandler itemHandler(GameTestHelper helper) {
        return Objects.requireNonNull(
                helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(MACHINE_POS), Direction.NORTH),
                "Machine must expose an item handler on its north side");
    }
}
