package io.github.lounode.ae2cs.common.item;

import appeng.core.definitions.ItemDefinition;
import appeng.items.materials.MaterialItem;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.ids.AE2CSCreativeTabIds;
import io.github.lounode.ae2cs.api.ids.AE2CSItemIds;
import io.github.lounode.ae2cs.core.AE2CS;
import io.github.lounode.ae2cs.core.AE2CSMainCreativeTab;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class AE2CSItems {

    public static final DeferredRegister.Items DR = DeferredRegister.createItems(AE2CS.MOD_ID);

    // spotless:off
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    ///
    /// MATERIALS
    ///

    public static final ItemDefinition<MaterialItem> PURE_CERTUS_QUARTZ_CRYSTAL = item("Pure Certus Quartz Crystal", AE2CSItemIds.PURE_CERTUS_QUARTZ_CRYSTAL, MaterialItem::new);

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }


    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
                                                   Function<Item.Properties, T> factory) {
        return item(name, id, factory, AE2CSCreativeTabIds.MAIN);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
                                                   Function<Item.Properties, T> factory,
                                                   @Nullable ResourceKey<CreativeModeTab> group) {

        Item.Properties p = new Item.Properties();

        Preconditions.checkArgument(id.getNamespace().equals(AE2CS.MOD_ID), "Can only register for AE2CS");
        var definition = new ItemDefinition<>(name, DR.registerItem(id.getPath(), factory));

        if (Objects.equals(group, AE2CSCreativeTabIds.MAIN)) {
            AE2CSMainCreativeTab.add(definition);
        } else if (group != null) {
            AE2CSMainCreativeTab.add(definition);
            AE2CSMainCreativeTab.addExternal(group, definition);
        }

        ITEMS.add(definition);

        return definition;
    }
}
