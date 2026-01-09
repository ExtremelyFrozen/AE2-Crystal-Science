package io.github.lounode.ae2cs.common.machine.component;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import io.github.lounode.ae2cs.api.genericinv.CombinedGenericInternalInventory;
import io.github.lounode.ae2cs.api.genericinv.GenericStackInvWrapper;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class InventoryComponent extends BaseMachineComponent
{
    /**
     * 原始仓库
     */
    private final Map<InvPort, GenericStackInv> ports = new EnumMap<>(InvPort.class);
    /**
     * 包装仓库，一般用于能力暴露
     */
    private final Map<InvPort, GenericStackInvWrapper> portWrappers = new EnumMap<>(InvPort.class);
    /**
     * 仓库集合，用于能力暴露
     */
    private CombinedGenericInternalInventory combined;

    /**
     * 输入之前必须把change给Inv准备好
     */
    public InventoryComponent addPort(InvPort port, GenericStackInv inv)
    {
        ports.put(port, inv);
        return this;
    }

    @Override
    public void onConstruct(MachineComponentContainer container)
    {
        if (ports.isEmpty()) throw new IllegalStateException("No ports available");

        var list = new ArrayList<GenericStackInvWrapper>();
        var added = new HashSet<GenericStackInv>();
        for (var portEntry : ports.entrySet())
        {
            switch (portEntry.getKey())
            {
                case INPUT ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    GenericStackInvWrapper wrapper = new GenericStackInvWrapper(portEntry.getValue())
                    {
                        @Override
                        public long extract(int slot, AEKey what, long amount, Actionable mode)
                        {
                            return 0;
                        }
                    };
                    list.add(wrapper);
                    portWrappers.put(InvPort.INPUT, wrapper);
                }
                case OUTPUT ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    GenericStackInvWrapper wrapper = new GenericStackInvWrapper(portEntry.getValue())
                    {
                        @Override
                        public long insert(int slot, AEKey what, long amount, Actionable mode)
                        {
                            return 0;
                        }
                    };
                    list.add(wrapper);
                    portWrappers.put(InvPort.OUTPUT, wrapper);
                }
                case WORK ->
                {
                    if (added.contains(portEntry.getValue())) continue;
                    added.add(portEntry.getValue());
                    GenericStackInvWrapper wrapper = new GenericStackInvWrapper(portEntry.getValue());
                    list.add(wrapper);
                    portWrappers.put(InvPort.WORK, wrapper);
                }
            }
        }
        combined = new CombinedGenericInternalInventory(list.toArray(GenericStackInvWrapper[]::new));

        container.exposeService(InventoryComponent.class, this);
        container.exposeService(CombinedGenericInternalInventory.class, combined);
    }

    public @Nullable GenericStackInv port(InvPort port)
    {
        return ports.get(port);
    }

    /**
     * 默认输出的combined会对各类port进行一次包装，规则如下：
     * - INPUT 允许输入，不允许输出
     * - OUTPUT 允许输出，不允许输入
     * - WORK 完全尊重原始Inv
     * 如果单个仓库被标记为多个port，则以其第一个标记的port为准
     */
    public @NotNull CombinedGenericInternalInventory combined()
    {
        return combined;
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider r)
    {
        for (var e : ports.entrySet()) e.getValue().writeToChildTag(tag, "inv_" + e.getKey().name().toLowerCase(), r);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider r)
    {
        for (var e : ports.entrySet()) e.getValue().readFromChildTag(tag, "inv_" + e.getKey().name().toLowerCase(), r);
    }

    @Override
    public void addDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        for (var inv : ports.values())
        {
            for (GenericStack gs : inv.toList())
            {
                if (gs == null) continue;
                gs.what().addDrops(gs.amount(), drops, level, pos);
            }
        }
    }

    @Override
    public void clearContent()
    {
        ports.values().forEach(GenericStackInv::clear);
    }
}
