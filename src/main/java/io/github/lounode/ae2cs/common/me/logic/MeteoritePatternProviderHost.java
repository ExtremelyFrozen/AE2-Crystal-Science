package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

public interface MeteoritePatternProviderHost extends PatternProviderLogicHost, IUpgradeableObject
{
    @Override
    default IUpgradeInventory getUpgrades()
    {
        if (getLogic() instanceof MeteoritePatternProviderLogic logic)
            return logic.getUpgrades();
        else
            return UpgradeInventories.empty();
    }
}
