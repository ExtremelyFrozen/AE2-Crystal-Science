package io.github.lounode.ae2cs.common.machine.component;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEKey;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.api.genericinv.GenericStackInvWrapper;
import io.github.lounode.ae2cs.api.networking.SideConfigField;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;

public class SideConfigComponent extends BaseMachineComponent
{
    private final EnumMap<Direction, SidePolicy> policies = new EnumMap<>(Direction.class);
    private boolean autoImport = false;
    private boolean autoExport = false;

    private MachineComponentContainer container;

    @Nullable
    private GenericStackInvComponent genericInvComponent = null;

    @Nullable
    private AppEngInvComponent appEngInvComponent = null;

    // appEngInvForSide缓存
    private final EnumMap<Direction, @Nullable BaseInternalInventory> appEngSideCache = new EnumMap<>(Direction.class);
    private final EnumSet<Direction> appEngSideCacheComputed = EnumSet.noneOf(Direction.class);

    // genericInvForSide缓存
    private final EnumMap<Direction, @Nullable GenericInternalInventory> genericSideCache = new EnumMap<>(Direction.class);
    private final EnumSet<Direction> genericSideCacheComputed = EnumSet.noneOf(Direction.class);

    private void invalidateAppEngCache(@Nullable Direction dir)
    {
        if (dir == null) return;
        appEngSideCacheComputed.remove(dir);
        appEngSideCache.remove(dir);
    }

    private void invalidateGenericCache(@Nullable Direction dir)
    {
        if (dir == null) return;
        genericSideCacheComputed.remove(dir);
        genericSideCache.remove(dir);
    }

    public void invalidateAllCaches()
    {
        appEngSideCacheComputed.clear();
        appEngSideCache.clear();

        genericSideCacheComputed.clear();
        genericSideCache.clear();
    }

    @Override
    public void onConstruct(MachineComponentContainer container)
    {
        this.container = container;

        if (container.hasService(GenericStackInvComponent.class))
            genericInvComponent = container.get(GenericStackInvComponent.class);
        if (container.hasService(AppEngInvComponent.class))
            appEngInvComponent = container.get(AppEngInvComponent.class);

        for (var dir : Direction.values())
        {
            policies.put(dir, SidePolicy.ALL);
        }

        invalidateAllCaches();

        container.exposeService(SideConfigComponent.class, this);
    }

    public EnumMap<Direction, SidePolicy> getPolicies()
    {
        return policies;
    }

    public boolean isAutoImport()
    {
        return autoImport;
    }

    public void setAutoImport(boolean autoImport)
    {
        if (this.autoImport == autoImport) return;

        // 主动模式不影响能力系统，只需要标脏即可
        this.autoImport = autoImport;
        this.container.host().markChanged();
    }

    public boolean isAutoExport()
    {
        return autoExport;
    }

    public void setAutoExport(boolean autoExport)
    {
        if (this.autoExport == autoExport) return;

        // 主动模式不影响能力系统，只需要标脏即可
        this.autoExport = autoExport;
        this.container.host().markChanged();
    }

    public SidePolicy get(Direction dir)
    {
        return policies.get(dir);
    }

    public void set(Direction dir, SidePolicy policy)
    {
        var old = policies.put(dir, policy);

        // 仅当实际变化时触发刷新 + 对应缓存失效
        if (old != policy)
        {
            invalidateAppEngCache(dir);
            invalidateGenericCache(dir);

            this.container.host().markChanged();
            this.container.host().invalidCap();
        }
    }

    public @Nullable GenericInternalInventory genericInvForSide(@Nullable Direction dir)
    {
        if (genericInvComponent == null) return null;
        if (dir == null) return genericInvComponent.combined();

        // 命中缓存（包括缓存结果为 null）
        if (genericSideCacheComputed.contains(dir))
        {
            return genericSideCache.get(dir);
        }

        SidePolicy policy = policies.get(dir);
        @Nullable GenericInternalInventory result;

        if (!policy.allowInsert() && !policy.allowExtract())
        {
            result = null;
        }
        else
        {
            result = new GenericStackInvWrapper(genericInvComponent.combined())
            {
                @Override
                public long insert(int slot, AEKey what, long amount, Actionable mode)
                {
                    return policy.allowInsert() ? super.insert(slot, what, amount, mode) : 0;
                }

                @Override
                public long extract(int slot, AEKey what, long amount, Actionable mode)
                {
                    return policy.allowExtract() ? super.extract(slot, what, amount, mode) : 0;
                }
            };
        }

        genericSideCache.put(dir, result);
        genericSideCacheComputed.add(dir);
        return result;
    }

