package io.github.lounode.ae2cs.api.util;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import com.google.common.util.concurrent.Runnables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class GenericStackInvHelper
{
    public static boolean hasAtLeastAnEmptySlot(GenericInternalInventory inv)
    {
        for (int i = 0; i < inv.size(); i++)
        {
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
    public static MEStorage getAdjacentMeStorage(Level level, BlockPos pos, @Nullable BlockEntity be, Direction side)
    {
        MEStorage storage;
        if (be != null)
        {
            storage = level.getCapability(AECapabilities.ME_STORAGE, be.getBlockPos(), be.getBlockState(), be, side);
        }
        else
        {
            storage = level.getCapability(AECapabilities.ME_STORAGE, pos, side);
        }
        if (storage != null)
        {
            return storage;
        }

        // 否则使用外部存储构建能力
        if (!(level instanceof ServerLevel serverLevel))
        {
            return null;
        }
        Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors.createExternalStorageStrategies(serverLevel, pos, side);
        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet())
        {
            MEStorage wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
            if (wrapper != null)
            {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (externalStorages.isEmpty())
        {
            return null;
        }
        if (externalStorages.size() == 1)
        {
            return externalStorages.values().iterator().next();
        }
        return new CompositeStorage(externalStorages);
    }
}
