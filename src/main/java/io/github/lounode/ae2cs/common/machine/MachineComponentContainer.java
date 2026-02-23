package io.github.lounode.ae2cs.common.machine;

import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.EnergyComponent;
import io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent;
import io.github.lounode.ae2cs.common.machine.component.IMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MachineComponentContainer
{
    private final IMachineHost host;
    private final List<IMachineComponent> components = new ArrayList<>();
    private final Map<Class<?>, Object> services = new HashMap<>();

    @Nullable
    private AppEngInvComponent appEngInvComponent;

    @Nullable
    private GenericStackInvComponent genericStackInvComponent;

    @Nullable
    private EnergyComponent energyComponent;


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

    public <T> void exposeService(Class<T> key, T impl)
    {
        services.put(key, impl);

        if (impl instanceof AppEngInvComponent inv)
            this.appEngInvComponent = inv;
        else if (impl instanceof GenericStackInvComponent inv)
            this.genericStackInvComponent = inv;
        else if (impl instanceof EnergyComponent energy)
            this.energyComponent = energy;
    }

    public boolean hasService(Class<?> key)
    {
        if (key == AppEngInvComponent.class) return appEngInvComponent != null;
        if (key == GenericStackInvComponent.class) return genericStackInvComponent != null;
        if (key == EnergyComponent.class) return energyComponent != null;

        return services.containsKey(key);
    }

    @NotNull
    public <T> T getService(Class<T> key)
    {
        Object obj;
        if (key == AppEngInvComponent.class) obj = appEngInvComponent;
        else if (key == GenericStackInvComponent.class) obj = genericStackInvComponent;
        else if (key == EnergyComponent.class) obj = energyComponent;
        else obj = services.get(key);

        if (obj == null) throw new IllegalStateException("Missing services: " + key.getName());
        return key.cast(obj);
    }

    public <T extends IMachineComponent> boolean hasComponent(Class<T> type)
    {
        for (var c : components) if (type.isInstance(c)) return true;
        return false;
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

    public void importSettings(MachineContext ctx, CompoundTag input, @Nullable Player player)
    {
        components.forEach(component -> component.importSettings(ctx, input, player));
    }

    public void exportSettings(MachineContext ctx, CompoundTag builder, @Nullable Player player)
    {
        components.forEach(component -> component.exportSettings(ctx, builder, player));
    }

    public void writeNbt(CompoundTag tag)
    {
        components.forEach(component -> component.writeNbt(tag));
    }

    public void readNbt(CompoundTag tag)
    {
        components.forEach(component -> component.readNbt(tag));
    }

    public void writeStream(FriendlyByteBuf data)
    {
        components.forEach(component -> component.writeStream(data));
    }

    public boolean readStream(FriendlyByteBuf data)
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