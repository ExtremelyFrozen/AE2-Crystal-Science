package io.github.lounode.ae2cs.datagen;

import appeng.block.misc.VibrationChamberBlock;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import static io.github.lounode.ae2cs.AE2CrystalScience.makeId;

public class AECSBlockStateProvider extends AE2BlockStateProvider
{
    public AECSBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, AECSConstants.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        /*
        Block chamber = AE2CrystalSeedsBlocks.crystalGrowthChamber;
        var key = BuiltInRegistries.BLOCK.getKey(chamber);

        String modelPath = key.getPath();
        ModelFile normalModel = models().cube(modelPath,
                modLoc("block/" + modelPath + "_bottom"),
                modLoc("block/" + modelPath + "_top"),
                modLoc("block/" + modelPath + "_front"),
                modLoc("block/" + modelPath + "_back"),
                modLoc("block/" + modelPath + "_side_left"),
                modLoc("block/" + modelPath + "_side_right")
        ).texture("particle", modLoc("block/" + modelPath + "_front"));
        horizontalBlock(chamber, machine(modelPath));

         */

        //machine(AE2CrystalSeedsBlocks.circuitEtcher);
        //machine(AE2CrystalSeedsBlocks.crystalVibrationChamber);
        machine(AECSBlocks.crystalGrowthChamber.get());
        crystalVibrationChamber();
        //machine(AE2CrystalSeedsBlocks.quartzGrindstone);
        //machine(AE2CrystalSeedsBlocks.crusher);
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

    private void blockWithItem(DeferredBlock<? extends Block> deferredBlock)
    {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }


    private void crystalVibrationChamber()
    {
        var offModel = models().cube(
                modelPath(AEBlocks.VIBRATION_CHAMBER),
                makeId("block/crystal_vibration_chamber_bottom"),
                makeId("block/crystal_vibration_chamber_top"),
                makeId("block/crystal_vibration_chamber_front"),
                makeId("block/crystal_vibration_chamber_back"),
                makeId("block/crystal_vibration_chamber_side"),
                makeId("block/crystal_vibration_chamber_side")).texture("particle", makeId("block/crystal_vibration_chamber_front"));
        var onModel = models().cube(
                modelPath(AEBlocks.VIBRATION_CHAMBER) + "_on",
                makeId("block/crystal_vibration_chamber_bottom"),
                makeId("block/crystal_vibration_chamber_top_on"),
                makeId("block/crystal_vibration_chamber_front_on"),
                makeId("block/crystal_vibration_chamber_back_on"),
                makeId("block/crystal_vibration_chamber_side"),
                makeId("block/crystal_vibration_chamber_side")).texture("particle", makeId("block/crystal_vibration_chamber_front_on"));

        multiVariantGenerator(AEBlocks.VIBRATION_CHAMBER)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(VibrationChamberBlock.ACTIVE)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));

        itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel.getLocation());
    }

    private String modelPath(BlockDefinition<?> block)
    {
        return block.id().getPath();
    }

    public void machine(Block block)
    {
        horizontalBlock(block, machine(BuiltInRegistries.BLOCK.getKey(block).getPath()));
    }

    public ModelFile machine(String name)
    {
        return models().cube(name,
                modLoc("block/" + name + "_bottom"),
                modLoc("block/" + name + "_top"),
                modLoc("block/" + name + "_front"),
                modLoc("block/" + name + "_back"),
                modLoc("block/" + name + "_side_left"),
                modLoc("block/" + name + "_side_right")
        ).texture("particle", modLoc("block/" + name + "_front"));
    }
}
