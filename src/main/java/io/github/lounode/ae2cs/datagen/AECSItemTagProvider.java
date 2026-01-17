package io.github.lounode.ae2cs.datagen;

import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.appflux.util.AFTags;
import com.glodblock.github.extendedae.util.EAETags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.sapporo1101.appgen.util.AGTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSItemTagProvider extends ItemTagsProvider
{
    public AECSItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTags, AECSConstants.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
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
                .addTag(AECSTags.Items.PURE_EMBER_CRYSTAL);
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


        // 粉尘
        tag(Tags.Items.DUSTS)
                .addTag(AECSTags.Items.DUST_RESONATING)
                .addTag(AECSTags.Items.DUST_QUARTZ)
                .addTag(AECSTags.Items.DUST_QUANTUM_ALLOY);
        tag(AECSTags.Items.DUST_RESONATING)
                .add(AECSItems.RESONATING_DUST.get());
        tag(AECSTags.Items.DUST_QUARTZ)
                .add(AECSItems.NETHER_QUARTZ_DUST.get());
        tag(AECSTags.Items.DUST_QUANTUM_ALLOY)
                .add(AECSItems.QUANTUM_CRYSTAL_DUST.get());

        // 存储方块
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(AECSTags.Items.STORAGE_BLOCK_SKY_STONE_CRYSTAL)
                .addTag(AECSTags.Items.STORAGE_BLOCK_RESONATING)
                .addTag(AECSTags.Items.STORAGE_BLOCK_ENDER_QUARTZ)
                .addTag(AECSTags.Items.STORAGE_BLOCK_SILICON);
        tag(AECSTags.Items.STORAGE_BLOCK_SKY_STONE_CRYSTAL)
                .add(AECSBlocks.PURE_METEOR_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_RESONATING)
                .add(AECSBlocks.PURE_RESONATING_CRYSTAL_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_ENDER_QUARTZ)
                .add(AECSBlocks.PURE_ENDER_QUARTZ_BLOCK.asItem());
        tag(AECSTags.Items.STORAGE_BLOCK_SILICON)
                .add(AECSBlocks.SILICON_BLOCK.asItem());

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
    }
}