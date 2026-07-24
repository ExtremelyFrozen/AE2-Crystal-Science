package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.CrystalFamilyBlocks;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;
import java.util.Optional;

@ForEachTest(idPrefix = "crystal_growth.", groups = "crystal_growth")
final class CrystalGrowthSequenceTests {

    private static final ResourceLocation SMALL = id("small");
    private static final ResourceLocation MEDIUM = id("medium");
    private static final ResourceLocation LARGE = id("large");
    private static final ResourceLocation CLUSTER = id("cluster");
    private static final ResourceLocation MATURE = id("mature");

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "four_stage_sequence", enabledByDefault = true)
    static void fourStageSequence(GameTestHelper helper) {
        CrystalGrowthSequence sequence = new CrystalGrowthSequence(List.of(SMALL, MEDIUM, LARGE, CLUSTER));

        helper.assertValueEqual(sequence.firstStageId(), SMALL, "first crystal stage");
        helper.assertValueEqual(sequence.nextStageId(SMALL), Optional.of(MEDIUM), "stage after small bud");
        helper.assertValueEqual(sequence.nextStageId(MEDIUM), Optional.of(LARGE), "stage after medium bud");
        helper.assertValueEqual(sequence.nextStageId(LARGE), Optional.of(CLUSTER), "stage after large bud");
        helper.assertValueEqual(sequence.nextStageId(CLUSTER), Optional.empty(), "stage after final cluster");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "five_stage_sequence", enabledByDefault = true)
    static void fiveStageSequence(GameTestHelper helper) {
        CrystalGrowthSequence sequence = new CrystalGrowthSequence(List.of(SMALL, MEDIUM, LARGE, CLUSTER, MATURE));

        helper.assertValueEqual(sequence.nextStageId(CLUSTER), Optional.of(MATURE), "stage after cluster");
        helper.assertValueEqual(sequence.nextStageId(MATURE), Optional.empty(), "stage after mature cluster");
        helper.assertValueEqual(sequence.nextStageId(id("unknown")), Optional.empty(), "unknown stage lookup");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "rejects_invalid_sequence", enabledByDefault = true)
    static void rejectsInvalidSequence(GameTestHelper helper) {
        helper.assertTrue(throwsIllegalArgument(() -> new CrystalGrowthSequence(List.of())),
                "An empty growth sequence must be rejected");
        helper.assertTrue(throwsIllegalArgument(() -> new CrystalGrowthSequence(List.of(SMALL, SMALL))),
                "A growth sequence with duplicate stages must be rejected");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "selects_growth_target", enabledByDefault = true)
    static void selectsGrowthTarget(GameTestHelper helper) {
        CrystalFamilyBlocks fourStageFamily = AECSBlocks.NETHER_QUARTZ_CRYSTALS;
        CrystalMotherRockBlock motherRock = fourStageFamily.motherRock().get();
        ResourceLocation smallStageId = fourStageFamily.motherRock().get().growthSequence().firstStageId();
        ResourceLocation mediumStageId = fourStageFamily.motherRock().get().growthSequence().stageIds().get(1);

        helper.assertValueEqual(
                motherRock.selectNextStageId(Blocks.AIR.defaultBlockState(), Direction.UP),
                Optional.of(smallStageId),
                "air growth target");
        helper.assertValueEqual(
                motherRock.selectNextStageId(Blocks.WATER.defaultBlockState(), Direction.UP),
                Optional.of(smallStageId),
                "water source growth target");
        helper.assertValueEqual(
                motherRock.selectNextStageId(
                        Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1),
                        Direction.UP),
                Optional.empty(),
                "flowing water growth target");

        var upwardSmallBud = fourStageFamily.stages().getFirst().get().defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, Direction.UP);
        helper.assertValueEqual(
                motherRock.selectNextStageId(upwardSmallBud, Direction.UP),
                Optional.of(mediumStageId),
                "matching bud direction");
        helper.assertValueEqual(
                motherRock.selectNextStageId(upwardSmallBud, Direction.NORTH),
                Optional.empty(),
                "mismatched bud direction");
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "registers_confirmed_families", enabledByDefault = true)
    static void registersConfirmedFamilies(GameTestHelper helper) {
        int localStageCount = AECSBlocks.getCrystalFamilies().stream()
                .mapToInt(family -> family.stages().size())
                .sum();

        helper.assertValueEqual(AECSBlocks.getCrystalFamilies().size(), 10, "local crystal family count");
        helper.assertValueEqual(AECSBlocks.getCrystalMotherRocks().size(), 11, "mother rock count");
        helper.assertValueEqual(localStageCount, 45, "local crystal stage count");
        helper.assertValueEqual(
                AECSBlocks.ENDER_QUARTZ_CRYSTALS.motherRock().get().growthSequence()
                        .nextStageId(ResourceLocation.fromNamespaceAndPath("ae2cs", "ender_quartz_crystal_cluster")),
                Optional.of(ResourceLocation.fromNamespaceAndPath("ae2cs", "ender_quartz_mature_crystal_cluster")),
                "five-stage registered family progression");
        helper.succeed();
    }

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("ae2cs_test", path);
    }
}
