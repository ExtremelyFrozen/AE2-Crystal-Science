package io.github.lounode.ae2cs.api.util;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.common.util.concurrent.Runnables;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class GenericStackInvHelper {

    public static boolean hasAtLeastAnEmptySlot(GenericInternalInventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i) == null)
                return true;
        }
        return false;
    }

    /**
     * 获取相邻方块在给定 side 上暴露出来的 MEStorage。
     * <p>
     * 1) 优先尝试 AECapabilities.ME_STORAGE（可覆盖任意 channel）
     * 2) 否则使用 AE2 的外部存储策略，将平台能力包装为 MEStorage
     * <p>
     * 注：改自{@link PatternProviderTarget}
     */
    @Nullable
    public static MEStorage getAdjacentMeStorage(Level level, BlockPos pos, @Nullable BlockEntity be, Direction side) {
        MEStorage storage;
        if (be != null) {
            storage = level.getCapability(AECapabilities.ME_STORAGE, be.getBlockPos(), be.getBlockState(), be, side);
        } else {
            storage = level.getCapability(AECapabilities.ME_STORAGE, pos, side);
        }
        if (storage != null) {
            return storage;
        }

        // 否则使用外部存储构建能力
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors.createExternalStorageStrategies(serverLevel, pos, side);
        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet()) {
            MEStorage wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
            if (wrapper != null) {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (externalStorages.isEmpty()) {
            return null;
        }
        if (externalStorages.size() == 1) {
            return externalStorages.values().iterator().next();
        }
        return new CompositeStorage(externalStorages);
    }

    /**
     * 直接使用能力获取GenericInternalInventory
     */
    @Nullable
    public static GenericInternalInventory getGenericInternalInv(Level level, BlockPos pos, @Nullable BlockEntity be, Direction side) {
        if (be != null) {
            return level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, be.getBlockPos(), be.getBlockState(), be, side);
        } else {
            return level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, side);
        }
    }

    /**
     * 仅获取包装能力的MEStorage，适用于那些不希望不小心直接与整个AE存储进行交互的行为
     */
    @Nullable
    public static MEStorage getBetterInteractMEStorage(Level level, BlockPos pos, Direction side) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors.createExternalStorageStrategies(serverLevel, pos, side);
        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet()) {
            MEStorage wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
            if (wrapper != null) {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (externalStorages.isEmpty()) {
            return null;
        }
        if (externalStorages.size() == 1) {
            return externalStorages.values().iterator().next();
        }
        return new CompositeStorage(externalStorages);
    }

    /**
     * 模拟把key+amount塞进inv（跨槽），返回插入量
     */
    public static long simulateInsertIntoInv(GenericInternalInventory inv, AEKey what, long amount) {
        return insertIntoInv(inv, what, amount, Actionable.SIMULATE);
    }

    /**
     * 把key+amount塞进inv（跨槽），返回插入量
     */
    public static long insertIntoInv(GenericInternalInventory inv, AEKey what, long amount, Actionable mode) {
        if (amount <= 0 || !inv.canInsert() || !inv.isSupportedType(what)) {
            return 0;
        }

        long inserted = 0;
        for (int slot = 0; slot < inv.size() && inserted < amount; slot++) {
            if (!inv.isAllowedIn(slot, what)) {
                continue;
            }

            long delta = inv.insert(slot, what, amount - inserted, mode);
            if (delta > 0) {
                inserted += delta;
            }
        }
        return inserted;
    }

    /**
     * 将剩余量优先回插到指定槽位（通常是刚刚抽出的槽位），再尝试回插到其它槽位，
     * 用于保底回滚
     */
    public static void reinsertToInvPreferSlot(GenericInternalInventory inv, int preferredSlot, AEKey what, long amount) {
        long remaining = amount;
        if (remaining <= 0 || !inv.canInsert() || !inv.isSupportedType(what)) {
            return;
        }

        // 优先回插原槽位
        if (preferredSlot >= 0 && preferredSlot < inv.size() && inv.isAllowedIn(preferredSlot, what)) {
            long back = inv.insert(preferredSlot, what, remaining, Actionable.MODULATE);
            remaining -= back;
        }

        // 如果还有剩余，插入别的槽位
        if (remaining > 0) {
            insertIntoInv(inv, what, remaining, Actionable.MODULATE);
        }
    }
}
