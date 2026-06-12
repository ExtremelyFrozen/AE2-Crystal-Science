package io.github.lounode.ae2cs.api;

import net.minecraft.world.item.Item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * 配合注入，用于在Ingredient中检查到某个Item时，新增一些期望的Item，以便动态控制配方，并自动衍生到和配方相关的地方。
 * 注意：这里的规则是是的其内容增加，而非替换！
 */
public class IngredientReplacer {

    /**
     * 关系存储表
     */
    private static final Map<Item, Set<Item>> EXTRA_ITEMS_BY_TARGET = new Reference2ObjectOpenHashMap<>();

    public static void onCommonSetUp() {
        // 我们在这里添加一些自带的替换规则，以便让高纯水晶适应一些没有使用tag写配方的模组
        // 原则上，如果对方模组支持tag我们尽可能使用tag
    }

    public static void setRule(Item target, @NotNull Set<Item> extras) {
        // 确保内部始终是 reference set，避免外部传进来一个普通 HashSet
        var set = new ReferenceOpenHashSet<Item>(extras.size());
        set.addAll(extras);
        EXTRA_ITEMS_BY_TARGET.put(target, set);
    }

    public static void addRule(Item target, @NotNull Set<Item> extras) {
        if (extras.isEmpty()) return;

        EXTRA_ITEMS_BY_TARGET
                .computeIfAbsent(target, k -> new ReferenceOpenHashSet<>())
                .addAll(extras);
    }

    public static void addRule(Item target, @NotNull Item extra) {
        EXTRA_ITEMS_BY_TARGET
                .computeIfAbsent(target, k -> new ReferenceOpenHashSet<>())
                .add(extra);
    }

    public static @Nullable Set<Item> getRule(Item target) {
        return EXTRA_ITEMS_BY_TARGET.get(target);
    }

    public static boolean hasRule(Item target) {
        return EXTRA_ITEMS_BY_TARGET.containsKey(target);
    }

    public static void clearRules() {
        EXTRA_ITEMS_BY_TARGET.clear();
    }

    public static boolean hasAnyRules() {
        return !EXTRA_ITEMS_BY_TARGET.isEmpty();
    }
}
