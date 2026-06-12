package io.github.lounode.ae2cs.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 将 oldState 的可兼容属性值迁移到 newState。
 * 规则：
 * 1) 默认：同名属性 + 值可兼容 -> 迁移
 * 2) 支持属性名映射（oldName -> newName）
 * 3) 只在 newState 支持该属性且该值合法时才会设置
 */
public final class BlockStateAligner {

    private BlockStateAligner() {}

    /**
     * 自动迁移同名属性
     */
    public static @NotNull BlockState align(@NotNull BlockState oldState, @NotNull BlockState newState) {
        return align(oldState, newState, Collections.emptyMap());
    }

    /**
     * 同名属性迁移，如果有nameMapping则会根据mapping规则进行覆盖迁移
     */
    public static @NotNull BlockState align(@NotNull BlockState oldState,
                                            @NotNull BlockState newState,
                                            @NotNull Map<String, String> nameMapping) {
        // 先做同名迁移
        newState = copySameNameProperties(oldState, newState);

        // 再做映射迁移（覆盖同名迁移的结果也没问题，最终都以“合法值”为准）
        if (!nameMapping.isEmpty()) {
            newState = copyMappedProperties(oldState, newState, nameMapping);
        }

        return newState;
    }

    /**
     * 同名属性迁移：old有，new也有，且old的值在new的possibleValues内
     */
    public static @NotNull BlockState copySameNameProperties(@NotNull BlockState oldState, @NotNull BlockState newState) {
        // 建索引：newState 的属性名 -> Property
        Map<String, Property<?>> newPropsByName = indexPropertiesByName(newState);

        BlockState result = newState;
        for (Property<?> oldProp : oldState.getProperties()) {
            Property<?> newProp = newPropsByName.get(oldProp.getName());
            if (newProp == null) {
                continue;
            }
            result = tryCopyValue(oldState, result, oldProp, newProp);
        }
        return result;
    }

    /**
     * 映射属性迁移：oldName -> newName
     */
    public static @NotNull BlockState copyMappedProperties(@NotNull BlockState oldState,
                                                           @NotNull BlockState newState,
                                                           @NotNull Map<String, String> nameMapping) {
        Map<String, Property<?>> oldPropsByName = indexPropertiesByName(oldState);
        Map<String, Property<?>> newPropsByName = indexPropertiesByName(newState);

        BlockState result = newState;
        for (Map.Entry<String, String> e : nameMapping.entrySet()) {
            Property<?> oldProp = oldPropsByName.get(e.getKey());
            Property<?> newProp = newPropsByName.get(e.getValue());
            if (oldProp == null || newProp == null) {
                continue;
            }
            result = tryCopyValue(oldState, result, oldProp, newProp);
        }
        return result;
    }

    private static Map<String, Property<?>> indexPropertiesByName(@NotNull BlockState state) {
        Map<String, Property<?>> map = new HashMap<>();
        for (Property<?> p : state.getProperties()) {
            map.put(p.getName(), p);
        }
        return map;
    }

    /**
     * 尝试把 oldState 的 oldProp 值迁移到 newState 的 newProp 上（仅当类型和值都兼容）。
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static @NotNull BlockState tryCopyValue(@NotNull BlockState oldState,
                                                    @NotNull BlockState newState,
                                                    @NotNull Property<?> oldProp,
                                                    @NotNull Property<?> newProp) {
        // 取旧值（一定存在）
        Comparable oldValue = oldState.getValue((Property) oldProp);

        // 新属性必须允许这个值
        if (!(newProp.getPossibleValues()).contains(oldValue)) {
            return newState;
        }

        try {
            return newState.setValue((Property) newProp, oldValue);
        } catch (Exception ignored) {
            return newState; // 忽略大部分异常
        }
    }
}
