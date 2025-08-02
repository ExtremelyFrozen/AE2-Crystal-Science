package io.github.lounode.ae2_crystal_seeds.data;

import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import io.github.lounode.ae2_crystal_seeds.common.block.AE2CrystalSeedsBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AE2CrystalSeedsBlockStateProvider extends BlockStateProvider {
    public AE2CrystalSeedsBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AE2CrystalSeedsAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Block chamber = AE2CrystalSeedsBlocks.crystalGrowthChamber;
        var key = BuiltInRegistries.BLOCK.getKey(chamber);

        String modelPath = key.getPath();
        ModelFile normalModel = models().orientableWithBottom(modelPath,
                modLoc("block/" + modelPath + "_side"),
                modLoc("block/" + modelPath + "_front"),
                modLoc("block/" + modelPath + "_bottom"),
                modLoc("block/" + modelPath + "_top")
        ).texture("back", modLoc("block/" + modelPath + "_back"));

        /*
        ModelFile litModel = models().cube(modelPath,
                modLoc(modelPath + "_down" + "_lit"),
                modLoc(modelPath + "_up" + "_lit"),
                modLoc(modelPath + "_north" + "_lit"),
                modLoc(modelPath + "_south" + "_lit"),
                modLoc(modelPath + "_east" + "_lit"),
                modLoc(modelPath + "_west" + "_lit")
        );

         */

        horizontalBlock(chamber, normalModel);

        /*
        getVariantBuilder(chamber)
                .forAllStates(state -> {
                    boolean lit = state.getValue(BlockStateProperties.LIT);
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

                    return ConfiguredModel.builder()
                            .modelFile(lit ? litModel : normalModel)
                            .rotationY((int) facing.getOpposite().toYRot())
                            .build();
                });

         */
    }
}
