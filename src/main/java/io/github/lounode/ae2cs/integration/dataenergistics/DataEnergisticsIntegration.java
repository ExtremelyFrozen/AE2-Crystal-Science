package io.github.lounode.ae2cs.integration.dataenergistics;

import net.neoforged.fml.ModList;

/** Native code checks this once-loaded flag before linking an optional DE compatibility wrapper. */
public final class DataEnergisticsIntegration {

    public static final boolean LOADED = ModList.get().isLoaded("data_energistics");

    private DataEnergisticsIntegration() {}
}
