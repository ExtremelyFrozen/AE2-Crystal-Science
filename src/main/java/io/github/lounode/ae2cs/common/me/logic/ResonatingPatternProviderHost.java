package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import java.util.List;
import java.util.Optional;

public interface ResonatingPatternProviderHost extends PatternProviderLogicHost, IUpgradeableObject {

    ResonatingPatternProviderLogic getResonatingLogic();

    @Override
    default IUpgradeInventory getUpgrades() {
        if (getLogic() instanceof ResonatingPatternProviderLogic logic)
            return logic.getUpgrades();
        else
            return UpgradeInventories.empty();
    }

    boolean isExtended();

    default void readDefaultsFromItem(net.minecraft.world.item.ItemStack stack) {
        getResonatingLogic().readDefaultsFromItem(stack);
    }

    default void writeDefaultsToStack(net.minecraft.world.item.ItemStack stack) {
        getResonatingLogic().writeDefaultsToStack(stack);
    }

    default List<Optional<EncodedResonatingPattern.Target>> getDefaultInputTargets() {
        return getResonatingLogic().getDefaultInputTargets();
    }

    default int getDefaultSelectedInput() {
        return getResonatingLogic().getDefaultSelectedInput();
    }

    void markForLogicClientUpdate();
}
