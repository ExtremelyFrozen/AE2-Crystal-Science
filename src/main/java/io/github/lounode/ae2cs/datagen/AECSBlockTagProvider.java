package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.pedroksl.advanced_ae.datagen.AAEConventionTags;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSBlockTagProvider extends BlockTagsProvider
{

    public AECSBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        // 信标基座
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(AECSBlocks.ENDER_BROADCASTER_BLOCK.get())
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL);

        // 非接口机器
        tag(AECSTags.Blocks.AECS_MACHINE)
                .add(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get())
                .add(AECSBlocks.CIRCUIT_ETCHER_BLOCK.get())
                .add(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK.get())
                .add(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get())
                .add(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.get())
                .add(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get())
                .add(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.get())
                .add(AECSBlocks.ENDER_BROADCASTER_BLOCK.get())
                .add(AECSBlocks.ENDER_EMITTER_BLOCK.get());

        // 接口机器
        tag(AECSTags.Blocks.AECS_PART)
                .add(AECSBlocks.ENDER_INTERFACE_BLOCK.get())
                .add(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get())
                .add(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get())
                .add(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.get())
                .add(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.get())
                .add(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK.get())
                .add(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.get())
                .add(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get())
                .add(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.get());

        // 防CarryOn和纸箱
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .add(AECSBlocks.ENDER_BROADCASTER_BLOCK.get())
                .add(AECSBlocks.ENDER_EMITTER_BLOCK.get());

        // 高纯水晶块
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL);
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .add(AECSBlocks.PURE_ENDER_QUARTZ_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .add(AECSBlocks.PURE_RESONATING_CRYSTAL_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .add(AECSBlocks.PURE_METEOR_CRYSTAL_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL)
                .add(AECSBlocks.PURE_REDSTONE_CRYSTAL_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL)
                .add(AECSBlocks.PURE_QUANTUM_CRYSTAL_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ)
                .add(AECSBlocks.PURE_ROSE_QUARTZ_BLOCK.get());
        tag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL)
                .add(AECSBlocks.IRRADIATED_CRYSTAL_BLOCK.get());

        // 矿
        tag(Tags.Blocks.ORES)
                .addTag(AECSTags.Blocks.ORES_CERTUS_QUARTZ);
        // 普通倍率矿石
        tag(Tags.Blocks.ORE_RATES_SINGULAR)
                .addTag(AECSTags.Blocks.ORES_CERTUS_QUARTZ);
        // 赛特斯石英矿
        tag(AECSTags.Blocks.ORES_CERTUS_QUARTZ)
                .add(AECSBlocks.CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get());
        // 生成在石头中的矿石
        tag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(AECSBlocks.CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get());
        // 深板岩层的矿石
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get());

        // 存储方块
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_SILICON);
        tag(AECSTags.Blocks.STORAGE_BLOCK_SILICON)
                .add(AECSBlocks.SILICON_BLOCK.get());

        // 联动区域
        tag(AAEConventionTags.QUANTUM_ALLOY_STORAGE_BLOCK_BLOCK)
                .add(AECSBlocks.PURE_QUANTUM_CRYSTAL_BLOCK.get());

        // 镐挖掘
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(AECSBlocks.CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.get())
                .add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.get())
                .addTag(AECSTags.Blocks.AECS_MACHINE)
                .addTag(AECSTags.Blocks.AECS_PART)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_PURE_CRYSTAL)
                .addTag(AECSTags.Blocks.STORAGE_BLOCK_SILICON);

        // 允许主世界洞穴生成覆盖这些方块
        tag(BlockTags.OVERWORLD_CARVER_REPLACEABLES)
                .addTag(AECSTags.Blocks.ORES_CERTUS_QUARTZ);
    }
}
