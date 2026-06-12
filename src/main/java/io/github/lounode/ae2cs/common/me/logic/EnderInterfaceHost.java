package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;

import appeng.helpers.InterfaceLogicHost;

public interface EnderInterfaceHost extends InterfaceLogicHost, ICustomRenderBounding {

    EnderInterfaceLogic getEnderInterfaceLogic();

    void markForLogicClientUpdate();

    @Override
    default boolean enableCustomRenderBounding() {
        return getEnderInterfaceLogic().isRenderRangeInClient();
    }

    @Override
    default int getRange() {
        return getEnderInterfaceLogic().getRange();
    }

    boolean isExtended();
}
