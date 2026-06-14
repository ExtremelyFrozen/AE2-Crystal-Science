package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.init.AECSTags;

import appeng.datagen.providers.tags.ConventionTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import com.glodblock.github.extendedae.util.EAETags;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSItemTagProvider extends ItemTagsProvider {

    public AECSItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // 水晶种子
        tag(AECSTags.Items.CRYSTAL_SEEDS)
                .add(AECSItems.getCrystalSeeds().stream().map(DeferredItem::get).toArray(Item[]::new));

        // 所有高纯水晶
        tag(AECSTags.Items.PURE_CRYSTAL)
                .addTag(AECSTags.Items.PURE_CERTUS_QUARTZ_CRYSTAL)
                .addTag(AECSTags.Items.PURE_FLUIX_CRYSTAL)
                .addTag(AECSTags.Items.PURE_NETHER_QUARTZ_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ENDER_QUARTZ)
                .addTag(AECSTags.Items.PURE_METEOR_CRYSTAL)
                .addTag(AECSTags.Items.PURE_RESONATING_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ENTRO_CRYSTAL)
                .addTag(AECSTags.Items.PURE_REDSTONE_CRYSTAL)
                .addTag(AECSTags.Items.PURE_QUANTUM_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ROSE_QUARTZ)
                .addTag(AECSTags.Items.PURE_IRRADIATED_CRYSTAL)
                .addTag(AECSTags.Items.PURE_EMBER_CRYSTAL)
                .addTag(AECSTags.Items.PURE_LINK_CRYSTAL)
                .addTag(AECSTags.Items.PURE_OVERLOAD_CRYSTAL)
                .addTag(AECSTags.Items.PURE_DATA_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ENERGIZED_FLUIX_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL);
        tag(AECSTags.Items.PURE_CERTUS_QUARTZ_CRYSTAL)
                .add(AECSItems.PURE_CERTUS_QUARTZ_CRYSTAL.get());
        tag(AECSTags.Items.PURE_FLUIX_CRYSTAL)
                .add(AECSItems.PURE_FLUIX_CRYSTAL.get());
        tag(AECSTags.Items.PURE_NETHER_QUARTZ_CRYSTAL)
                .add(AECSItems.PURE_NETHER_QUARTZ_CRYSTAL.get());
        tag(AECSTags.Items.PURE_ENDER_QUARTZ)
                .add(AECSItems.PURE_ENDER_QUARTZ.get());
        tag(AECSTags.Items.PURE_METEOR_CRYSTAL)
                .add(AECSItems.PURE_METEOR_CRYSTAL.get());
        tag(AECSTags.Items.PURE_RESONATING_CRYSTAL)
                .add(AECSItems.PURE_RESONATING_CRYSTAL.get());
        tag(AECSTags.Items.PURE_ENTRO_CRYSTAL)
                .add(AECSItems.PURE_ENTRO_CRYSTAL.get());
        tag(AECSTags.Items.PURE_REDSTONE_CRYSTAL)
                .add(AECSItems.PURE_REDSTONE_CRYSTAL.get());
        tag(AECSTags.Items.PURE_QUANTUM_CRYSTAL)
                .add(AECSItems.PURE_QUANTUM_CRYSTAL.get());
        tag(AECSTags.Items.PURE_ROSE_QUARTZ)
                .add(AECSItems.PURE_ROSE_QUARTZ.get());
        tag(AECSTags.Items.PURE_IRRADIATED_CRYSTAL)
                .add(AECSItems.PURE_IRRADIATED_CRYSTAL.get());
        tag(AECSTags.Items.PURE_EMBER_CRYSTAL)
                .add(AECSItems.PURE_EMBER_CRYSTAL.get());
        tag(AECSTags.Items.PURE_LINK_CRYSTAL)
                .add(AECSItems.PURE_LINK_CRYSTAL.get());
        tag(AECSTags.Items.PURE_OVERLOAD_CRYSTAL)
                .add(AECSItems.PURE_OVERLOAD_CRYSTAL.get());
        tag(AECSTags.Items.PURE_DATA_CRYSTAL)
                .add(AECSItems.PURE_DATA_CRYSTAL.get());
        tag(AECSTags.Items.PURE_ENERGIZED_FLUIX_CRYSTAL)
                .add(AECSItems.PURE_ENERGIZED_FLUIX_CRYSTAL.get());
        tag(AECSTags.Items.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL)
                .add(AECSItems.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL.get());

        // 粉尘
        tag(Tags.Items.DUSTS)
                .addTag(AECSTags.Items.DUST_RESONATING)
                .addTag(AECSTags.Items.DUST_QUARTZ)
                .addTag(AECSTags.Items.DUST_QUANTUM_ALLOY)
                .addTag(AECSTags.Items.DUST_REDSTONE_CRYSTAL);
        tag(AECSTags.Items.DUST_RESONATING)
                .add(AECSItems.RESONATING_DUST.get());
        tag(AECSTags.Items.DUST_QUARTZ)
                .add(AECSItems.NETHER_QUARTZ_DUST.get());
        tag(AECSTags.Items.DUST_QUANTUM_ALLOY)
                .add(AECSItems.QUANTUM_CRYSTAL_DUST.get());
        tag(AECSTags.Items.DUST_REDSTONE_CRYSTAL)
                .add(AECSItems.REDSTONE_CRYSTAL_DUST.get());

        // 齿轮
        tag(AECSTags.Items.GEARS)
                .addTag(AECSTags.Items.GEARS_WOOD);
        tag(AECSTags.Items.GEARS_WOOD)
                .add(AECSItems.WOODEN_GEAR.get());

        // 空白压印模板实际上不算压印模板，是故不计入
        tag(ConventionTags.INSCRIBER_PRESSES)
                .add(AECSItems.RESONATING_PRINT_PRESS.get());

        // 面粉
        tag(AECSTags.Items.FLOURS)
                .addTag(AECSTags.Items.FLOURS_WHEAT);
        tag(AECSTags.Items.FLOURS_WHEAT)
                .add(AECSItems.FLOUR.get());

        // 工具
        tag(ItemTags.SWORDS)
                .add(AECSItems.ENDER_CRYSTAL_SWORD.get())
                .add(AECSItems.METEOR_CRYSTAL_SWORD.get())
                .add(AECSItems.RESONATING_CRYSTAL_SWORD.get());
        tag(ItemTags.AXES)
                .add(AECSItems.ENDER_CRYSTAL_AXE.get())
                .add(AECSItems.METEOR_CRYSTAL_AXE.get())
                .add(AECSItems.RESONATING_CRYSTAL_AXE.get());
        tag(ItemTags.PICKAXES)
                .add(AECSItems.ENDER_CRYSTAL_PICKAXE.get())
                .add(AECSItems.METEOR_CRYSTAL_PICKAXE.get())
                .add(AECSItems.RESONATING_CRYSTAL_PICKAXE.get());
        tag(ItemTags.SHOVELS)
                .add(AECSItems.ENDER_CRYSTAL_SHOVEL.get())
                .add(AECSItems.METEOR_CRYSTAL_SHOVEL.get())
                .add(AECSItems.RESONATING_CRYSTAL_SHOVEL.get());
        tag(ItemTags.HOES)
                .add(AECSItems.ENDER_CRYSTAL_HOE.get())
                .add(AECSItems.METEOR_CRYSTAL_HOE.get())
                .add(AECSItems.RESONATING_CRYSTAL_HOE.get());

        // 石英水晶对标区
        tag(Tags.Items.GEMS)
                .addTag(AECSTags.Items.GEM_RESONATING)
                .addTag(AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .addTag(AECSTags.Items.GEM_ENDER_QUARTZ);

        tag(ConventionTags.CERTUS_QUARTZ)
                .addTag(AECSTags.Items.PURE_CERTUS_QUARTZ_CRYSTAL);
        tag(ConventionTags.ALL_FLUIX)
                .addTag(AECSTags.Items.PURE_FLUIX_CRYSTAL);
        tag(ConventionTags.FLUIX_CRYSTAL)
                .addTag(AECSTags.Items.PURE_FLUIX_CRYSTAL);
        tag(Tags.Items.GEMS_QUARTZ)
                .addTag(AECSTags.Items.PURE_NETHER_QUARTZ_CRYSTAL);
        tag(AECSTags.Items.GEM_ENDER_QUARTZ)
                .addTag(AECSTags.Items.PURE_ENDER_QUARTZ);
        tag(AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .addTag(AECSTags.Items.PURE_METEOR_CRYSTAL);
        tag(AECSTags.Items.GEM_RESONATING)
                .addTag(AECSTags.Items.PURE_RESONATING_CRYSTAL);
        tag(EAETags.ENTRO_CRYSTAL)
                .addTag(AECSTags.Items.PURE_ENTRO_CRYSTAL);

        // 矿
        tag(Tags.Items.ORES)
                .addTag(AECSTags.Items.ORES_CERTUS_QUARTZ);
        // 普通倍率矿石
        tag(Tags.Items.ORE_RATES_SINGULAR)
                .addTag(AECSTags.Items.ORES_CERTUS_QUARTZ);
        // 赛特斯石英矿
        tag(AECSTags.Items.ORES_CERTUS_QUARTZ)
                .add(AECSBlocks.CERTUS_QUARTZ_ORE.asItem())
                .add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.asItem())
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.asItem())
                .add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.asItem());
        // 生成在石头中的矿石
        tag(Tags.Items.ORES_IN_GROUND_STONE)
                .add(AECSBlocks.CERTUS_QUARTZ_ORE.asItem())
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_ORE.asItem());
        // 深板岩层的矿石
        tag(Tags.Items.ORES_IN_GROUND_DEEPSLATE)
                .add(AECSBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.asItem())
                .add(AECSBlocks.DEEPSLATE_CHARGED_CERTUS_QUARTZ_ORE.asItem());

        // 非接口机器
        tag(AECSTags.Items.AECS_MACHINE)
                .add(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.asItem())
                .add(AECSBlocks.CIRCUIT_ETCHER_BLOCK.asItem())
                .add(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK.asItem())
                .add(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.asItem())
                .add(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.asItem())
                .add(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.asItem())
                .add(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.asItem())
                .add(AECSBlocks.ENDER_BROADCASTER_BLOCK.asItem())
                .add(AECSBlocks.ENDER_EMITTER_BLOCK.asItem());

        // 接口机器
        tag(AECSTags.Items.AECS_PART)
                .add(AECSBlocks.ENDER_INTERFACE_BLOCK.asItem())
                .add(AECSBlocks.EX_ENDER_INTERFACE_BLOCK.asItem())
                .add(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.asItem())
                .add(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.asItem())
                .add(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.asItem())
                .add(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK.asItem())
                .add(AECSBlocks.MIRROR_PATTERN_PROVIDER_BLOCK.asItem())
                .add(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.asItem())
                .add(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.asItem())
                .add(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.asItem());
        tag(AECSTags.Items.AECS_PART)
                .add(AECSParts.ENDER_INTERFACE_PART.get())
                .add(AECSParts.EX_ENDER_INTERFACE_PART.get())
                .add(AECSParts.INTEGRATE_INTERFACE_PART.get())
                .add(AECSParts.EX_INTEGRATE_INTERFACE_PART.get())
                .add(AECSParts.RESONATING_PATTERN_PROVIDER_PART.get())
                .add(AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART.get())
                .add(AECSParts.MIRROR_PATTERN_PROVIDER_PART.get())
                .add(AECSParts.SIMPLE_PATTERN_PROVIDER_PART.get())
                .add(AECSParts.METEORITE_PATTERN_PROVIDER_PART.get())
                .add(AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART.get());

        // 高纯水晶块
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_LINK_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_CHARGED_OVERLOAD_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_GRID)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_CHARGED_CERTUS_QUARTZ);
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .add(AECSBlocks.PURE_ENDER_QUARTZ_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .add(AECSBlocks.PURE_RESONATING_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .add(AECSBlocks.PURE_METEOR_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_REDSTONE_CRYSTAL)
                .add(AECSBlocks.PURE_REDSTONE_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_QUANTUM_CRYSTAL)
                .add(AECSBlocks.PURE_QUANTUM_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ROSE_QUARTZ)
                .add(AECSBlocks.PURE_ROSE_QUARTZ_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_IRRADIATED_CRYSTAL)
                .add(AECSBlocks.IRRADIATED_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_LINK_CRYSTAL)
                .add(AECSBlocks.PURE_LINK_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_CHARGED_OVERLOAD_CRYSTAL)
                .add(AECSBlocks.CHARGED_OVERLOAD_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_GRID)
                .add(AECSBlocks.PURE_CRYSTAL_GRID_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_CHARGED_CERTUS_QUARTZ)
                .add(AECSBlocks.CHARGED_CERTUS_QUARTZ_BLOCK.asItem());

        // 存储方块
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_SILICON);
        tag(AECSTags.Items.STORAGE_BLOCK_SILICON)
                .add(AECSBlocks.SILICON_BLOCK.asItem());
    }
}
