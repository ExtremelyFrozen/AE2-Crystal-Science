package io.github.lounode.ae2cs.common.machine.component;

import io.github.lounode.ae2cs.api.genericinv.CombinedGenericInternalInventory;
import io.github.lounode.ae2cs.api.genericinv.GenericInvStorageAdapter;
import io.github.lounode.ae2cs.api.genericinv.GenericStackInvWrapper;
import io.github.lounode.ae2cs.api.networking.SideConfigField;
import io.github.lounode.ae2cs.api.util.GenericStackInvHelper;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.PlatformInventoryWrapper;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;

public class SideConfigComponent extends BaseMachineComponent {

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

    private void invalidateAppEngCache(@Nullable Direction dir) {
        if (dir == null) return;
        appEngSideCacheComputed.remove(dir);
        appEngSideCache.remove(dir);
    }

    private void invalidateGenericCache(@Nullable Direction dir) {
        if (dir == null) return;
        genericSideCacheComputed.remove(dir);
        genericSideCache.remove(dir);
    }

    public void invalidateAllCaches() {
        appEngSideCacheComputed.clear();
        appEngSideCache.clear();

        genericSideCacheComputed.clear();
        genericSideCache.clear();
    }

    @Override
    public void onConstruct(MachineComponentContainer container) {
        this.container = container;

        if (container.hasService(GenericStackInvComponent.class))
            genericInvComponent = container.get(GenericStackInvComponent.class);
        if (container.hasService(AppEngInvComponent.class))
            appEngInvComponent = container.get(AppEngInvComponent.class);

        for (var dir : Direction.values()) {
            policies.put(dir, SidePolicy.ALL);
        }

        invalidateAllCaches();

        container.exposeService(SideConfigComponent.class, this);
    }

    public EnumMap<Direction, SidePolicy> getPolicies() {
        return policies;
    }

    public boolean isAutoImport() {
        return autoImport;
    }

    public void setAutoImport(boolean autoImport) {
        if (this.autoImport == autoImport) return;

        // 主动模式不影响能力系统，只需要标脏即可
        this.autoImport = autoImport;
        this.container.host().markChanged();
    }

    public boolean isAutoExport() {
        return autoExport;
    }

    public void setAutoExport(boolean autoExport) {
        if (this.autoExport == autoExport) return;

        // 主动模式不影响能力系统，只需要标脏即可
        this.autoExport = autoExport;
        this.container.host().markChanged();
    }

    public SidePolicy get(Direction dir) {
        return policies.get(dir);
    }

    public void set(Direction dir, SidePolicy policy) {
        var old = policies.put(dir, policy);

        // 仅当实际变化时触发刷新 + 对应缓存失效
        if (old != policy) {
            invalidateAppEngCache(dir);
            invalidateGenericCache(dir);

            this.container.host().markChanged();
            this.container.host().invalidCap();
        }
    }

    public @Nullable GenericInternalInventory genericInvForSide(@Nullable Direction dir) {
        if (genericInvComponent == null) return null;
        if (dir == null) return genericInvComponent.combined();

        // 命中缓存（包括缓存结果为 null）
        if (genericSideCacheComputed.contains(dir)) {
            return genericSideCache.get(dir);
        }

        SidePolicy policy = policies.get(dir);
        @Nullable
        GenericInternalInventory result;

        if (!policy.allowInsert() && !policy.allowExtract()) {
            result = null;
        } else {
            result = new GenericStackInvWrapper(genericInvComponent.combined()) {

                @Override
                public long insert(int slot, AEKey what, long amount, Actionable mode) {
                    return policy.allowInsert() ? super.insert(slot, what, amount, mode) : 0;
                }

                @Override
                public long extract(int slot, AEKey what, long amount, Actionable mode) {
                    if (mode == Actionable.SIMULATE) {
                        return super.extract(slot, what, amount, mode);
                    }
                    return policy.allowExtract() ? super.extract(slot, what, amount, mode) : 0;
                }
            };
        }

        genericSideCache.put(dir, result);
        genericSideCacheComputed.add(dir);
        return result;
    }

