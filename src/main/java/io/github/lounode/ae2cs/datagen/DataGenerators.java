package io.github.lounode.ae2cs.datagen;

import com.mojang.logging.LogUtils;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSEnchantments;
import io.github.lounode.ae2cs.datagen.properties.GrowProcess;
import io.github.lounode.ae2cs.datagen.recipes.*;
import io.github.lounode.ae2cs.datagen.worldgen.AECSBiomeModifiers;
import io.github.lounode.ae2cs.datagen.worldgen.AECSConfiguredFeatures;
import io.github.lounode.ae2cs.datagen.worldgen.AECSPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class DataGenerators
{
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event)
    {
        LOGGER.info("数据生成启动");

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        event.createProvider((output,lookup) -> new DatapackBuiltinEntriesProvider(
                output,
                lookup,
                new RegistrySetBuilder()
                        .add(Registries.ENCHANTMENT, AECSEnchantments::bootstrap)
                        .add(Registries.CONFIGURED_FEATURE, AECSConfiguredFeatures::bootstrap)
                        .add(Registries.PLACED_FEATURE, AECSPlacedFeatures::bootstrap)
                        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AECSBiomeModifiers::bootstrap),
                Set.of(AECSConstants.MODID)));


        // 生成方块战利品表
        event.createProvider((output,lookup) -> new LootTableProvider(
                output,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(AECSBlockLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookup
        ));

        // 生成物品和方块模型 / blockstate（一般不依赖 lookupProvider，但保持原样即可）
        event.createProvider((output,lookup) -> new AECSModelProvider(output));
//        generator.createProvider(event.includeServer(), new AECSBlockModelProvider(packOutput, existingFileHelper));
//        event.createProvider((output,lookup) -> new AECSBlockStateProvider(output));

        // 生成方块、物品、流体标签
//        BlockTagsProvider blockTagsProvider = new AECSBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
//        generator.addProvider(event.includeServer(), blockTagsProvider);
//        generator.addProvider(event.includeServer(), new AECSItemTagProvider(
//                packOutput,
//                lookupProvider,
//                blockTagsProvider.contentsGetter(),
//                existingFileHelper
//        ));
//        generator.addProvider(event.includeServer(), new AECSFluidTagsProvider(packOutput, lookupProvider, existingFileHelper));

        // 生成配方表
//        generator.addProvider(event.includeServer(), new AECSAggregatorRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCraftRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSEtcherRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSFurnaceRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSMiscRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSPulverizerRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSStonecutterRecipeProvider(packOutput, lookupProvider));
        // 联动配方
//        generator.addProvider(event.includeServer(), new AECSCompatAAERecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatAFRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatAGRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatCreateRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatEAERecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatMegaCellRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatMEKRecipeProvider(packOutput, lookupProvider));
//        generator.addProvider(event.includeServer(), new AECSCompatOCRecipeProvider(packOutput, lookupProvider));
    }

    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event){
        event.register(AE2CrystalScience.makeId("grow_progress"), GrowProcess.MAP_CODEC);
    }

}