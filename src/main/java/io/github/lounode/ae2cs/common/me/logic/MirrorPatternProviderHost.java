package io.github.lounode.ae2cs.common.me.logic;

import appeng.helpers.patternprovider.PatternProviderLogicHost;

public interface MirrorPatternProviderHost extends PatternProviderLogicHost
{
    MirrorPatternProviderLogic getMirroringLogic();

    @Override
    default boolean isVisibleInTerminal() {
        return false;
    }
}