    public @Nullable BaseInternalInventory appEngInvForSide(@Nullable Direction dir)
    {
        if (appEngInvComponent == null) return null;
        if (dir == null) return appEngInvComponent.combined();

        // 命中缓存（包括缓存结果为 null）
        if (appEngSideCacheComputed.contains(dir))
        {
            return appEngSideCache.get(dir);
        }

        SidePolicy policy = policies.get(dir);
        @Nullable BaseInternalInventory result;

        if (!policy.allowInsert() && !policy.allowExtract())
        {
            result = null;
        }
        else
        {
            result = new FilteredInternalInventory(appEngInvComponent.combined(), new IAEItemFilter()
            {
                @Override
                public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
                {
                    return policy.allowInsert();
                }

                @Override
                public boolean allowExtract(InternalInventory inv, int slot, int amount)
                {
                    return policy.allowExtract();
                }
            });
        }

        appEngSideCache.put(dir, result);
        appEngSideCacheComputed.add(dir);
        return result;
    }

    @Override
    public void importSettings(MachineContext ctx, DataComponentMap input, @Nullable Player player)
    {
        super.importSettings(ctx, input, player);

        SideConfigField configField = input.get(AECSDataComponents.SIDE_CONFIG_FOR_MEMORY_CARD.get());
        if (configField != null)
        {
            this.autoImport = configField.autoImport();
            this.autoExport = configField.autoExport();
            for (var kv : configField.sidePolicies().entrySet())
            {
                if (kv.getKey() != null && kv.getValue() != null)
                {
                    this.policies.put(kv.getKey(), kv.getValue());
                }
            }

            invalidateAllCaches();
            ctx.host().invalidCap();
        }
    }

    @Override
    public void exportSettings(MachineContext ctx, DataComponentMap.Builder builder, @Nullable Player player)
    {
        super.exportSettings(ctx, builder, player);

        SideConfigField configField = new SideConfigField(this.policies.clone(), this.autoImport, this.autoExport);
        builder.set(AECSDataComponents.SIDE_CONFIG_FOR_MEMORY_CARD, configField);
    }

    @Override
    public void onLoad(MachineContext ctx)
    {
        super.onLoad(ctx);
        invalidateAllCaches();
        ctx.host().invalidCap();
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries)
    {
        tag.putBoolean("side_config_auto_import", this.autoImport);
        tag.putBoolean("side_config_auto_export", this.autoExport);

        CompoundTag polTag = new CompoundTag();
        for (Direction dir : Direction.values())
        {
            SidePolicy policy = this.policies.get(dir);
            if (policy == null) continue;

            polTag.putString(dir.getName(), policy.name());
        }
        tag.put("side_policies", polTag);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries)
    {
        this.autoImport = tag.getBoolean("side_config_auto_import");
        this.autoExport = tag.getBoolean("side_config_auto_export");

        this.policies.clear();

        if (tag.contains("side_policies", CompoundTag.TAG_COMPOUND))
        {
            CompoundTag polTag = tag.getCompound("side_policies");

            for (Direction dir : Direction.values())
            {
                String dirKey = dir.getName();

                if (!polTag.contains(dirKey, CompoundTag.TAG_STRING))
                    continue;

                String name = polTag.getString(dirKey);
                try
                {
                    this.policies.put(dir, SidePolicy.valueOf(name));
                }
                catch (IllegalArgumentException ignored)
                {
                    // 方便以后存档兼容
                }
            }
        }

        SidePolicy def = SidePolicy.ALL;
        for (Direction dir : Direction.values())
        {
            this.policies.putIfAbsent(dir, def);
        }

        // 策略读完后，缓存整体失效
        invalidateAllCaches();
    }
}