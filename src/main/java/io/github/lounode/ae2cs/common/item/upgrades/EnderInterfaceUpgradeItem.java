package io.github.lounode.ae2cs.common.item.upgrades;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.api.util.BlockDefinitionSupplier;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;

public class EnderInterfaceUpgradeItem extends UpgradeItem
{

    public EnderInterfaceUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(BlockDefinitionSupplier.of(AEBlocks.INTERFACE), AECSBlocks.ENDER_INTERFACE_BLOCK);
        registerPartReplaceInfo(AEParts.INTERFACE, AECSParts.ENDER_INTERFACE_PART);
    }
}
