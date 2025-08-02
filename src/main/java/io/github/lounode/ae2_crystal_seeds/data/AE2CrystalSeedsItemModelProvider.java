package io.github.lounode.ae2_crystal_seeds.data;

import com.google.gson.JsonElement;
import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import io.github.lounode.ae2_crystal_seeds.common.item.AE2CrystalSeedsItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static io.github.lounode.ae2_crystal_seeds.common.item.AE2CrystalSeedsItems.*;

public class AE2CrystalSeedsItemModelProvider extends ItemModelProvider {



    public AE2CrystalSeedsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AE2CrystalSeedsAPI.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Set<Item> items = BuiltInRegistries.ITEM.stream()
                .filter(i -> AE2CrystalSeedsAPI.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                .collect(Collectors.toSet());

        registerItemOverrides(items);

        for (var item : items) {
            if (item instanceof BlockItem blockItem) {
                simpleBlockItem(blockItem.getBlock());
                continue;
            }
            basicItem(item);
        }
    }

    private void registerItemOverrides(Set<Item> items) {
        crystalSeedItem(certusQuartzSeed);
        items.remove(certusQuartzSeed);

        crystalSeedItem(fluixCrystalSeed);
        items.remove(fluixCrystalSeed);

        crystalSeedItem(netherQuartzSeed);
        items.remove(netherQuartzSeed);

        crystalSeedItem(entroCrystalSeed);
        items.remove(entroCrystalSeed);
    }

    public ItemModelBuilder crystalSeedItem(Item item) {
        return crystalSeedItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    public ItemModelBuilder crystalSeedItem(ResourceLocation item) {
        var main = getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_0"))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(modid,"age"), 0.333f)
                .model(new ModelFile.UncheckedModelFile(
                        ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_1")))
                .end()
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(modid,"age"), 0.666f)
                .model(new ModelFile.UncheckedModelFile(
                        ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_2")))
                .end();
        var age333 = getBuilder(item + "_1")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_1"));
        var age666 = getBuilder(item + "_2")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_2"));
        return main;
    }
}
