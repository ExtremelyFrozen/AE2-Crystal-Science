package io.github.lounode.ae2cs.datagen.worldgen;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class AECSConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> CERTUS_QUARTZ_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, AE2CrystalScience.makeId("certus_quartz_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> CHARGED_CERTUS_QUARTZ_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, AE2CrystalScience.makeId("charged_certus_quartz_ore"));

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneRule = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateRule = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // 普通赛特斯矿
        List<OreConfiguration.TargetBlockState> certusTargets = List.of(
                OreConfiguration.target(stoneRule, AECSBlocks.CERTUS_QUARTZ_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateRule, AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get().defaultBlockState()));
        context.register(CERTUS_QUARTZ_ORE,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(certusTargets, 4)));

        // 充能赛特斯矿
        List<OreConfiguration.TargetBlockState> chargedTargets = List.of(
                OreConfiguration.target(stoneRule, AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateRule, AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get().defaultBlockState()));
        context.register(CHARGED_CERTUS_QUARTZ_ORE,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(chargedTargets, 4)));
    }
}
