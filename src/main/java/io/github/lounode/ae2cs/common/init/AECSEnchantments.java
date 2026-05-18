package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AECSEnchantments
{
    private AECSEnchantments()
    {
    }

    private static final List<Def> DEFINITIONS = new ArrayList<>();

    private record Def(
            ResourceKey<Enchantment> key,
            TagKey<Item> supportedItemsTag,
            String descriptionKey,
            int weight,
            int maxLevel,
            Enchantment.Cost minCost,
            Enchantment.Cost maxCost,
            int anvilCost,
            List<EquipmentSlotGroup> slots,
            boolean showInEnchantingTable
    )
    {
    }

    /**
     * 末影链接
     */
    public static final ResourceKey<Enchantment> ENDER_LINK = defineHiddenMarker(
            "ender_link",
            Tags.Items.TOOLS
    );

    private static ResourceKey<Enchantment> defineHiddenMarker(String path, TagKey<Item> supportedItemsTag)
    {
        return define(
                path,
                supportedItemsTag,
                null,
                1,
                1,
                new Enchantment.Cost(1, 0),
                new Enchantment.Cost(1, 0),
                1,
                List.of(EquipmentSlotGroup.ANY),
                false
        );
    }

    private static ResourceKey<Enchantment> define(
            String path,
            TagKey<Item> supportedItemsTag,
            String descriptionKey,
            int weight,
            int maxLevel,
            Enchantment.Cost minCost,
            Enchantment.Cost maxCost,
            int anvilCost,
            List<EquipmentSlotGroup> slots,
            boolean showInEnchantingTable
    )
    {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, AE2CrystalScience.makeId(path));

        String finalDescKey = (descriptionKey == null || descriptionKey.isBlank())
                ? ("enchantment." + AECSConstants.MODID + "." + path)
                : descriptionKey;

        DEFINITIONS.add(new Def(
                key,
                supportedItemsTag,
                finalDescKey,
                weight,
                maxLevel,
                minCost,
                maxCost,
                anvilCost,
                slots == null ? List.of(EquipmentSlotGroup.ANY) : slots,
                showInEnchantingTable
        ));

        return key;
    }

    public static void bootstrap(BootstrapContext<Enchantment> ctx)
    {
        HolderGetter<Item> items = ctx.lookup(Registries.ITEM);

        for (Def def : DEFINITIONS)
        {
            register(ctx, items, def);
        }
    }

    private static void register(BootstrapContext<Enchantment> ctx, HolderGetter<Item> items, Def def)
    {
        HolderSet<Item> supportedItems = items.getOrThrow(def.supportedItemsTag());

        Optional<HolderSet<Item>> primaryItems = def.showInEnchantingTable()
                ? Optional.empty()
                : Optional.of(emptyPrimary());

        Enchantment enchantment = new Enchantment(
                Component.translatable(def.descriptionKey()),
                new Enchantment.EnchantmentDefinition(
                        supportedItems,
                        primaryItems,
                        def.weight(),
                        def.maxLevel(),
                        def.minCost(),
                        def.maxCost(),
                        def.anvilCost(),
                        def.slots()
                ),
                HolderSet.empty(),
                DataComponentMap.builder().build()
        );

        ctx.register(def.key(), enchantment);
    }

    private static HolderSet<Item> emptyPrimary()
    {
        return HolderSet.direct(List.of());
    }

    public static Identifier id(ResourceKey<Enchantment> key)
    {
        return key.location();
    }
}