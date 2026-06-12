package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 这个类我们在1.20.1版本使用，1.21.1版本中无用，此处仅留存文件，以便随时启用
 */
public class AECSBlockModelProvider extends BlockModelProvider {

    public AECSBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {}

    /**
     * 生成便携元件的模型
     *
     * @param texture 纹理
     */
    protected BlockModelBuilder driveBlockModel(String name, String texture) {
        // 让 EFH 放行校验
        allowExternalModel("ae2:block/drive/drive_cell");
        allowExternalTexture(texture);

        return withExistingParent(name, ResourceLocation.parse("ae2:block/drive/drive_cell"))
                .texture("cell", texture);
    }

    private String getItemName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    private void allowExternalModel(String path) {
        ResourceLocation rl = ResourceLocation.parse(path);
        if (!rl.getNamespace().equals(AECSConstants.MODID)) {
            this.existingFileHelper.trackGenerated(rl, ModelProvider.MODEL); // 注意这里是 MODEL
        }
    }

    /**
     * 把非本模组命名空间的纹理标记为“已生成”，从而绕过存在性校验
     */
    private void allowExternalTexture(String path) {
        ResourceLocation rl = ResourceLocation.parse(path);
        if (!rl.getNamespace().equals(AECSConstants.MODID)) {
            this.existingFileHelper.trackGenerated(rl, ModelProvider.TEXTURE);
        }
    }
}
