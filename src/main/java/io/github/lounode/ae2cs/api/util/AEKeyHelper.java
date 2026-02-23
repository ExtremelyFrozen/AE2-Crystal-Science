package io.github.lounode.ae2cs.api.util;

import appeng.api.stacks.AEKey;
import io.github.lounode.ae2cs.AE2CrystalScience;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public class AEKeyHelper
{
    /**
     * 将 Object2LongMap<AEKey> 序列化到 data[name]（ListTag，每个元素是 CompoundTag）。
     */
    public static void writeKeyAmountMap(CompoundTag data, String name,
                                         Object2LongMap<AEKey> map)
    {
        var list = new ListTag();

        for (var e : map.object2LongEntrySet())
        {
            long amount = e.getLongValue();
            if (amount <= 0) continue;

            CompoundTag entry = new CompoundTag();
            CompoundTag keyTag;
            try
            {
                keyTag = e.getKey().toTagGeneric();
            }
            catch (Throwable ex)
            {
                AE2CrystalScience.LOGGER.error("failed to write key amount map", ex);
                continue; // 如果发生异常，则打印错误后继续
            }

            if (keyTag != null)
            {
                entry.put("key", keyTag);
                entry.putLong("amount", amount);
                list.add(entry);
            }
        }

        data.put(name, list);
    }

    /**
     * 从 data[name] 反序列化到 target（会先 clear）
     */
    public static void readKeyAmountMap(CompoundTag data, String name,
                                        Object2LongMap<AEKey> target)
    {
        target.clear();

        if (!data.contains(name, Tag.TAG_LIST))
        {
            return;
        }

        var list = data.getList(name, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            var entry = list.getCompound(i);

            long amount = entry.getLong("amount");
            if (amount <= 0) continue;

            // key 为空或解析失败就跳过
            if (!entry.contains("key", Tag.TAG_COMPOUND)) continue;
            var key = AEKey.fromTagGeneric(entry.getCompound("key"));
            if (key != null)
            {
                target.put(key, amount);
            }
        }
    }

    /**
     * 读取并返回一个新 map
     */
    public static @NotNull Object2LongOpenHashMap<AEKey> readKeyAmountMapNew(CompoundTag data, String name)
    {
        var map = new Object2LongOpenHashMap<AEKey>();
        readKeyAmountMap(data, name, map);
        return map;
    }
}
