package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Objects;

public class AECSItemModelProvider extends ItemModelProvider
{

    public AECSItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        for (DeferredItem<CrystalSeedItem> item : AECSItems.getCrystalSeeds())
        {
            crystalSeedItem(item.get());
        }
        for (DeferredItem<PureCrystalItem> item : AECSItems.getPureCrystal())
        {
            basicItem(item.get());
        }
        for (DeferredItem<? extends Item> item : AECSItems.getTools())
        {
            handheldItem(item.get());
        }
        for (DeferredItem<? extends Item> item : AECSItems.getOthers())
        {
            basicItem(item.get());
        }
    }


    private String getItemName(ItemLike item)
    {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    /**
     * 把非本模组命名空间的纹理标记为“已生成”，从而绕过存在性校验
     */
    private void allowExternalTexture(String path)
    {
        ResourceLocation rl = ResourceLocation.parse(path);
        if (!rl.getNamespace().equals(AECSConstants.MODID))
        {
            this.existingFileHelper.trackGenerated(rl, ModelProvider.TEXTURE);
        }
    }

    public ItemModelBuilder crystalSeedItem(Item item)
    {
        return crystalSeedItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    public ItemModelBuilder crystalSeedItem(ResourceLocation item)
    {
        var main = getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_0"))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(modid, "age"), 0.333f)
                .model(new ModelFile.UncheckedModelFile(
                        ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath() + "_1")))
                .end()
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(modid, "age"), 0.666f)
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
