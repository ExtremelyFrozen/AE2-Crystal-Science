package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.recipe.pulse_centrifuge.PulseCentrifugeRecipe;

import appeng.api.config.Actionable;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;

@ForEachTest(idPrefix = "pulse_centrifuge.", groups = "pulse_centrifuge")
final class PulseCentrifugeTests {

    private static final SizedIngredient INPUT = new SizedIngredient(Ingredient.of(Items.STONE), 1);

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "validates_result_count", enabledByDefault = true)
    static void validatesResultCount(GameTestHelper helper) {
        helper.assertTrue(throwsIllegalArgument(() -> new PulseCentrifugeRecipe(INPUT, List.of(), 200)),
                "A recipe without results must be rejected");
        helper.assertTrue(throwsIllegalArgument(() -> new PulseCentrifugeRecipe(INPUT, List.of(
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD)), 200)),
                "A recipe with five results must be rejected");
        helper.assertTrue(throwsIllegalArgument(() -> new PulseCentrifugeRecipe(
                INPUT, List.of(ItemStack.EMPTY), 200)),
                "An empty result must be rejected");
        helper.assertTrue(throwsIllegalArgument(() -> new PulseCentrifugeRecipe(
                INPUT, List.of(new ItemStack(Items.IRON_INGOT)), 0)),
                "A non-positive energy cost must be rejected");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "plans_merged_outputs_without_mutation", enabledByDefault = true)
    static void plansMergedOutputsWithoutMutation(GameTestHelper helper) {
        AppEngInternalInventory output = new AppEngInternalInventory(4);
        output.setItemDirect(0, new ItemStack(Items.IRON_INGOT, 60));

        List<ItemStack> plan = PulseCentrifugeBlockEntity.planOutputInsertion(output, List.of(
                new ItemStack(Items.IRON_INGOT, 2),
                new ItemStack(Items.IRON_INGOT, 3)));

        helper.assertTrue(plan != null, "Outputs that fit must produce a plan");
        helper.assertValueEqual(plan.get(0).getCount(), 64, "Merged first output slot count");
        helper.assertValueEqual(plan.get(1).getCount(), 1, "Overflow second output slot count");
        helper.assertValueEqual(output.getStackInSlot(0).getCount(), 60, "Planning must not mutate real output");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "rejects_combined_output_overflow_atomically", enabledByDefault = true)
    static void rejectsCombinedOutputOverflowAtomically(GameTestHelper helper) {
        AppEngInternalInventory output = new AppEngInternalInventory(4);
        output.setItemDirect(0, new ItemStack(Items.DIAMOND, 64));
        output.setItemDirect(1, new ItemStack(Items.EMERALD, 64));
        output.setItemDirect(2, new ItemStack(Items.COPPER_INGOT, 64));

        List<ItemStack> plan = PulseCentrifugeBlockEntity.planOutputInsertion(output, List.of(
                new ItemStack(Items.IRON_INGOT, 40),
                new ItemStack(Items.GOLD_INGOT, 40)));

        helper.assertTrue(plan == null, "Results that only fit individually must reject the complete batch");
        helper.assertTrue(output.getStackInSlot(3).isEmpty(), "Rejected planning must not insert partial output");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "stops_before_processing_when_outputs_are_full", enabledByDefault = true)
    static void stopsBeforeProcessingWhenOutputsAreFull(GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(1, 1, 1);
        helper.setBlock(machinePos, AECSBlocks.PULSE_CENTRIFUGE_BLOCK.get());
        PulseCentrifugeBlockEntity centrifuge = (PulseCentrifugeBlockEntity) helper.getBlockEntity(machinePos);

        centrifuge.injectAEPower(400, Actionable.MODULATE);
        centrifuge.getInputInv().insertItem(0, new ItemStack(Items.STONE), false);
        for (int slot = 0; slot < centrifuge.getOutputInv().size(); slot++) {
            centrifuge.getOutputInv().setItemDirect(slot, new ItemStack(Items.DIAMOND, 64));
        }

        helper.runAfterDelay(5, () -> {
            helper.assertValueEqual(centrifuge.getRecipeProgress(), 0, "Blocked recipe progress");
            helper.assertValueEqual((int) centrifuge.getAECurrentPower(), 400, "Blocked recipe stored energy");
            helper.assertTrue(!centrifuge.isProcessing(), "Blocked recipe must not be processing");
            helper.assertValueEqual(centrifuge.getInputInv().getStackInSlot(0).getCount(), 1,
                    "Blocked recipe input count");

            centrifuge.getOutputInv().setItemDirect(2, ItemStack.EMPTY);
            centrifuge.getOutputInv().setItemDirect(3, ItemStack.EMPTY);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(centrifuge.getInputInv().getStackInSlot(0).isEmpty(),
                        "Input must be consumed after all outputs fit");
                helper.assertValueEqual(countItem(centrifuge.getOutputInv(), Items.IRON_INGOT), 1,
                        "Primary output count");
                helper.assertValueEqual(countItem(centrifuge.getOutputInv(), Items.GOLD_INGOT), 2,
                        "Secondary output count");
                helper.succeed();
            });
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "separates_fluid_input_and_output", enabledByDefault = true)
    static void separatesFluidInputAndOutput(GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(1, 1, 1);
        helper.setBlock(machinePos, AECSBlocks.PULSE_CENTRIFUGE_BLOCK.get());
        PulseCentrifugeBlockEntity centrifuge = (PulseCentrifugeBlockEntity) helper.getBlockEntity(machinePos);

        int filled = centrifuge.getFluidTanks().fill(new FluidStack(Fluids.WATER, 20000),
                IFluidHandler.FluidAction.EXECUTE);

        helper.assertValueEqual(filled, 16000, "Accepted input fluid amount");
        helper.assertValueEqual(centrifuge.getFluidTanks().input().getFluidAmount(), 16000, "Left tank amount");
        helper.assertTrue(centrifuge.getFluidTanks().output().isEmpty(),
                "Input must not overflow into the output tank");

        centrifuge.getFluidTanks().output().setFluid(new FluidStack(Fluids.WATER, 4000));
        FluidStack drained = centrifuge.getFluidTanks().drain(4000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(drained.getAmount(), 4000, "Extracted output fluid amount");
        helper.assertValueEqual(centrifuge.getFluidTanks().input().getFluidAmount(), 16000,
                "Extracting output must not drain the input tank");
        helper.succeed();
    }

    private static int countItem(AppEngInternalInventory inventory, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }
}
