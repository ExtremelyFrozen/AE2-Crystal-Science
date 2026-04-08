package io.github.lounode.ae2cs.common.me.logic;

import appeng.helpers.patternprovider.PatternProviderLogicHost;

public interface MirroredSimplePatternProviderHost extends PatternProviderLogicHost
{
    MirroredSimplePatternProviderLogic getMirroringLogic();

    @Override
    default boolean isVisibleInTerminal() {
        return !getMirroringLogic().isMirroring() && PatternProviderLogicHost.super.isVisibleInTerminal();
    }
}
