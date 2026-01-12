package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;

public class EnderInterfaceUpgradeItem extends UpgradeItem
{
    public EnderInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        addAllowedReplacement(AECSBlocks.ENDER_INTERFACE_BLOCK);
        addAllowedReplacement(AECSParts.ENDER_INTERFACE_PART);
    }
}
