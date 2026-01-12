package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSParts;

public class ExtendedResonatingPatternProviderUpgradeItem extends UpgradeItem
{
    public ExtendedResonatingPatternProviderUpgradeItem(Properties properties)
    {
        super(properties);

        registerBlockReplaceInfo(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK);
        registerPartReplaceInfo(AECSParts.RESONATING_PATTERN_PROVIDER_PART, AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART);
    }
}
