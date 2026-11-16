package io.github.lounode.ae2cs.integration.dataenergistics.entrypoint;

import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsEntrypoint;
import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsPlugin;
import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsRegistry;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderCapabilities;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderToolbarActions;
import com.fish_dan_.data_energistics.api.registry.machine.capacity.CraftingMachineCapacityRegistration;
import com.fish_dan_.data_energistics.api.registry.provider.definition.PatternProviderRuntimeRegistration;
import com.fish_dan_.data_energistics.api.registry.provider.dispatch.PatternProviderDispatchHooks;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderLogic;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import io.github.lounode.ae2cs.integration.dataenergistics.patternprovider.Ae2CrystalScienceAdaptiveRoute;
import io.github.lounode.ae2cs.integration.dataenergistics.patternprovider.Ae2CrystalScienceCapacity;
import io.github.lounode.ae2cs.integration.dataenergistics.patternprovider.Ae2CrystalScienceMeteoriteRoute;
import io.github.lounode.ae2cs.integration.dataenergistics.patternprovider.CrystalScienceProviderDefinitions;
import it.unimi.dsi.fastutil.objects.ObjectList;

/** Registers AE2 Crystal Science pattern-provider variants and routes. */
@DataEnergisticsEntrypoint(requiredMods = "data_energistics")
public final class CrystalScienceDataEnergisticsPlugin implements DataEnergisticsPlugin {

    @Override
    public void register(DataEnergisticsRegistry registry) {
        registry.patternProviders().registerRuntime(PatternProviderRuntimeRegistration.<ResonatingPatternProviderLogic>builder()
                .registrationId(AE2CrystalScience.parseOrMakeId("de/resonating_dispatch"))
                .providerType(ResonatingPatternProviderLogic.class)
                .dispatchHooks(logic -> PatternProviderDispatchHooks.requestingRedstoneTuning(logic, logic::hasPendingDispatchInputs))
                .build());
        registry.patternProviders().registerRuntime(PatternProviderRuntimeRegistration.<MeteoritePatternProviderLogic>builder()
                .registrationId(AE2CrystalScience.parseOrMakeId("de/meteorite_dispatch"))
                .providerType(MeteoritePatternProviderLogic.class)
                .dispatchHooks(logic -> PatternProviderDispatchHooks.requestingRedstoneTuning(logic, logic::hasPendingDispatchInputs))
                .build());
        registry.adaptivePatternProviders().register(CrystalScienceProviderDefinitions.fixed(
                "ae2cs/simple",
                CrystalScienceProviderDefinitions.itemIds(
                        "ae2cs:simple_pattern_provider", "ae2cs:simple_pattern_provider_part"),
                5,
                CrystalScienceProviderDefinitions.capabilities()));
        registry.adaptivePatternProviders().register(CrystalScienceProviderDefinitions.fixed(
                "ae2cs/resonating",
                CrystalScienceProviderDefinitions.itemIds(
                        "ae2cs:resonating_pattern_provider", "ae2cs:resonating_pattern_provider_part"),
                9,
                CrystalScienceProviderDefinitions.capabilities(
                        AdaptivePatternProviderCapabilities.RESONATING),
                new Ae2CrystalScienceAdaptiveRoute(),
                AdaptivePatternProviderToolbarActions.RESONATING_PULL));
        registry.adaptivePatternProviders().register(CrystalScienceProviderDefinitions.fixed(
                "ae2cs/extended_resonating",
                CrystalScienceProviderDefinitions.itemIds(
                        "ae2cs:extended_resonating_pattern_provider",
                        "ae2cs:extended_resonating_pattern_provider_part",
                        "ae2cs:ex_resonating_pattern_provider",
                        "ae2cs:ex_resonating_pattern_provider_part"),
                36,
                CrystalScienceProviderDefinitions.capabilities(
                        AdaptivePatternProviderCapabilities.RESONATING),
                new Ae2CrystalScienceAdaptiveRoute(),
                AdaptivePatternProviderToolbarActions.RESONATING_PULL));
        registry.adaptivePatternProviders().register(CrystalScienceProviderDefinitions.fixed(
                "ae2cs/meteorite",
                CrystalScienceProviderDefinitions.itemIds(
                        "ae2cs:meteorite_pattern_provider", "ae2cs:meteorite_pattern_provider_part"),
                63,
                CrystalScienceProviderDefinitions.capabilities(
                        AdaptivePatternProviderCapabilities.METEORITE),
                new Ae2CrystalScienceMeteoriteRoute()));
        for (String id : ObjectList.of("circuit_etcher", "crystal_aggregator", "crystal_pulverizer",
                "quartz_grindstone", "crystal_growth_chamber", "entropy_variation_reaction_chamber")) {
            registry.craftingMachines().registerCapacity(CraftingMachineCapacityRegistration.blockEntity(
                    AE2CrystalScience.parseOrMakeId("de/" + id + "_capacity"),
                    AE2CrystalScience.parseOrMakeId(id),
                    Ae2CrystalScienceCapacity::capture));
        }
    }
}
