package io.github.lounode.ae2cs.datagen;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import com.google.gson.JsonPrimitive;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import static io.github.lounode.ae2cs.AE2CrystalScience.makeId;

public class AECSBlockStateProvider extends BlockStateProvider
{
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>("ae2:z",
            r -> new JsonPrimitive(r.ordinal() * 90));

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
        genPatternProviderLike(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get(), "block/integrated_interface");
        genPatternProviderLike(AECSBlocks.METEORITE_CRAFTER_BLOCK.get(), "block/meteorite_crafter");
        machine(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get());
        //crystalVibrationChamber();
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

        multiVariantGenerator(AEBlocks.VIBRATION_CHAMBER.block())
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(VibrationChamberBlock.ACTIVE)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));

        itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel.getLocation());
    }

    /**
     * @param block    类似样板供应器的方块
     * @param basePath 如：block/integrated_interface，随后你需要保证 block/integrated_interface/ 中包含其贴图
     *                 （base、alternate、alternate_front、alternate_arrow）
     */
    private void genPatternProviderLike(Block block, String basePath)
    {
        // 普通 cubeAll 模型（所有面同贴图）
        ModelFile normalModel = cubeAllWithTexture(block, AE2CrystalScience.makeId(basePath + "/base"));
        simpleBlockItem(block, normalModel);

        // 自动生成 oriented 模型
        ModelFile orientedModel = models()
                .withExistingParent(basePath + "_oriented", mcLoc("block/cube"))
                .texture("particle", AE2CrystalScience.makeId(basePath + "/base"))
                .texture("down", AE2CrystalScience.makeId(basePath + "/alternate"))
                .texture("up", AE2CrystalScience.makeId(basePath + "/alternate_front"))
                .texture("north", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("south", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("east", AE2CrystalScience.makeId(basePath + "/alternate_arrow"))
                .texture("west", AE2CrystalScience.makeId(basePath + "/alternate_arrow"));

        // blockstate：根据 PUSH_DIRECTION 选择 normal / oriented，并对 oriented 做旋转
        multiVariantGenerator(block, Variant.variant())
                .with(PropertyDispatch.property(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
                    Direction forward = pushDirection.getDirection();
                    if (forward == null)
                    {
                        return Variant.variant().with(VariantProperties.MODEL, normalModel.getLocation());
                    }
                    else
                    {
                        BlockOrientation orientation = BlockOrientation.get(forward);
                        return applyRotation(
                                Variant.variant().with(VariantProperties.MODEL, orientedModel.getLocation()),
                                // +90 因为默认模型是朝上的，而方块方向假设是朝北的
                                orientation.getAngleX() + 90,
                                orientation.getAngleY(),
                                0);
                    }
                }));
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

    private ModelFile cubeAllWithTexture(Block block, ResourceLocation texture)
    {
        return this.models().cubeAll(path(block).getPath(), texture);
    }

    private ResourceLocation path(Block block)
    {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    protected final MultiVariantGenerator multiVariantGenerator(Block block, Variant... variants)
    {
        if (variants.length == 0)
        {
            variants = new Variant[]{Variant.variant()};
        }
        var builder = MultiVariantGenerator.multiVariant(block, variants);
        registeredBlocks.put(block, () -> builder.get().getAsJsonObject());
        return builder;
    }

    protected static PropertyDispatch createFacingSpinDispatch(int baseRotX, int baseRotY)
    {
        return PropertyDispatch.properties(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return applyRotation(
                            Variant.variant(),
                            orientation.getAngleX() + baseRotX,
                            orientation.getAngleY() + baseRotY,
                            orientation.getAngleZ());
                });
    }

    protected static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ)
    {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        angleZ = normalizeAngle(angleZ);

        if (angleX != 0)
        {
            variant = variant.with(VariantProperties.X_ROT, rotationByAngle(angleX));
        }
        if (angleY != 0)
        {
            variant = variant.with(VariantProperties.Y_ROT, rotationByAngle(angleY));
        }
        if (angleZ != 0)
        {
            variant = variant.with(Z_ROT, rotationByAngle(angleZ));
        }
        return variant;
    }

    private static int normalizeAngle(int angle)
    {
        return angle - (angle / 360) * 360;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle)
    {
        return switch (angle)
        {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    protected static PropertyDispatch createFacingSpinDispatch()
    {
        return createFacingSpinDispatch(0, 0);
    }
}
