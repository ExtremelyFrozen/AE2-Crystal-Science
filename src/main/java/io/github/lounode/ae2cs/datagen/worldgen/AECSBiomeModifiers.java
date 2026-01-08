package io.github.lounode.ae2cs.datagen.worldgen;

import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AECSBiomeModifiers
{
    public static final ResourceKey<BiomeModifier> ADD_CERTUS_QUARTZ_ORES = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, AE2CrystalScience.makeId("add_certus_quartz_ores"));

    public static void bootstrap(BootstrapContext<BiomeModifier> context)
    {
        HolderGetter<Biome> biomeLookup = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedLookup = context.lookup(Registries.PLACED_FEATURE);

        HolderSet<Biome> overworldBiomes = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);

        Holder.Reference<PlacedFeature> certusPlaced = placedLookup.getOrThrow(AECSPlacedFeatures.CERTUS_QUARTZ_ORE_PLACED);
        Holder.Reference<PlacedFeature> chargedPlaced = placedLookup.getOrThrow(AECSPlacedFeatures.CHARGED_CERTUS_QUARTZ_ORE_PLACED);

        HolderSet<PlacedFeature> features = HolderSet.direct(certusPlaced, chargedPlaced);

        context.register(ADD_CERTUS_QUARTZ_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes, features, GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }
}
