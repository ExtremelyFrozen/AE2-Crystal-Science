package io.github.lounode.ae2cs.integration.dataenergistics.client;

import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.client.gui.widgets.AECSSettingToggleButton;

import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsEntrypoint;
import com.fish_dan_.data_energistics.api.entrypoint.client.DataEnergisticsClientPlugin;
import com.fish_dan_.data_energistics.api.entrypoint.client.DataEnergisticsClientRegistry;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderToolbarActions;
import com.fish_dan_.data_energistics.api.registry.adaptive.client.AdaptivePatternProviderToolbarButton;

/**
 * Client-only AE2CS target-storage extraction control for Adaptive providers.
 */
@DataEnergisticsEntrypoint(clientOnly = true, requiredMods = "data_energistics")
public final class CrystalScienceDataEnergisticsClientPlugin implements DataEnergisticsClientPlugin {

    @Override
    public void register(DataEnergisticsClientRegistry registry) {
        var toolbar = registry.adaptivePatternProviderToolbar();
        toolbar.register(AdaptivePatternProviderToolbarActions.RESONATING_PULL, 700, context -> {
            var menu = context.menu();
            var button = new AECSSettingToggleButton<>(AECSSettings.PULL_MODE, PullMode.PULL_OFF,
                    (control, backwards) -> menu.sendSetResonatingPullEnabled(
                            control.getNextValue(backwards) == PullMode.PULL_ON));
            return new AdaptivePatternProviderToolbarButton(button,
                    () -> button.set(menu.isResonatingPullEnabled() ? PullMode.PULL_ON : PullMode.PULL_OFF));
        });
    }
}
