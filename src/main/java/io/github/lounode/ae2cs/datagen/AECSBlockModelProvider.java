package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class AECSBlockModelProvider extends BlockModelProvider
{
    public AECSBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
    }

    /**
     * 生成便携元件的模型
     *
     * @param texture 纹理
     */
    protected BlockModelBuilder driveBlockModel(String name, String texture)
    {
        // 让 EFH 放行校验
        allowExternalModel("ae2:block/drive/drive_cell");
        allowExternalTexture(texture);

        return withExistingParent(name, ResourceLocation.tryParse("ae2:block/drive/drive_cell"))
                .texture("cell", texture);
    }

    private String getItemName(ItemLike item)
    {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }

    private void allowExternalModel(String path)
    {
        ResourceLocation rl = ResourceLocation.tryParse(path);
        if (!rl.getNamespace().equals(AECSConstants.MODID))
        {
            this.existingFileHelper.trackGenerated(rl, ModelProvider.MODEL); // 注意这里是 MODEL
        }
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

}
