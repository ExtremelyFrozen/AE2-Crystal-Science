package io.github.lounode.ae2cs.datagen;

import com.mojang.logging.LogUtils;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSEnchantments;
import io.github.lounode.ae2cs.datagen.properties.GrowProcess;
import io.github.lounode.ae2cs.datagen.recipes.*;
import io.github.lounode.ae2cs.datagen.recipes.compat.AECSCompatAAERecipeProvider;
import io.github.lounode.ae2cs.datagen.recipes.compat.AECSCompatEAERecipeProvider;
import io.github.lounode.ae2cs.datagen.recipes.compat.AECSCompatOCRecipeProvider;
import io.github.lounode.ae2cs.datagen.worldgen.AECSBiomeModifiers;
import io.github.lounode.ae2cs.datagen.worldgen.AECSConfiguredFeatures;
import io.github.lounode.ae2cs.datagen.worldgen.AECSPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class DataGenerators {
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        LOGGER.info("数据生成启动");

        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                        .add(Registries.ENCHANTMENT, AECSEnchantments::bootstrap)
                        .add(Registries.CONFIGURED_FEATURE, AECSConfiguredFeatures::bootstrap)
                        .add(Registries.PLACED_FEATURE, AECSPlacedFeatures::bootstrap)
                        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AECSBiomeModifiers::bootstrap),
                Set.of(AECSConstants.MODID));

        // 生成方块战利品表
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(AECSBlockLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookup
        ));

        // 生成物品和方块模型
        event.createProvider(AECSModelProvider::new);
        // 生成 AE 26.1 part item -> part model 绑定
        event.createProvider(AECSPartModelProvider::new);
        // 生成方块、物品、流体标签
        event.createProvider(AECSBlockTagProvider::new);
        event.createProvider(AECSItemTagProvider::new);
        event.createProvider(AECSFluidTagsProvider::new);

        event.addProvider(new DataComponentBindingProvider(event.getLookupProvider()));

        // 生成配方表
        event.createProvider(AECSAggregatorRecipeProvider.Runner::new);
        event.createProvider(AECSCraftRecipeProvider.Runner::new);
        event.createProvider(AECSEtcherRecipeProvider.Runner::new);
        event.createProvider(AECSFurnaceRecipeProvider.Runner::new);
        event.createProvider(AECSMiscRecipeProvider.Runner::new);
        event.createProvider(AECSPulverizerRecipeProvider.Runner::new);
        event.createProvider(AECSStonecutterRecipeProvider.Runner::new);
        // 联动配方
        event.createProvider(AECSCompatAAERecipeProvider.Runner::new);
//        generator.addProvider(event.includeServer(), new AECSCompatAFRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatAGRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatCreateRecipeProvider(packOutput, lookupProvider));
        event.createProvider(AECSCompatEAERecipeProvider.Runner::new);
//        generator.addProvider(event.includeServer(), new AECSCompatMegaCellRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatMEKRecipeProvider(packOutput, lookupProvider));
        event.createProvider(AECSCompatOCRecipeProvider.Runner::new);
    }

    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(AE2CrystalScience.makeId("grow_progress"), GrowProcess.MAP_CODEC);
    }

    private record DataComponentBindingProvider(CompletableFuture<HolderLookup.Provider> lookupProvider) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return lookupProvider.thenAccept(lookup -> lookup.lookupOrThrow(Registries.ITEM)
                    .listElements()
                    .filter(holder -> !holder.areComponentsBound())
                    .forEach(holder -> holder.bindComponents(DataComponentMap.EMPTY)));
        }

        @Override
        public String getName() {
            return "AE2 Crystal Science Data Component Binding";
        }
    }
}
