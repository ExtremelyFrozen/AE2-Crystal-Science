package io.github.lounode.ae2cs.datagen;

import com.mojang.logging.LogUtils;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.datagen.recipes.*;
import io.github.lounode.ae2cs.datagen.recipes.compat.*;
import io.github.lounode.ae2cs.datagen.worldgen.AECSBiomeModifiers;
import io.github.lounode.ae2cs.datagen.worldgen.AECSConfiguredFeatures;
import io.github.lounode.ae2cs.datagen.worldgen.AECSPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID)
public class DataGenerators
{
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        LOGGER.info("数据生成启动");

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        CompletableFuture<HolderLookup.Provider> baseLookupProvider = event.getLookupProvider();

        var builtin = new DatapackBuiltinEntriesProvider(
                packOutput,
                baseLookupProvider,
                new RegistrySetBuilder()
                        .add(Registries.CONFIGURED_FEATURE, AECSConfiguredFeatures::bootstrap)
                        .add(Registries.PLACED_FEATURE, AECSPlacedFeatures::bootstrap)
                        .add(ForgeRegistries.Keys.BIOME_MODIFIERS, AECSBiomeModifiers::bootstrap),
                Set.of(AECSConstants.MODID)
        );
        generator.addProvider(event.includeServer(), builtin);

        CompletableFuture<HolderLookup.Provider> lookupProvider = builtin.getRegistryProvider();

        // 生成方块战利品表
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(AECSBlockLootTableProvider::new, LootContextParamSets.BLOCK))
        ));

        // 生成物品和方块模型 / blockstate（一般不依赖 lookupProvider，但保持原样即可）
        generator.addProvider(event.includeClient(), new AECSItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new AECSBlockModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new AECSBlockStateProvider(packOutput, existingFileHelper));

        // 生成方块、物品、流体标签
        BlockTagsProvider blockTagsProvider = new AECSBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new AECSItemTagProvider(
                packOutput,
                lookupProvider,
                blockTagsProvider.contentsGetter(),
                existingFileHelper
        ));
        generator.addProvider(event.includeServer(), new AECSFluidTagsProvider(packOutput, lookupProvider, existingFileHelper));

        // 生成配方表
        generator.addProvider(event.includeServer(), new AECSAggregatorRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCraftRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSEtcherRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSFurnaceRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSMiscRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSPulverizerRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSStonecutterRecipeProvider(packOutput, lookupProvider));
        // 联动配方
        generator.addProvider(event.includeServer(), new AECSCompatAAERecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatAFRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatCreateRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatEAERecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatMegaCellRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatMEKRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new AECSCompatOCRecipeProvider(packOutput, lookupProvider));
    }
}
