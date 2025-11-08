package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.common.block.AE2CrystalSeedsBlocks;
import io.github.lounode.ae2cs.common.block.entity.AE2CrystalSeedsBlockEntities;
import io.github.lounode.ae2cs.common.item.AE2CrystalSeedsItems;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.github.lounode.ae2cs.AE2CSCreativeTabs.AE2_CRYSTAL_SEEDS_TAB_KEY;
import static io.github.lounode.ae2cs.common.util.resourcelocation.ResourceLocationUtil.prefix;

//@Mod(AE2CrystalSeedsAPI.MOD_ID)
public class AE2CrystalSeeds {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2CrystalSeeds(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        bind(modEventBus, Registries.BLOCK, AE2CrystalSeedsBlocks::registerBlocks);
        bind(modEventBus, Registries.BLOCK_ENTITY_TYPE, AE2CrystalSeedsBlockEntities::registerTiles);
        bindForItems(modEventBus, AE2CrystalSeedsBlocks::registerItemBlocks);
        bind(modEventBus, Registries.DATA_COMPONENT_TYPE, (BiConsumer<DataComponentType<?>, ResourceLocation> r) -> {
            r.accept(CrystalSeedItem.GROW_PROCESS, prefix("grow_process"));
        });
        bindForItems(modEventBus, AE2CrystalSeedsItems::registerItems);

        modEventBus.addListener(this::addCreative);
        bind(modEventBus, Registries.CREATIVE_MODE_TAB, AE2CSCreativeTabs::registerCreativeTabs);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AE2_CRYSTAL_SEEDS_TAB_KEY) {
            event.accept(AE2CrystalSeedsBlocks.crystalGrowthChamber);
            event.accept(AE2CrystalSeedsBlocks.circuitEtcher);
            event.accept(AE2CrystalSeedsBlocks.crystalVibrationChamber);
            event.accept(AE2CrystalSeedsBlocks.crusher);
            event.accept(AE2CrystalSeedsBlocks.quartzGrindstone);

            event.accept(AE2CrystalSeedsItems.crystalGrowthCard);

            event.accept(AE2CrystalSeedsItems.pureCertusQuartzCrystal);
            event.accept(AE2CrystalSeedsItems.pureFluixCrystal);
            event.accept(AE2CrystalSeedsItems.pureNetherQuartzCrystal);
            event.accept(AE2CrystalSeedsItems.pureEntroCrystal);
            event.accept(AE2CrystalSeedsItems.pureRedstoneCrystal);
            event.accept(AE2CrystalSeedsItems.pureQuantumCrystal);
            event.accept(AE2CrystalSeedsItems.pureRoseQuartz);
            event.accept(AE2CrystalSeedsItems.pureEnderQuartz);
            event.accept(AE2CrystalSeedsItems.pureResonatingCrystal);
            event.accept(AE2CrystalSeedsItems.pureMeteorCrystal);

            event.accept(AE2CrystalSeedsItems.certusQuartzSeed);
            event.accept(AE2CrystalSeedsItems.fluixCrystalSeed);
            event.accept(AE2CrystalSeedsItems.netherQuartzSeed);
            event.accept(AE2CrystalSeedsItems.entroCrystalSeed);
            event.accept(AE2CrystalSeedsItems.redstoneCrystalSeed);
            event.accept(AE2CrystalSeedsItems.quantumCrystalSeed);
            event.accept(AE2CrystalSeedsItems.roseQuartzSeed);
            event.accept(AE2CrystalSeedsItems.enderQuartzSeed);
            event.accept(AE2CrystalSeedsItems.resonatingSeed);
            event.accept(AE2CrystalSeedsItems.meteorSeed);
        }
    }

    private static <T> void bind(IEventBus modEventBus, ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        modEventBus.addListener((RegisterEvent event) -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    private void bindForItems(IEventBus modEventBus, Consumer<BiConsumer<Item, ResourceLocation>> source) {
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.ITEM)) {
                source.accept((t, rl) -> {
                    event.register(Registries.ITEM, rl, () -> t);
                });
            }
        });
    }
}
