package io.github.lounode.ae2cs.integration.dataenergistics.patternprovider;

import appeng.api.stacks.AEItemKey;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderDispatch;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderProfile;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderRegistration;
import com.fish_dan_.data_energistics.api.registry.adaptive.AdaptivePatternProviderToolbarAction;
import io.github.lounode.ae2cs.AE2CrystalScience;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.function.Predicate;

/** AECS-owned adaptive declarations built exclusively from the public DE registration API. */
public final class CrystalScienceProviderDefinitions {

    private CrystalScienceProviderDefinitions() {}

    public static AdaptivePatternProviderRegistration fixed(String path, Predicate<ItemStack> matcher,
                                                            int slots, ObjectSet<ResourceLocation> capabilities, ResourceLocation... actions) {
        return fixed(path, matcher, slots, capabilities, context -> false, actions);
    }

    public static AdaptivePatternProviderRegistration fixed(String path, Predicate<ItemStack> matcher,
                                                            int slots, ObjectSet<ResourceLocation> capabilities, AdaptivePatternProviderDispatch dispatch,
                                                            ResourceLocation... actions) {
        var toolbar = new ObjectArrayList<AdaptivePatternProviderToolbarAction>(actions.length);
        for (var action : actions) {
            toolbar.add(new AdaptivePatternProviderToolbarAction(action));
        }
        // Keep the existing definition identity so pending route state follows the same provider after the move.
        return new AdaptivePatternProviderRegistration(
                AE2CrystalScience.parseOrMakeId("data_energistics:adaptive_pattern_provider/" + path),
                stack -> {
                    if (!matcher.test(stack)) {
                        return null;
                    }
                    var icon = stack.copyWithCount(1);
                    return new AdaptivePatternProviderProfile(slots, icon, AEItemKey.of(icon.getItem()),
                            icon.getHoverName(), capabilities);
                }, dispatch, toolbar);
    }

    public static ObjectSet<ResourceLocation> capabilities(ResourceLocation... capabilities) {
        return new ObjectOpenHashSet<>(Arrays.asList(capabilities));
    }

    public static Predicate<ItemStack> itemIds(String... ids) {
        ObjectSet<ResourceLocation> items = new ObjectOpenHashSet<>();
        for (String id : ids) {
            items.add(AE2CrystalScience.parseOrMakeId(id));
        }
        return stack -> items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
