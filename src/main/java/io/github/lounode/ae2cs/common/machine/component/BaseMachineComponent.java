package io.github.lounode.ae2cs.common.machine.component;

import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;

public abstract class BaseMachineComponent implements IMachineComponent
{
    protected MachineComponentContainer container;

    @Override
    public void onConstruct(MachineComponentContainer container)
    {
        IMachineComponent.super.onConstruct(container);
        this.container = container;
    }

    protected void markChanged()
    {
        if (container != null)
        {
            container.host().markChanged();
        }
    }

    protected void markForClientUpdate()
    {
        if (container != null)
        {
            container.host().markForClientUpdate();
        }
    }
}
