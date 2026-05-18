package io.github.lounode.ae2cs.api.util;

import appeng.api.stacks.AEKey;
import com.mojang.serialization.Codec;
import io.github.lounode.ae2cs.AE2CrystalScience;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AEKeyHelper
{
    public static final Codec<Map<AEKey, Long>> AEKEY_LONG_MAP_CODEC = Codec.unboundedMap(AEKey.CODEC, Codec.LONG);

    /**
     * 将 Object2LongMap<AEKey> 序列化到 data[name]（ListTag，每个元素是 CompoundTag）。
     */
    public static void writeKeyAmountMap(ValueOutput output, String name, Object2LongMap<AEKey> map)
    {
        output.store(name,AEKEY_LONG_MAP_CODEC,map);
    }

    /**
     * 从 data[name] 反序列化到 target（会先 clear）
     */
    public static void readKeyAmountMap(ValueInput input, String name, Object2LongMap<AEKey> target)
    {
        target.clear();

        input.read(name, AEKEY_LONG_MAP_CODEC).ifPresent(target::putAll);
    }

    /**
     * 读取并返回一个新 map
     */
    public static @NotNull Object2LongOpenHashMap<AEKey> readKeyAmountMapNew(ValueInput input, String name)
    {
        var map = new Object2LongOpenHashMap<AEKey>();
        readKeyAmountMap(input, name, map);
        return map;
    }
}
