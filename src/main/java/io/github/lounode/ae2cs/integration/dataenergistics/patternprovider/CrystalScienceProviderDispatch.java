package io.github.lounode.ae2cs.integration.dataenergistics.patternprovider;

import io.github.lounode.ae2cs.common.me.logic.OptionalProviderDispatch;

import appeng.helpers.patternprovider.PatternProviderLogic;

import com.fish_dan_.data_energistics.api.registry.provider.dispatch.PatternProviderDispatchHooks;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Bridges native overrides to the same policy already attached by the AE2 base lifecycle hooks. */
@NullMarked
public final class CrystalScienceProviderDispatch {

    private CrystalScienceProviderDispatch() {}

    public static @Nullable OptionalProviderDispatch callbacks(PatternProviderLogic logic) {
        var hooks = PatternProviderDispatchHooks.attachedTo(logic);
        return hooks == null ? null : new OptionalProviderDispatch(
                hooks::redstoneInputChanged, hooks::patternPushed, hooks::afterWork);
    }
}
