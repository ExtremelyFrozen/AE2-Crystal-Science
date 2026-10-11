package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class AECSCreativeModeTabs {

    private static final long CRYSTAL_TAB_ICON_CYCLE_MS = 1000L;

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AECSConstants.MODID);

    public static final Supplier<CreativeModeTab> AE2CS_CREATIVE_TAB = CREATIVE_MODE_TAB.register(
            "ae2cs_creative_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AECSItems.PURE_RESONATING_CRYSTAL.get()))
                    .title(Component.translatable("creativetab.ae2cs.items"))
                    .displayItems((params, output) -> {
                        for (DeferredItem<? extends Item> ro : AECSItems.getALL()) {
                            output.accept(ro.get());
                        }
                        for (DeferredItem<? extends Item> ro : AECSParts.getAll()) {
                            output.accept(ro.get());
                        }
                        Set<DeferredBlock<? extends Block>> crystalBlocks = new HashSet<>(AECSBlocks.getCrystalGrowthBlocks());
                        for (DeferredBlock<? extends Block> ro : AECSBlocks.getALL()) {
                            if (!crystalBlocks.contains(ro)) {
                                output.accept(ro.get());
                            }
                        }
                    })
                    .build());

    public static final Supplier<CreativeModeTab> AE2CS_CRYSTAL_TAB = CREATIVE_MODE_TAB.register(
            "ae2cs_crystal_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AECSBlocks.NETHER_QUARTZ_CRYSTALS.motherRock().get()))
                    .title(Component.translatable("creativetab.ae2cs.crystals"))
                    .withTabFactory(CyclingCreativeModeTab::new)
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AECSConstants.MODID, "ae2cs_creative_tab"))
                    .displayItems((params, output) -> {
                        for (DeferredBlock<? extends Block> block : AECSBlocks.getCrystalGrowthBlocks()) {
                            output.accept(block.get());
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

    /**
     * Vanilla caches the first icon stack, so this tab ignores that cache and walks its own contents.
     */
    private static final class CyclingCreativeModeTab extends CreativeModeTab {

        private int shownIndex = -1;
        private ItemStack shownIcon = ItemStack.EMPTY;

        private CyclingCreativeModeTab(Builder builder) {
            super(builder);
        }

        @Override
        public ItemStack getIconItem() {
            Collection<ItemStack> items = this.getDisplayItems();
            if (items.isEmpty()) {
                return super.getIconItem();
            }

            int index = (int) ((System.currentTimeMillis() / CRYSTAL_TAB_ICON_CYCLE_MS) % items.size());
            if (index != this.shownIndex || this.shownIcon.isEmpty()) {
                int cursor = 0;
                for (ItemStack stack : items) {
                    if (cursor++ == index) {
                        this.shownIndex = index;
                        this.shownIcon = stack.copy();
                        break;
                    }
                }
            }
            return this.shownIcon.isEmpty() ? super.getIconItem() : this.shownIcon;
        }
    }
}
