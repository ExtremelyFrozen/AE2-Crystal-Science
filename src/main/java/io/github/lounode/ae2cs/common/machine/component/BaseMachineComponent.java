package io.github.lounode.ae2cs.common.machine.component;

import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;

import org.jetbrains.annotations.Nullable;

public abstract class BaseMachineComponent implements IMachineComponent {

    protected @Nullable MachineComponentContainer container;

    @Override
    public void onConstruct(MachineComponentContainer container) {
        IMachineComponent.super.onConstruct(container);
        this.container = container;
    }

    protected void markChanged() {
        if (container != null) {
            container.host().markChanged();
        }
    }

    protected void markForClientUpdate() {
        if (container != null) {
            container.host().markForClientUpdate();
        }
    }
}
