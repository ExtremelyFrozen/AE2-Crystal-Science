package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;

public class ExtendedIntegratedInterfaceUpgradeItem extends UpgradeItem
{
    public ExtendedIntegratedInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.INTEGRATED_INTERFACE_BLOCK, AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK);
        registerPartReplaceInfo(AECSParts.INTEGRATE_INTERFACE_PART, AECSParts.EX_INTEGRATE_INTERFACE_PART);
    }
}
