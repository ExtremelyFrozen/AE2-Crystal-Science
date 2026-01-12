package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;

public class ExtendedEnderInterfaceUpgradeItem extends UpgradeItem
{
    public ExtendedEnderInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.ENDER_INTERFACE_BLOCK, AECSBlocks.EX_ENDER_INTERFACE_BLOCK);
        registerPartReplaceInfo(AECSParts.ENDER_INTERFACE_PART, AECSParts.EX_ENDER_INTERFACE_PART);
    }
}
