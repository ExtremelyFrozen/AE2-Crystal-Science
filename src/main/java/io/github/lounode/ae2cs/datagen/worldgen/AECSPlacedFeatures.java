package io.github.lounode.ae2cs.datagen.worldgen;

import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class AECSPlacedFeatures
{
    public static final ResourceKey<PlacedFeature> CERTUS_QUARTZ_ORE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, AE2CrystalScience.makeId("certus_quartz_ore_placed"));

    public static final ResourceKey<PlacedFeature> CHARGED_CERTUS_QUARTZ_ORE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, AE2CrystalScience.makeId("charged_certus_quartz_ore_placed"));

    // 赛特斯石英矿石生成范围
    private static final int CERTUS_MIN_Y = -59;
    private static final int CERTUS_MAX_Y = 72;

    public static void bootstrap(BootstrapContext<PlacedFeature> context)
    {
        HolderGetter<ConfiguredFeature<?, ?>> cfgLookup = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> certusCfg = cfgLookup.getOrThrow(AECSConfiguredFeatures.CERTUS_QUARTZ_ORE);
        Holder<ConfiguredFeature<?, ?>> chargedCfg = cfgLookup.getOrThrow(AECSConfiguredFeatures.CHARGED_CERTUS_QUARTZ_ORE);

        List<PlacementModifier> common = List.of(
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(CERTUS_MIN_Y), VerticalAnchor.absolute(CERTUS_MAX_Y)),
                BiomeFilter.biome()
        );

        // 普通：18 次/区块
        context.register(CERTUS_QUARTZ_ORE_PLACED, new PlacedFeature(certusCfg, List.of(
                CountPlacement.of(18),
                common.get(0), common.get(1), common.get(2)
        )));

        // 充能：2 次/区块
        context.register(CHARGED_CERTUS_QUARTZ_ORE_PLACED, new PlacedFeature(chargedCfg, List.of(
                CountPlacement.of(2),
                common.get(0), common.get(1), common.get(2)
        )));
    }
}
