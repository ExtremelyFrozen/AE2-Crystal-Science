package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
        for (RegistryObject<CrystalSeedItem> item : AECSItems.getCrystalSeeds())
        {
            crystalSeedItem(item.get());
        }
        for (RegistryObject<PureCrystalItem> item : AECSItems.getPureCrystal())
        {
            basicItem(item.get());
        }
        for (RegistryObject<? extends Item> item : AECSItems.getOthers())
        {
            basicItem(item.get());
        }
    }


    private String getItemName(ItemLike item)
    {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }

    /**
     * 把非本模组命名空间的纹理标记为“已生成”，从而绕过存在性校验
     */
    private void allowExternalTexture(String path)
    {
        ResourceLocation rl = ResourceLocation.tryParse(path);
        if (!rl.getNamespace().equals(AECSConstants.MODID))
        {
            this.existingFileHelper.trackGenerated(rl, ModelProvider.TEXTURE);
        }
    }

    public ItemModelBuilder crystalSeedItem(Item item)
    {
        return crystalSeedItem(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    public ItemModelBuilder crystalSeedItem(ResourceLocation item)
    {
        var main = getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.tryBuild(item.getNamespace(), "item/" + item.getPath() + "_0"))
                .override()
                .predicate(ResourceLocation.tryBuild(modid, "age"), 0.333f)
                .model(new ModelFile.UncheckedModelFile(
                        ResourceLocation.tryBuild(item.getNamespace(), "item/" + item.getPath() + "_1")))
                .end()
                .override()
                .predicate(ResourceLocation.tryBuild(modid, "age"), 0.666f)
                .model(new ModelFile.UncheckedModelFile(
                        ResourceLocation.tryBuild(item.getNamespace(), "item/" + item.getPath() + "_2")))
                .end();
        var age333 = getBuilder(item + "_1")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.tryBuild(item.getNamespace(), "item/" + item.getPath() + "_1"));
        var age666 = getBuilder(item + "_2")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.tryBuild(item.getNamespace(), "item/" + item.getPath() + "_2"));
        return main;
    }

}
