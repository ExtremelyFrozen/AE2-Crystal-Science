package io.github.lounode.ae2cs.integration.dataenergistics.client;

import io.github.lounode.ae2cs.client.gui.UpgradeablePatternProviderGUI;

import com.fish_dan_.data_energistics.api.registry.provider.client.PatternProviderScreenRegistration;
import com.fish_dan_.data_energistics.api.registry.provider.client.PatternProviderUpgradeLayout;

/** Client-only optional screen constructor entry; its signature contains only AECS types. */
public final class CrystalScienceProviderScreens {

    private CrystalScienceProviderScreens() {}

    public static void attach(UpgradeablePatternProviderGUI<?> screen) {
        PatternProviderScreenRegistration.attachRedstoneTuning(screen, PatternProviderUpgradeLayout.INLINE_SINGLE);
    }
}
