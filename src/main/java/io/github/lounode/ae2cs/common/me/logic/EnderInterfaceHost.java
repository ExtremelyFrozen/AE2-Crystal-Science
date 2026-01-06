package io.github.lounode.ae2cs.common.me.logic;

import appeng.helpers.InterfaceLogicHost;
import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;

public interface EnderInterfaceHost extends InterfaceLogicHost, ICustomRenderBounding
{
    EnderInterfaceLogic getEnderInterfaceLogic();

    void markForLogicClientUpdate();

    @Override
    default boolean enableCustomRenderBounding()
    {
        return getEnderInterfaceLogic().isRenderRangeInClient();
    }

    @Override
    default int getRange()
    {
        return getEnderInterfaceLogic().getRange();
    }
}
