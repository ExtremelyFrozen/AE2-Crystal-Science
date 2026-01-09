package io.github.lounode.ae2cs.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MachineComponentContainer
{
    private final IMachineHost host;
    private final List<IMachineComponent> components = new ArrayList<>();
    private final Map<Class<?>, Object> services = new HashMap<>();

    public MachineComponentContainer(IMachineHost host)
    {
        this.host = host;
    }

    public IMachineHost host()
    {
        return host;
    }

    public <T extends IMachineComponent> T add(T component)
    {
        components.add(component);
        component.onConstruct(this);
        return component;
    }

    public void exposeService(Class<?> key, Object impl)
    {
        services.put(key, impl);
    }

    public boolean hasService(Class<?> key)
    {
        return services.containsKey(key);
    }

    public <T extends IMachineComponent> boolean hasComponent(Class<T> type)
    {
        for (var c : components) if (type.isInstance(c)) return true;
        return false;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> key)
    {
        T service = (T) services.get(key);
        if (service == null) throw new IllegalStateException("Missing services: " + key.getName());
        return service;
    }

    @NotNull
    public <T extends IMachineComponent> T get(Class<T> type)
    {
        for (var c : components) if (type.isInstance(c)) return type.cast(c);
        throw new IllegalStateException("Missing component: " + type.getName());
    }

    public void onLoad(MachineContext ctx)
    {
        components.forEach(component -> component.onLoad(ctx));
    }

    public void onServerTick(MachineContext ctx)
    {
        components.forEach(component -> component.onServerTick(ctx));
    }

    public void onClientTick(MachineContext ctx)
    {
        components.forEach(component -> component.onClientTick(ctx));
    }

    public void writeNbt(CompoundTag tag, HolderLookup.Provider r)
    {
        components.forEach(component -> component.writeNbt(tag, r));
    }

    public void readNbt(CompoundTag tag, HolderLookup.Provider r)
    {
        components.forEach(component -> component.readNbt(tag, r));
    }

    public void writeStream(RegistryFriendlyByteBuf data)
    {
        components.forEach(component -> component.writeStream(data));
    }

    public boolean readStream(RegistryFriendlyByteBuf data)
    {
        boolean changed = false;

        for (var component : components)
        {
            if (component.readStream(data))
            {
                changed = true;
            }
        }
        return changed;
    }

    public void addDrops(Level l, BlockPos p, List<ItemStack> drops)
    {
        components.forEach(component -> component.addDrops(l, p, drops));
    }

    public void clearContent()
    {
        components.forEach(IMachineComponent::clearContent);
    }
}