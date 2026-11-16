package io.github.lounode.ae2cs.integration.dataenergistics.patternprovider;

import io.github.lounode.ae2cs.common.menu.UpgradeablePatternProviderMenu;

import com.fish_dan_.data_energistics.api.registry.provider.menu.PatternProviderMenuRegistration;

/** Optional menu constructor entry; its signature contains only AECS types. */
public final class CrystalScienceProviderMenus {

    private CrystalScienceProviderMenus() {}

    public static void attach(UpgradeablePatternProviderMenu menu) {
        PatternProviderMenuRegistration.attachRedstoneTuning(menu);
    }
}