    public @Nullable BaseInternalInventory appEngInvForSide(@Nullable Direction dir) {
        if (appEngInvComponent == null) return null;
        if (dir == null) return appEngInvComponent.combined();

        // 命中缓存（包括缓存结果为 null）
        if (appEngSideCacheComputed.contains(dir)) {
            return appEngSideCache.get(dir);
        }

        SidePolicy policy = policies.get(dir);
        @Nullable
        BaseInternalInventory result;

        if (!policy.allowInsert() && !policy.allowExtract()) {
            result = null;
        } else {
            var combined = appEngInvComponent.combined();
            result = new FilteredInternalInventory(combined, new IAEItemFilter() {

                @Override
                public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                    return policy.allowInsert();
                }

                @Override
                public boolean allowExtract(InternalInventory inv, int slot, int amount) {
                    return true;
                }
            }) {

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (simulate) {
                        return combined.extractItem(slot, amount, true);
                    }
                    if (!policy.allowExtract()) {
                        return ItemStack.EMPTY;
                    }
                    return combined.extractItem(slot, amount, false);
                }
            };
        }

        appEngSideCache.put(dir, result);
        appEngSideCacheComputed.add(dir);
        return result;
    }

    /**
     * 实际处理自动输入与自动输出
     */
    @Override
    public void onServerTick(MachineContext ctx) {
        super.onServerTick(ctx);

        Level level = ctx.level();
        BlockPos pos = ctx.pos();

        tickAppEngInv(level, pos);
        tickGenericInv(level, pos);
    }

    private void tickAppEngInv(@NotNull Level level, @NotNull BlockPos pos) {
        if (level.isClientSide || appEngInvComponent == null) return;
        if (!autoImport && !autoExport) return;

        BaseInternalInventory self = appEngInvComponent.combined();

        // 每个方向每tick的最大搬运量
        final int TRANSFER_PER_SIDE = 576;

        for (var kv : policies.entrySet()) {
            Direction dir = kv.getKey();
            SidePolicy policy = kv.getValue();

            boolean doExport = autoExport && policy.allowExtract();
            boolean doImport = autoImport && policy.allowInsert();
            if (!doExport && !doImport) continue;

            // 获取目标位置
            BlockPos otherPos = pos.relative(dir);
            Direction otherSide = dir.getOpposite();

            // 利用AE2对外部进行物品能力进行包装，简化部分操作
            IItemHandler otherItemHandler = null;
            BlockEntity otherBE = level.getBlockEntity(otherPos);
            if (otherBE != null) {
                otherItemHandler = otherBE.getCapability(ForgeCapabilities.ITEM_HANDLER, otherSide).resolve().orElse(null);
            }
            if (otherItemHandler == null) continue;
            PlatformInventoryWrapper otherInv = new PlatformInventoryWrapper(otherItemHandler);

            // 输出
            if (doExport) {
                int remaining = TRANSFER_PER_SIDE;

                for (int slot = 0; slot < self.size() && remaining > 0; slot++) {
                    ItemStack inside = self.getStackInSlot(slot);
                    if (inside.isEmpty()) {
                        continue;
                    }

                    int attempt = Math.min(inside.getCount(), remaining);

                    // 先模拟外部能吃多少
                    ItemStack toTry = inside.copy();
                    toTry.setCount(attempt);

                    ItemStack overflowSim = otherInv.addItems(toTry, true);
                    int accepted = attempt - overflowSim.getCount();
                    if (accepted <= 0) {
                        continue;
                    }

                    // 真正抽出accepted，再塞进外部
                    ItemStack extracted = self.extractItem(slot, accepted, false);
                    if (extracted.isEmpty()) {
                        continue;
                    }

                    ItemStack overflow = otherInv.addItems(extracted, false);

                    // 保底回插
                    if (!overflow.isEmpty()) {
                        self.addItems(overflow, false);
                    }
                    remaining -= accepted;
                }
            }

            // 输入
            if (doImport) {
                int remaining = TRANSFER_PER_SIDE;

                for (int slot = 0; slot < otherInv.size() && remaining > 0; slot++) {
                    ItemStack out = otherInv.getStackInSlot(slot);
                    if (out.isEmpty()) {
                        continue;
                    }

                    int attempt = Math.min(out.getCount(), remaining);

                    // 先模拟内部能吃多少
                    ItemStack toTry = out.copy();
                    toTry.setCount(attempt);

                    ItemStack overflowSim = self.addItems(toTry, true);
                    int accepted = attempt - overflowSim.getCount();
                    if (accepted <= 0) {
                        continue;
                    }

                    // 真正从外部抽出accepted，塞进内部
                    ItemStack extracted = otherInv.extractItem(slot, accepted, false);
                    if (extracted.isEmpty()) {
                        continue;
                    }

                    ItemStack overflow = self.addItems(extracted, false);

                    // 保底回插
                    if (!overflow.isEmpty()) {
                        otherInv.addItems(overflow, false);
                    }

                    remaining -= accepted;
                }
            }
        }
    }

    private void tickGenericInv(@NotNull Level level, @NotNull BlockPos pos) {
        if (level.isClientSide || genericInvComponent == null) return;
        if (!autoImport && !autoExport) return;

        CombinedGenericInternalInventory original = genericInvComponent.combined();
        // 同时实现 MEStorage+GenericInternalInventory用于批量插拔更方便
        GenericInvStorageAdapter self = new GenericInvStorageAdapter(original);

        final long TRANSFER_PER_SIDE = 576L;
        IActionSource source = IActionSource.empty();

        for (var kv : policies.entrySet()) {
            Direction dir = kv.getKey();
            SidePolicy policy = kv.getValue();

            boolean doExport = autoExport && policy.allowExtract();
            boolean doImport = autoImport && policy.allowInsert();
            if (!doExport && !doImport) continue;

            BlockPos otherPos = pos.relative(dir);
            Direction otherSide = dir.getOpposite();
            BlockEntity otherBe = level.getBlockEntity(otherPos);

            // 遍历优先使用 GenericInternalInventory
            GenericInternalInventory otherInv = GenericStackInvHelper.getGenericInternalInv(level, otherPos, otherBe, otherSide);

            // 对于普通仓库使用MEStorage包装（getBetterInteractMEStorage只取包装能力，不直接取MEStorage能力，防止从ME接口直接拿到整个网络存储）
            MEStorage otherStorage = GenericStackInvHelper.getBetterInteractMEStorage(level, otherPos, otherSide);

            if (otherInv == null && otherStorage == null) {
                continue;
            }

            // 输出
            if (doExport) {
                long remaining = TRANSFER_PER_SIDE;

                // 如果有Inv，则只操作Inv
                if (otherInv != null) {
                    for (int slot = 0; slot < self.size() && remaining > 0; slot++) {
                        GenericStack inside = self.getStack(slot);
                        if (inside == null || inside.amount() <= 0) continue;

                        AEKey what = inside.what();
                        long attempt = Math.min(inside.amount(), remaining);

                        // 先模拟目标最大接受量
                        long accepted = GenericStackInvHelper.simulateInsertIntoInv(otherInv, what, attempt);
                        if (accepted <= 0) continue;

                        // 从self槽位抽出accepted
                        long extracted = self.extract(slot, what, accepted, Actionable.MODULATE);
                        if (extracted <= 0) continue;

                        // 塞进目标
                        long inserted = GenericStackInvHelper.insertIntoInv(otherInv, what, extracted, Actionable.MODULATE);

                        // 保底回插到
                        long overflow = extracted - inserted;
                        if (overflow > 0) {
                            GenericStackInvHelper.reinsertToInvPreferSlot(self, slot, what, overflow);
                        }

                        remaining -= inserted;
                    }
                } else // 其次尝试Storage
                {
                    for (int slot = 0; slot < self.size() && remaining > 0; slot++) {
                        GenericStack in = self.getStack(slot);
                        if (in == null || in.amount() <= 0) continue;

                        AEKey what = in.what();
                        long attempt = Math.min(in.amount(), remaining);

                        long accepted = otherStorage.insert(what, attempt, Actionable.SIMULATE, source);
                        if (accepted <= 0) continue;

                        long extracted = self.extract(slot, what, accepted, Actionable.MODULATE);
                        if (extracted <= 0) continue;

                        long inserted = otherStorage.insert(what, extracted, Actionable.MODULATE, source);

                        long overflow = extracted - inserted;
                        if (overflow > 0) {
                            GenericStackInvHelper.reinsertToInvPreferSlot(self, slot, what, overflow);
                        }

                        remaining -= inserted;
                    }
                }
            }

            // 输入
            if (doImport) {
                long remaining = TRANSFER_PER_SIDE;

                // 优先Inv
                if (otherInv != null) {
                    for (int s = 0; s < otherInv.size() && remaining > 0; s++) {
                        GenericStack out = otherInv.getStack(s);
                        if (out == null || out.amount() <= 0) continue;

                        AEKey what = out.what();
                        long attempt = Math.min(out.amount(), remaining);

                        // 先模拟self容量
                        long accepted = self.insert(what, attempt, Actionable.SIMULATE, source);
                        if (accepted <= 0) continue;

                        // 真正从外部槽位抽出
                        long extracted = otherInv.extract(s, what, accepted, Actionable.MODULATE);
                        if (extracted <= 0) continue;

                        // 真正塞进self
                        long inserted = self.insert(what, extracted, Actionable.MODULATE, source);

                        // 保底回插
                        long overflow = extracted - inserted;
                        if (overflow > 0) {
                            GenericStackInvHelper.reinsertToInvPreferSlot(otherInv, s, what, overflow);
                        }

                        remaining -= inserted;
                    }
                } else // 无Inv，使用Storage
                {
                    KeyCounter counter = otherStorage.getAvailableStacks();
                    for (AEKey what : counter.keySet()) {
                        if (remaining <= 0) break;

                        long available = counter.get(what);
                        if (available <= 0) continue;

                        long attempt = Math.min(available, remaining);

                        long accepted = self.insert(what, attempt, Actionable.SIMULATE, source);
                        if (accepted <= 0) continue;

                        long extracted = otherStorage.extract(what, accepted, Actionable.MODULATE, source);
                        if (extracted <= 0) continue;

                        long inserted = self.insert(what, extracted, Actionable.MODULATE, source);

                        long overflow = extracted - inserted;
                        if (overflow > 0) {
                            otherStorage.insert(what, overflow, Actionable.MODULATE, source);
                        }

                        remaining -= inserted;
                    }
                }
            }
        }
    }

    @Override
    public void importSettings(MachineContext ctx, CompoundTag input, @Nullable Player player) {
        super.importSettings(ctx, input, player);

        SideConfigField configField = AECSDataComponents.getSideConfigForMemoryCard(input);
        if (configField != null) {
            this.autoImport = configField.autoImport();
            this.autoExport = configField.autoExport();
            for (var kv : configField.sidePolicies().entrySet()) {
                if (kv.getKey() != null && kv.getValue() != null) {
                    this.policies.put(kv.getKey(), kv.getValue());
                }
            }

            invalidateAllCaches();
            ctx.host().invalidCap();
        }
    }

    @Override
    public void exportSettings(MachineContext ctx, CompoundTag builder, @Nullable Player player) {
        super.exportSettings(ctx, builder, player);

        SideConfigField configField = new SideConfigField(this.policies.clone(), this.autoImport, this.autoExport);
        AECSDataComponents.setSideConfigForMemoryCard(builder, configField);
    }

    @Override
    public void onLoad(MachineContext ctx) {
        super.onLoad(ctx);
        invalidateAllCaches();
        ctx.host().invalidCap();
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putBoolean("side_config_auto_import", this.autoImport);
        tag.putBoolean("side_config_auto_export", this.autoExport);

        CompoundTag polTag = new CompoundTag();
        for (Direction dir : Direction.values()) {
            SidePolicy policy = this.policies.get(dir);
            if (policy == null) continue;

            polTag.putString(dir.getName(), policy.name());
        }
        tag.put("side_policies", polTag);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        this.autoImport = tag.getBoolean("side_config_auto_import");
        this.autoExport = tag.getBoolean("side_config_auto_export");

        this.policies.clear();

        if (tag.contains("side_policies", CompoundTag.TAG_COMPOUND)) {
            CompoundTag polTag = tag.getCompound("side_policies");

            for (Direction dir : Direction.values()) {
                String dirKey = dir.getName();

                if (!polTag.contains(dirKey, CompoundTag.TAG_STRING))
                    continue;

                String name = polTag.getString(dirKey);
                try {
                    this.policies.put(dir, SidePolicy.valueOf(name));
                } catch (IllegalArgumentException ignored) {
                    // 方便以后存档兼容
                }
            }
        }

        SidePolicy def = SidePolicy.ALL;
        for (Direction dir : Direction.values()) {
            this.policies.putIfAbsent(dir, def);
        }

        // 策略读完后，缓存整体失效
        invalidateAllCaches();
    }
}
