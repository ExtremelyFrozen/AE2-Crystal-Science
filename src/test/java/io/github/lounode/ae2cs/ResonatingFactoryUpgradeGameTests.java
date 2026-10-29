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
        verifyFactory(helper, new ResonatingGrowthFactoryBlockEntity(BlockPos.ZERO,
                AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get().defaultBlockState()));
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

    private static void verifyFactory(GameTestHelper helper, AENetworkedComponentBlockEntity host) {
        host.setLevel(helper.getLevel());
        var upgrades = ((IUpgradeableObject) host).getUpgrades();
        var inventory = inventories(host).getFirst();
        helper.assertTrue(upgrades.size() == 6, "Six dedicated upgrade slots");
        helper.assertTrue(upgrades.getUpgradableItem().asItem() == host.getBlockState().getBlock().asItem(), "Correct machine identity");
        helper.assertTrue(inventory.getSlotLimit(0) == 64, "Base capacity is 64");
        for (int slot = 0; slot < 6; slot++) {
            var card = slot < 4 ? AECSItems.OVERLOAD_CARD : AECSItems.RESONATING_STACKING_CARD;
            var other = slot < 4 ? AECSItems.RESONATING_STACKING_CARD : AECSItems.OVERLOAD_CARD;
            helper.assertTrue(!upgrades.isItemValid(slot, other.toStack()), "Reject card in wrong dedicated slot");
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
        var inputSlots = menu.getSlots(host instanceof ResonatingGrowthFactoryBlockEntity ? SlotSemantics.STORAGE : SlotSemantics.MACHINE_INPUT);
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
            case ResonatingGrowthFactoryBlockEntity growth -> List.of(growth.getInventory());
            case ResonatingCircuitEtcherFactoryBlockEntity etcher -> List.of(etcher.getInputInv(), etcher.getOutputInv());
            default -> throw new IllegalArgumentException("Unexpected factory");
        };
    }
}
