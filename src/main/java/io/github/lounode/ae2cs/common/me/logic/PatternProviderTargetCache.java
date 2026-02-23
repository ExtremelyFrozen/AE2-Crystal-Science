package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.BlockApiCache;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

// 复制自AE原版类
class PatternProviderTargetCache
{
    private final BlockApiCache<MEStorage> cache;
    private final Direction direction;
    private final IActionSource src;
    private final Map<AEKeyType, ExternalStorageStrategy> strategies;

    PatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src)
    {
        this.cache = BlockApiCache.create(Capabilities.STORAGE, l, pos);
        this.direction = direction;
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
    }

    @Nullable PatternProviderTarget find()
    {
        MEStorage meStorage = (MEStorage) this.cache.find(this.direction);
        if (meStorage != null)
        {
            return this.wrapMeStorage(meStorage);
        }
        else
        {
            IdentityHashMap<AEKeyType, MEStorage> externalStorages = new IdentityHashMap<>(2);

            for (Map.Entry<AEKeyType, ExternalStorageStrategy> entry : this.strategies.entrySet())
            {
                MEStorage wrapper = (entry.getValue()).createWrapper(false, () -> {
                });
                if (wrapper != null)
                {
                    externalStorages.put((AEKeyType) entry.getKey(), wrapper);
                }
            }

            if (!externalStorages.isEmpty())
            {
                return this.wrapMeStorage(new CompositeStorage(externalStorages));
            }
            else
            {
                return null;
            }
        }
    }

    private PatternProviderTarget wrapMeStorage(final MEStorage storage)
    {
        return new PatternProviderTarget()
        {
            public long insert(AEKey what, long amount, Actionable type)
            {
                return storage.insert(what, amount, type, PatternProviderTargetCache.this.src);
            }

            public boolean containsPatternInput(Set<AEKey> patternInputs)
            {
                for (Object2LongMap.Entry<AEKey> stack : storage.getAvailableStacks())
                {
                    if (patternInputs.contains((stack.getKey()).dropSecondary()))
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
