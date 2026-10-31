package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.util.GlobalChunkPos;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import appeng.api.networking.IGridNode;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEColor;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ae2cs")
@PrefixGameTestTemplate(false)
public class EnderEmitterAutoLinkGameTests {

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void newlyPlacedMachinesAndPartsConnectWithoutScanning(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), AEBlocks.CREATIVE_ENERGY_CELL.block());
        var earlyPos = new BlockPos(0, 0, 4);
        placeItem(helper, earlyPos, AEBlocks.INTERFACE.stack());
        var emitterPos = new BlockPos(1, 2, 1);
        placeItem(helper, emitterPos, AECSBlocks.ENDER_EMITTER_BLOCK.toStack());
        var emitter = (EnderEmitterBlockEntity) helper.getBlockEntity(emitterPos);
        var firstPos = new BlockPos(3, 1, 1);
        var delayedPos = new BlockPos(3, 1, 3);
        var cablePos = new BlockPos(3, 2, 2);
        var panelPos = new BlockPos(1, 1, 3);

        helper.runAtTickTime(40, () -> {
            helper.assertTrue(emitter.isActive(), "Emitter has a powered physical input");
            placeItem(helper, firstPos, AEBlocks.INTERFACE.stack());
            helper.assertTrue(EnderEmitterBlockEntity.getConnectableNodes(helper.getLevel(), helper.absolutePos(firstPos)).isEmpty(),
                    "The placement event precedes AE2 node initialization");
            helper.assertTrue(emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(firstPos)),
                    "Placement immediately appears in the pending list without an active scan");
            assertLinked(helper, emitter, earlyPos);
        });
        helper.runAtTickTime(80, () -> {
            assertLinked(helper, emitter, firstPos);
            installDelayedHost(helper, delayedPos);
            var absolute = helper.absolutePos(delayedPos);
            EnderEmitterBlockEntity.addPosToRecentEmitter(helper.getLevel(), absolute);
            helper.assertTrue(emitter.getPendingRenderPositionsSnapshot().contains(absolute),
                    "A second placement is discovered independently of the first one");
        });
        helper.runAtTickTime(100, () -> {
            helper.assertTrue(emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(delayedPos)),
                    "A host with delayed nodes survives more than the initial two-tick delay");
            ((DelayedInterfaceBlockEntity) helper.getBlockEntity(delayedPos)).exposeNodes = true;
        });
        helper.runAtTickTime(140, () -> {
            assertLinked(helper, emitter, delayedPos);
            placeItem(helper, cablePos, AEParts.GLASS_CABLE.stack(AEColor.TRANSPARENT));
        });
        helper.runAtTickTime(180, () -> {
            assertLinked(helper, emitter, cablePos);
            var cable = PartHelper.getPartHost(helper.getLevel(), helper.absolutePos(cablePos));
            helper.assertTrue(cable != null && cable.getPart(null) != null, "Pure cable exposes its center part");
            helper.assertTrue(cable.addPart(AEParts.IMPORT_BUS.asItem(), Direction.EAST, null) != null,
                    "A bus can be added to the already connected cable");
            placeItem(helper, panelPos, AEParts.TERMINAL.stack());
        });
        helper.runAtTickTime(220, () -> {
            assertLinked(helper, emitter, panelPos);
            var bus = PartHelper.getPart(helper.getLevel(), helper.absolutePos(cablePos), Direction.EAST);
            helper.assertTrue(bus != null && bus.getGridNode() != null && bus.getGridNode().isActive(),
                    "The attached bus receives a channel through the wirelessly connected cable");
            emitter.getConfigManager().putSetting(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.DISABLE);
            EnderEmitterBlockEntity.removePosFromEmitter(emitter, helper.absolutePos(panelPos));
            EnderEmitterBlockEntity.addPosToRecentEmitter(helper.getLevel(), helper.absolutePos(panelPos));
            helper.assertTrue(!emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(panelPos)),
                    "Automatic discovery respects the disabled cable setting");
            emitter.getConfigManager().putSetting(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.ENABLE);
            emitter.getConfigManager().putSetting(AECSSettings.AUTO_LINK_MODE, AutoLinkMode.DISABLE);
            EnderEmitterBlockEntity.addPosToRecentEmitter(helper.getLevel(), helper.absolutePos(panelPos));
            helper.assertTrue(!emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(panelPos)),
                    "Automatic discovery respects the disabled auto setting");
            EnderEmitterBlockEntity.addAllRecentBEtoEmitter(emitter);
            helper.assertTrue(emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(panelPos)),
                    "An explicit scan still works when automatic discovery is disabled");
        });
        helper.runAtTickTime(260, () -> {
            assertLinked(helper, emitter, panelPos);
            for (var pos : new BlockPos[] { firstPos, delayedPos }) {
                EnderEmitterBlockEntity.removePosFromEmitter(emitter, helper.absolutePos(pos));
                installDelayedHost(helper, pos);
                helper.assertTrue(EnderEmitterBlockEntity.addPosToEmitter(emitter, helper.absolutePos(pos), false, true),
                        "A scan queues the host while its nodes are unavailable");
            }
        });
        helper.runAtTickTime(280, () -> {
            helper.assertTrue(emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(firstPos)),
                    "An unavailable node is retained during its retry window");
            helper.setBlock(delayedPos, Blocks.AIR);
        });
        helper.runAtTickTime(300, () -> helper.assertTrue(!emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(delayedPos)),
                "A removed host is discarded instead of being retried forever"));
        helper.runAtTickTime(380, () -> {
            helper.assertTrue(!emitter.getPendingRenderPositionsSnapshot().contains(helper.absolutePos(firstPos)),
                    "A host that never exposes a node expires after the bounded retry window");
            // Leave no active emitter to claim candidates belonging to other game tests.
            helper.setBlock(emitterPos, Blocks.AIR);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void chunkIndexDoesNotRequireReadyNodes(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var emitter = newEmitter(helper, pos);
        helper.assertTrue(!emitter.getMainNode().isReady(), "No node has been initialized");
        emitter.serverTick();
        var key = new GlobalChunkPos(helper.getLevel().dimension(), pos);
        helper.assertTrue(EnderEmitterBlockEntity.EMITTER_CHUNK_POSITIONS.getOrDefault(key, java.util.Set.of()).contains(pos),
                "Server ticks register the index even before grid readiness");
        emitter.setRemoved();
        helper.assertTrue(!EnderEmitterBlockEntity.EMITTER_CHUNK_POSITIONS.getOrDefault(key, java.util.Set.of()).contains(pos),
                "Removal cleans up the index");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyBlockRangeMigratesToChunkRadius(GameTestHelper helper) {
        var emitter = newEmitter(helper, helper.absolutePos(new BlockPos(1, 1, 1)));
        emitter.getUpgrades().insertItem(0, AEItems.WIRELESS_BOOSTER.stack(64), false);
        var saved = new CompoundTag();
        emitter.saveAdditional(saved, helper.getLevel().registryAccess());
        saved.remove("chunk_radius");
        saved.putInt("link_distance", 33);
        var restored = newEmitter(helper, emitter.getBlockPos());
        restored.loadTag(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getLinkDistance() == 3, "33 legacy blocks round up to three chunks with boosters");
        saved.putInt("link_distance", Integer.MAX_VALUE);
        restored.loadTag(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getLinkDistance() == 3, "Legacy conversion cannot overflow or exceed the upgrade limit");
        saved.putInt("chunk_radius", 0);
        restored.loadTag(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getLinkDistance() == 0, "The new field takes precedence, including radius zero");
        var roundTrip = new CompoundTag();
        restored.saveAdditional(roundTrip, helper.getLevel().registryAccess());
        helper.assertTrue(roundTrip.getInt("chunk_radius") == 0 && !roundTrip.contains("link_distance"),
                "Subsequent saves use the current range format");
        emitter.setRemoved();
        restored.setRemoved();
        helper.succeed();
    }

    private static EnderEmitterBlockEntity newEmitter(GameTestHelper helper, BlockPos pos) {
        var state = AECSBlocks.ENDER_EMITTER_BLOCK.get().defaultBlockState();
        var emitter = (EnderEmitterBlockEntity) AECSBlocks.ENDER_EMITTER_BLOCK.get().newBlockEntity(pos, state);
        emitter.setLevel(helper.getLevel());
        return emitter;
    }

    private static void installDelayedHost(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, AEBlocks.INTERFACE.block());
        var absolute = helper.absolutePos(pos);
        helper.getLevel().removeBlockEntity(absolute);
        helper.getLevel().setBlockEntity(new DelayedInterfaceBlockEntity(absolute));
        helper.getLevel().invalidateCapabilities(absolute);
    }

    private static void placeItem(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        // Keep the fake player's collision box away from cable parts during placement.
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 3, 0))));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var absolute = helper.absolutePos(pos);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        helper.assertTrue(stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "Item placement succeeds through the real NeoForge placement event");
    }

    private static void assertLinked(GameTestHelper helper, EnderEmitterBlockEntity emitter, BlockPos pos) {
        helper.assertTrue(emitter.getLinkedRenderPositionsSnapshot().contains(helper.absolutePos(pos)),
                "Automatically linked target at " + pos);
        helper.assertTrue(EnderEmitterBlockEntity.getConnectableNodes(helper.getLevel(), helper.absolutePos(pos)).stream()
                .anyMatch(IGridNode::isActive), "The target has a working AE2 network connection");
    }

    private static class DelayedInterfaceBlockEntity extends InterfaceBlockEntity {

        private boolean exposeNodes;

        private DelayedInterfaceBlockEntity(BlockPos pos) {
            super(AEBlockEntities.INTERFACE.get(), pos, AEBlocks.INTERFACE.block().defaultBlockState());
        }

        @Override
        public IGridNode getGridNode(Direction side) {
            return exposeNodes ? super.getGridNode(side) : null;
        }
    }
}
