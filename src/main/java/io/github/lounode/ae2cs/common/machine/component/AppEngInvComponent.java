package io.github.lounode.ae2cs.common.machine.component;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AppEngInvComponent extends BaseMachineComponent
{
    /**
     * 原始仓库
     */
    private final Map<InvPort, AppEngInternalInventory> ports = new EnumMap<>(InvPort.class);
    /**
     * 包装仓库，一般用于能力暴露
     */
    private final Map<InvPort, FilteredInternalInventory> portWrappers = new EnumMap<>(InvPort.class);
    /**
     * 仓库集合，用于能力暴露
     */
    private CombinedInternalInventory combined;

    /**
     * 输入之前必须把change给Inv准备好
     */
    public AppEngInvComponent addPort(InvPort port, AppEngInternalInventory inv)
    {
        ports.put(port, inv);
        return this;
    }

    public AppEngInvComponent setWrap(InvPort port, FilteredInternalInventory inv)
    {
        portWrappers.put(port, inv);
        Set<FilteredInternalInventory> filters = Collections.newSetFromMap(new IdentityHashMap<>());
        filters.addAll(portWrappers.values());
        combined = new CombinedInternalInventory(filters.toArray(FilteredInternalInventory[]::new));

        if (container != null)
        {
            container.host().invalidCap();
            if (container.hasService(SideConfigComponent.class))
            {
                container.getService(SideConfigComponent.class).invalidateAllCaches();
            }
        }
        return this;
    }

    @Override
    public void onConstruct(MachineComponentContainer container)
    {
        if (ports.isEmpty()) throw new IllegalStateException("No ports available");

        var list = new ArrayList<FilteredInternalInventory>();
        var added = new HashSet<AppEngInternalInventory>();
        for (var portEntry : ports.entrySet())
        {
            switch (portEntry.getKey())
            {
                case INPUT ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    FilteredInternalInventory wrapper = new FilteredInternalInventory(portEntry.getValue(), new IAEItemFilter()
                    {
                        @Override
                        public boolean allowExtract(InternalInventory inv, int slot, int amount)
                        {
                            return false;
                        }
                    });
                    list.add(wrapper);
                    portWrappers.put(InvPort.INPUT, wrapper);
                }
                case OUTPUT ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    FilteredInternalInventory wrapper = new FilteredInternalInventory(portEntry.getValue(), new IAEItemFilter()
                    {
                        @Override
                        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
                        {
                            return false;
                        }
                    });
                    list.add(wrapper);
                    portWrappers.put(InvPort.OUTPUT, wrapper);
                }
                case WORK ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    FilteredInternalInventory wrapper = new FilteredInternalInventory(portEntry.getValue(), new IAEItemFilter()
                    {
                    });
                    list.add(wrapper);
                    portWrappers.put(InvPort.WORK, wrapper);
                }
            }
        }
        combined = new CombinedInternalInventory(list.toArray(FilteredInternalInventory[]::new));

        container.exposeService(AppEngInvComponent.class, this);
        container.exposeService(CombinedInternalInventory.class, combined);
    }

    public @Nullable AppEngInternalInventory port(InvPort port)
    {
        return ports.get(port);
    }

    public @Nullable FilteredInternalInventory warp(InvPort port)
    {
        return portWrappers.get(port);
    }

    /**
     * 默认输出的combined会对各类port进行一次包装，规则如下：
     * - INPUT 允许输入，不允许输出
     * - OUTPUT 允许输出，不允许输入
     * - WORK 完全尊重原始Inv
     * 如果单个仓库被标记为多个port，则以其第一个标记的port为准
     */
    public @NotNull CombinedInternalInventory combined()
    {
        return combined;
    }

    @Override
    public void writeNbt(CompoundTag tag)
    {
        for (var e : ports.entrySet()) e.getValue().writeToNBT(tag, "inv_" + e.getKey().name().toLowerCase());
    }

    @Override
    public void readNbt(CompoundTag tag)
    {
        for (var e : ports.entrySet()) e.getValue().readFromNBT(tag, "inv_" + e.getKey().name().toLowerCase());
    }

    @Override
    public void addDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        for (var inv : ports.values())
        {
            for (ItemStack stack : inv)
            {
                if (stack.isEmpty()) continue;
                drops.add(stack.copy());
            }
        }
    }

    @Override
    public void clearContent()
    {
        ports.values().forEach(AppEngInternalInventory::clear);
    }
}
