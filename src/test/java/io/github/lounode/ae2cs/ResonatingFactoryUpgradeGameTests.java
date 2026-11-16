package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import io.github.lounode.ae2cs.common.menu.*;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("ae2cs")
@PrefixGameTestTemplate(false)
public class ResonatingFactoryUpgradeGameTests {

    @GameTest(template = "empty")
    public static void aggregator(GameTestHelper helper) {
        verifyFactory(helper, new ResonatingAggregatorFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_AGGREGATOR_FACTORY_BLOCK.get().defaultBlockState()));
    }

    @GameTest(template = "empty")
    public static void pulverizer(GameTestHelper helper) {
        verifyFactory(helper, new ResonatingPulverizerFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_PULVERIZER_FACTORY_BLOCK.get().defaultBlockState()));
    }

    @GameTest(template = "empty")
    public static void growth(GameTestHelper helper) {
        var host = new ResonatingGrowthFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get().defaultBlockState());
        host.setLevel(helper.getLevel());
        var input = host.getInputInv();
        var upgrades = host.getUpgrades();
        var rock = new ItemStack(Items.BUDDING_AMETHYST, 64);
        helper.assertTrue(input.insertItem(0, rock, false).getCount() == 63, "One rock fits without stacking cards");
        helper.assertTrue(input.insertItem(1, new ItemStack(Items.COBBLESTONE), false).is(Items.COBBLESTONE),
                "Non-mother-rock items cannot be inserted");
        var card = AECSItems.RESONATING_STACKING_CARD.toStack();
        helper.assertTrue(appeng.api.upgrades.Upgrades.isUpgradeCardItem(card), "AE2 recognizes the stacking card");
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        var menu = new ResonatingGrowthFactoryMenu(1, player.getInventory(), host);
        var cardSlot = menu.getSlots(SlotSemantics.UPGRADE).getFirst();
        menu.setCarried(card.copy());
        menu.clicked(cardSlot.index, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        helper.assertTrue(upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD) == 1,
                "Clicking the top slot installs the first stacking card");
        helper.assertTrue(input.insertItem(0, rock, false).getCount() == 33, "One stacking card allows 32 rocks");
        var playerSlot = menu.getSlots(SlotSemantics.PLAYER_INVENTORY).getFirst();
        ItemStack previous = playerSlot.getItem();
        try {
            playerSlot.set(card.copy());
            menu.quickMoveStack(player, playerSlot.index);
            helper.assertTrue(upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD) == 2,
                    "Shift-click installs the second stacking card");
        } finally {
            playerSlot.set(previous);
        }
        helper.assertTrue(input.insertItem(0, rock, false).getCount() == 32, "Two stacking cards allow 64 rocks");
        menu.setCarried(card.copy());
        menu.clicked(menu.getSlots(SlotSemantics.UPGRADE).get(2).index, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        helper.assertTrue(ItemStack.matches(menu.getCarried(), card), "A third stacking card stays on the cursor");
        helper.assertTrue(upgrades.extractItem(0, 1, false).isEmpty(), "A full stack protects a card in the top slot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthFactoryProducesSelectedStageInPairedOutput(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get());
        var host = (ResonatingGrowthFactoryBlockEntity) helper.getBlockEntity(pos);
        host.getInputInv().insertItem(9, new ItemStack(Items.BUDDING_AMETHYST), false);
        for (int stage = 0; stage < 3; stage++) host.cycleTargetStage();
        for (int slot = 0; slot < 4; slot++) {
            host.getUpgrades().insertItem(slot, AECSItems.OVERLOAD_CARD.toStack(), false);
        }
        host.injectAEPower(100, appeng.api.config.Actionable.MODULATE);
        for (int tick = 0; tick < 3; tick++) host.serverTick();
        helper.assertTrue(host.getOutputInv().isEmpty(), "Full overclock still takes four ticks");
        host.serverTick();
        ItemStack output = host.getOutputInv().getStackInSlot(9);
        helper.assertTrue(output.is(Items.SMALL_AMETHYST_BUD) && output.getCount() == 6,
                "The selected stage and full-overclock yield go to the paired output slot");
        helper.assertTrue(host.getInputInv().getStackInSlot(9).is(Items.BUDDING_AMETHYST),
                "A mother rock without decay is retained");
        helper.assertTrue(host.getAECurrentPower() == 0, "The completed growth consumes energy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthFactoryProducesDustAndCrystalDirectly(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get());
        var host = (ResonatingGrowthFactoryBlockEntity) helper.getBlockEntity(pos);
        host.getInputInv().insertItem(0, AEBlocks.FLAWLESS_BUDDING_QUARTZ.stack(), false);
        host.getInputInv().insertItem(1, new ItemStack(Items.BUDDING_AMETHYST), false);
        for (int slot = 0; slot < 4; slot++) {
            host.getUpgrades().insertItem(slot, AECSItems.OVERLOAD_CARD.toStack(), false);
        }
        host.cycleTargetStage();
        host.injectAEPower(300, appeng.api.config.Actionable.MODULATE);
        for (int tick = 0; tick < 4; tick++) host.serverTick();
        helper.assertTrue(ItemStack.matches(host.getOutputInv().getStackInSlot(0), AEItems.CERTUS_QUARTZ_DUST.stack(6)),
                "Dust target produces the mapped quartz dust");
        helper.assertTrue(host.getOutputInv().getStackInSlot(1).isEmpty() && host.getAECurrentPower() == 200,
                "Amethyst has no dust mapping and consumes no energy in dust mode");
        host.getOutputInv().clear();
        host.cycleTargetStage();
        var saved = new CompoundTag();
        var registries = helper.getLevel().registryAccess();
        host.saveAdditional(saved, registries);
        host.cycleTargetStage();
        host.loadTag(saved, registries);
        for (int tick = 0; tick < 4; tick++) host.serverTick();
        helper.assertTrue(ItemStack.matches(host.getOutputInv().getStackInSlot(0), AEItems.CERTUS_QUARTZ_CRYSTAL.stack(6)),
                "The saved crystal target produces quartz crystals");
        helper.assertTrue(ItemStack.matches(host.getOutputInv().getStackInSlot(1), new ItemStack(Items.AMETHYST_SHARD, 6)),
                "Amethyst crystal target produces amethyst shards");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthFactoryReturnsSavedDecayToMotherRockSlots(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get());
        var host = (ResonatingGrowthFactoryBlockEntity) helper.getBlockEntity(pos);
        var quartz = AEBlocks.QUARTZ_BLOCK.stack();
        var damaged = AEBlocks.DAMAGED_BUDDING_QUARTZ.stack();
        host.getInputInv().setItemDirect(0, damaged);
        var saved = new CompoundTag();
        var registries = helper.getLevel().registryAccess();
        host.saveAdditional(saved, registries);
        saved.remove("pending_decay");
        var oldDecay = new io.github.lounode.ae2cs.common.inventory.ResonatingStackingInventory(24, () -> 64, () -> {});
        oldDecay.setItemDirect(0, quartz.copy());
        oldDecay.setItemDirect(9, quartz.copy());
        oldDecay.writeToNBT(saved, "decay_output", registries);
        host.loadTag(saved, registries);
        host.serverTick();
        helper.assertTrue(host.getOutputInv().isEmpty(), "Decay never enters the crystal output");
        helper.assertTrue(ItemStack.matches(host.getInputInv().getStackInSlot(0), damaged), "Remaining mother rock is retained");
        helper.assertTrue(ItemStack.matches(host.getInputInv().getStackInSlot(1), quartz), "Partial decay uses another mother rock slot");
        helper.assertTrue(ItemStack.matches(host.getInputInv().getStackInSlot(9), quartz), "Decay prefers its original mother rock slot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void circuitEtcher(GameTestHelper helper) {
        verifyFactory(helper, new ResonatingCircuitEtcherFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_CIRCUIT_ETCHER_FACTORY_BLOCK.get().defaultBlockState()));
    }

    @GameTest(template = "empty")
    public static void ordinaryEtcherRetainsItsUpgradeRules(GameTestHelper helper) {
        var host = new CircuitEtcherBlockEntity(BlockPos.ZERO, AECSBlocks.CIRCUIT_ETCHER_BLOCK.get().defaultBlockState());
        host.setLevel(helper.getLevel());
        var upgrades = host.getUpgrades();
        helper.assertTrue(upgrades.size() == 4 && upgrades.getMaxInstalled(AEItems.SPEED_CARD) == 4,
                "Ordinary etcher keeps four generic upgrade slots");
        helper.assertTrue(upgrades.getMaxInstalled(AECSItems.OVERLOAD_CARD) == 2,
                "Ordinary etcher keeps its two-card meteorite limit");
        helper.assertTrue(upgrades.getMaxInstalled(AECSItems.RESONATING_STACKING_CARD) == 0,
                "Factory stacking cards do not alter ordinary etcher");
        var menu = new CircuitEtcherMenu(1, FakePlayerFactory.getMinecraft(helper.getLevel()).getInventory(), host);
        helper.assertTrue(menu.getSlots(SlotSemantics.MACHINE_INPUT).size() == 3 && menu.getSlots(SlotSemantics.MACHINE_OUTPUT).size() == 1, "Ordinary etcher retains its inventory layout");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthFactorySideConfiguration(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get());
        var host = (ResonatingGrowthFactoryBlockEntity) helper.getBlockEntity(pos);
        var config = host.getMachineComponents().getService(SideConfigComponent.class);
        var inventory = FakePlayerFactory.getMinecraft(helper.getLevel()).getInventory();
        var menu = new SideConfigMenu(AECSMenus.SIDE_CONFIG_MENU.get(), 1, inventory, host);
        menu.setReturnToMenuType(AECSMenus.RESONATING_GROWTH_FACTORY_MENU.get());
        helper.assertTrue(menu.getHost() == host && menu.getReturnToMenuType() == AECSMenus.RESONATING_GROWTH_FACTORY_MENU.get(),
                "Side configuration opens for growth factory and retains its return menu");
        helper.assertTrue(host.getMainMenuIcon().is(AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.asItem()),
                "Side configuration uses the factory's return icon");
        var handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), Direction.NORTH);
        helper.assertTrue(handler != null && handler.getSlots() == 48, "Factory exposes its real inventory to sided automation");
        config.set(Direction.NORTH, SidePolicy.NONE);
        helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), Direction.NORTH) == null,
                "Disabling a side disables its inventory capability");
        config.setAutoImport(true);
        config.setAutoExport(true);
        var saved = new CompoundTag();
        host.saveAdditional(saved, helper.getLevel().registryAccess());
        var restored = new ResonatingGrowthFactoryBlockEntity(BlockPos.ZERO, host.getBlockState());
        restored.setLevel(helper.getLevel());
        restored.loadTag(saved, helper.getLevel().registryAccess());
        var restoredConfig = restored.getMachineComponents().getService(SideConfigComponent.class);
        helper.assertTrue(restoredConfig.get(Direction.NORTH) == SidePolicy.NONE && restoredConfig.isAutoImport() && restoredConfig.isAutoExport(),
                "Growth factory preserves side policies and automatic-transfer settings when reloaded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pulverizerAverageMixedInput(GameTestHelper helper) {
        verifyAverageInput(helper, 0, new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.DIRT, 64));
        verifyAverageInput(helper, 0, new ItemStack(Items.ENDER_PEARL, 16), new ItemStack(Items.IRON_SWORD));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pulverizerAverageStackingInput(GameTestHelper helper) {
        ItemStack[] stacks = new ItemStack[9];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new ItemStack(i % 2 == 0 ? Items.COBBLESTONE : Items.DIRT, 1024);
        }
        verifyAverageInput(helper, 2, stacks);
        verifyAverageInput(helper, 2, new ItemStack(Items.COBBLESTONE, 1024));
        helper.succeed();
    }

    private static void verifyAverageInput(GameTestHelper helper, int stackingCards, ItemStack... stacks) {
        var host = new ResonatingPulverizerFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_PULVERIZER_FACTORY_BLOCK.get().defaultBlockState());
        host.setLevel(helper.getLevel());
        for (int i = 0; i < stackingCards; i++) {
            host.getUpgrades().insertItem(4 + i, AECSItems.RESONATING_STACKING_CARD.toStack(), false);
        }
        var input = host.getInputInv();
        for (int i = 0; i < stacks.length; i++) input.setItemDirect(i, stacks[i].copy());
        host.setAverageInputEnabled(true);
        for (ItemStack original : stacks) {
            int expected = 0;
            for (ItemStack stack : stacks) {
                if (ItemStack.isSameItemSameComponents(original, stack)) expected += stack.getCount();
            }
            int actual = 0;
            int min = Integer.MAX_VALUE;
            int max = 0;
            for (ItemStack stack : input) {
                if (!ItemStack.isSameItemSameComponents(original, stack)) continue;
                actual += stack.getCount();
                min = Math.min(min, stack.getCount());
                max = Math.max(max, stack.getCount());
                int limit = stack.isStackable() ? stack.getMaxStackSize() * host.getStackCapacity() / 64 : 1;
                helper.assertTrue(stack.getCount() <= limit, "Balanced stacks respect upgraded capacity");
            }
            helper.assertTrue(actual == expected, "Averaging preserves every item and its components");
            helper.assertTrue(max - min <= 1, "Each item type is evenly distributed");
        }
        List<ItemStack> snapshot = new ArrayList<>();
        for (ItemStack stack : input) snapshot.add(stack.copy());
        host.setAverageInputEnabled(false);
        host.setAverageInputEnabled(true);
        for (int i = 0; i < input.size(); i++) {
            helper.assertTrue(ItemStack.matches(snapshot.get(i), input.getStackInSlot(i)), "Repeated averaging is stable");
        }
    }

    private static void verifyFactory(GameTestHelper helper, AENetworkedComponentBlockEntity host) {
        host.setLevel(helper.getLevel());
        var upgrades = ((IUpgradeableObject) host).getUpgrades();
        var inventory = inventories(host).getFirst();
        helper.assertTrue(upgrades.size() == 6, "Six upgrade slots");
        helper.assertTrue(upgrades.getUpgradableItem().asItem() == host.getBlockState().getBlock().asItem(), "Correct machine identity");
        helper.assertTrue(inventory.getSlotLimit(0) == 64, "Base capacity is 64");
        for (int slot = 0; slot < 6; slot++) {
            var card = slot < 4 ? AECSItems.OVERLOAD_CARD : AECSItems.RESONATING_STACKING_CARD;
            if (slot == 4) {
                helper.assertTrue(!upgrades.isItemValid(slot, AECSItems.OVERLOAD_CARD.toStack()), "Reject a fifth meteorite card");
            }
            helper.assertTrue(!upgrades.isItemValid(slot, AEItems.SPEED_CARD.stack()), "Reject ordinary speed card");
            helper.assertTrue(upgrades.insertItem(slot, card.toStack(2), false).getCount() == 1, "One card per slot");
            if (slot == 4) helper.assertTrue(inventory.getSlotLimit(0) == 256, "One stacking card gives capacity 256");
        }
        helper.assertTrue(upgrades.getInstalledUpgrades(AECSItems.OVERLOAD_CARD) == 4, "All four meteorite cards installed");
        helper.assertTrue(upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD) == 2, "Both stacking cards installed");
        helper.assertTrue(inventory.getSlotLimit(0) == 1024, "Two stacking cards give capacity 1024");
        for (var inv : inventories(host)) {
            helper.assertTrue(inv.insertItem(0, new ItemStack(Items.COBBLESTONE, 1025), false).getCount() == 1,
                    "Automation inserts exactly 1024 items");
        }
        helper.assertTrue(upgrades.extractItem(5, 1, true).isEmpty(), "Simulation blocks unsafe stacking-card removal");
        helper.assertTrue(upgrades.extractItem(5, 1, false).isEmpty(), "Unsafe stacking-card removal is blocked");

        var playerInventory = FakePlayerFactory.getMinecraft(helper.getLevel()).getInventory();
        UpgradeableMenu<?> menu = switch (host) {
            case ResonatingAggregatorFactoryBlockEntity aggregator -> new ResonatingAggregatorFactoryMenu(1, playerInventory, aggregator);
            case ResonatingPulverizerFactoryBlockEntity pulverizer -> new ResonatingPulverizerFactoryMenu(1, playerInventory, pulverizer);
            case ResonatingGrowthFactoryBlockEntity growth -> new ResonatingGrowthFactoryMenu(1, playerInventory, growth);
            case ResonatingCircuitEtcherFactoryBlockEntity etcher -> new CircuitEtcherMenu(
                    AECSMenus.RESONATING_CIRCUIT_ETCHER_FACTORY_MENU.get(), 1, playerInventory, etcher);
            default -> throw new IllegalArgumentException("Unexpected factory");
        };
        helper.assertTrue(menu.getSlots(SlotSemantics.UPGRADE).size() == 6, "Menu exposes all six upgrade slots");
        var inputSlots = menu.getSlots(SlotSemantics.MACHINE_INPUT);
        helper.assertTrue(inputSlots.getFirst().getMaxStackSize(new ItemStack(Items.COBBLESTONE)) == 1024,
                "Menu insertion uses the enlarged capacity");
        if (host instanceof ResonatingCircuitEtcherFactoryBlockEntity) {
            helper.assertTrue(inputSlots.size() == 9 && menu.getSlots(SlotSemantics.MACHINE_OUTPUT).size() == 3,
                    "Etcher retains nine inputs and three outputs");
        }

        CompoundTag saved = new CompoundTag();
        host.saveAdditional(saved, helper.getLevel().registryAccess());
        var restored = (AENetworkedComponentBlockEntity) host.getType().create(BlockPos.ZERO, host.getBlockState());
        restored.setLevel(helper.getLevel());
        restored.loadTag(saved, helper.getLevel().registryAccess());
        for (var inv : inventories(restored)) {
            helper.assertTrue(inv.getStackInSlot(0).getCount() == 1024, "Save and reload preserve oversized stacks");
        }
        helper.assertTrue(((IUpgradeableObject) restored).getUpgrades().getInstalledUpgrades(AECSItems.OVERLOAD_CARD) == 4,
                "Save and reload preserve all meteorite cards");
        List<ItemStack> drops = new ArrayList<>();
        restored.addAdditionalDrops(helper.getLevel(), BlockPos.ZERO, drops);
        helper.assertTrue(drops.stream().filter(stack -> stack.is(Items.COBBLESTONE)).mapToInt(ItemStack::getCount).sum() == inventories(host).size() * 1024, "Breaking preserves every stored item");
        helper.assertTrue(drops.stream().allMatch(stack -> stack.getCount() <= stack.getMaxStackSize()), "Drops use legal vanilla stack sizes");

        for (var inv : inventories(host)) inv.setItemDirect(0, new ItemStack(Items.COBBLESTONE, 256));
        helper.assertTrue(!upgrades.extractItem(5, 1, false).isEmpty(), "Safe stacking-card removal succeeds");
        helper.assertTrue(inventory.getSlotLimit(0) == 256, "Capacity drops to 256");
        helper.assertTrue(upgrades.extractItem(4, 1, false).isEmpty(), "Last stacking card protects 256-item stack");
        for (var inv : inventories(host)) inv.clear();
        helper.assertTrue(!upgrades.extractItem(4, 1, false).isEmpty(), "Last card removable after emptying inventory");
        helper.assertTrue(inventory.getSlotLimit(0) == 64, "Capacity returns to 64");
        host.clearContent();
        helper.assertTrue(upgrades.isEmpty(), "Clearing content clears upgrades");
        helper.succeed();
    }

    private static List<? extends AppEngInternalInventory> inventories(AENetworkedComponentBlockEntity host) {
        return switch (host) {
            case ResonatingAggregatorFactoryBlockEntity aggregator -> List.of(aggregator.getInputInv(), aggregator.getOutputInv());
            case ResonatingPulverizerFactoryBlockEntity pulverizer -> List.of(pulverizer.getInputInv(), pulverizer.getOutputInv());
            case ResonatingCircuitEtcherFactoryBlockEntity etcher -> List.of(etcher.getInputInv(), etcher.getOutputInv());
            default -> throw new IllegalArgumentException("Unexpected factory");
        };
    }
}
