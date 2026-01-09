package io.github.lounode.ae2cs.common.machine;

import appeng.api.networking.IManagedGridNode;

public abstract class NetworkMachineComponent extends BaseMachineComponent
{
    IManagedGridNode node;

    public NetworkMachineComponent(IManagedGridNode node)
    {
        this.node = node;
    }

    public IManagedGridNode getMainNode()
    {
        return node;
    }
}
