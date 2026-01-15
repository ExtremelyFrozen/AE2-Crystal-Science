package io.github.lounode.ae2cs.datagen;

import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import net.pedroksl.advanced_ae.datagen.AAEConventionTags;
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
        tag(AECSTags.Items.CRYSTAL_SEEDS)
                .add(AECSItems.getCrystalSeeds().stream().map(DeferredItem::get).toArray(Item[]::new));
        tag(AECSTags.Items.PURIFIED_CRYSTAL)
                .add(AECSItems.getPureCrystal().stream().map(DeferredItem::get).toArray(Item[]::new));

        // 宝石
        tag(Tags.Items.GEMS)
                .addTag(AECSTags.Items.GEM_RESONATING)
                .addTag(AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .addTag(AECSTags.Items.GEM_ENDER_QUARTZ);
        tag(AECSTags.Items.GEM_RESONATING)
                .add(AECSItems.pureResonatingCrystal.get());
        tag(AECSTags.Items.GEM_ENDER_QUARTZ)
                .add(AECSItems.pureEnderQuartz.get());
        tag(AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .add(AECSItems.pureMeteorCrystal.get());
        tag(Tags.Items.GEMS_QUARTZ)
                .add(AECSItems.pureNetherQuartzCrystal.get());


        // 粉尘
        tag(Tags.Items.DUSTS)
                .addTag(AECSTags.Items.DUST_RESONATING)
                .addTag(AECSTags.Items.DUST_QUARTZ);
        tag(AECSTags.Items.DUST_RESONATING)
                .add(AECSItems.RESONATING_DUST.get());
        tag(AECSTags.Items.DUST_QUARTZ)
                .add(AECSItems.NETHER_QUARTZ_DUST.get());

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


        // 联动区域
        tag(AAEConventionTags.QUANTUM_ALLOY)
                .add(AECSItems.pureQuantumCrystal.get());
        tag(AAEConventionTags.QUANTUM_ALLOY_STORAGE_BLOCK_ITEM)
                .add(AECSBlocks.PURE_QUANTUM_CRYSTAL_BLOCK.asItem());
    }
}