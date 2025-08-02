package io.github.lounode.ae2_crystal_seeds;

import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import io.github.lounode.ae2_crystal_seeds.common.block.AE2CrystalSeedsBlocks;
import io.github.lounode.ae2_crystal_seeds.common.block.entity.AE2CrystalSeedsBlockEntities;
import io.github.lounode.ae2_crystal_seeds.common.item.AE2CrystalSeedsItems;
import io.github.lounode.ae2_crystal_seeds.common.item.CrystalSeedItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.github.lounode.ae2_crystal_seeds.AE2CrystalSeedsCreativeTabs.AE2_CRYSTAL_SEEDS_TAB_KEY;
import static io.github.lounode.ae2_crystal_seeds.common.item.AE2CrystalSeedsItems.*;
import static io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.ResourceLocationUtil.prefix;

@Mod(AE2CrystalSeedsAPI.MOD_ID)
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
        bind(modEventBus, Registries.CREATIVE_MODE_TAB, AE2CrystalSeedsCreativeTabs::registerCreativeTabs);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AE2_CRYSTAL_SEEDS_TAB_KEY) {
            event.accept(AE2CrystalSeedsBlocks.crystalGrowthChamber);
            event.accept(certusQuartzSeed);
            event.accept(fluixCrystalSeed);
            event.accept(netherQuartzSeed);
            event.accept(entroCrystalSeed);
            event.accept(pureCertusQuartzCrystal);
            event.accept(pureFluixCrystal);
            event.accept(pureNetherQuartzCrystal);
            event.accept(pureEntroCrystal);
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
