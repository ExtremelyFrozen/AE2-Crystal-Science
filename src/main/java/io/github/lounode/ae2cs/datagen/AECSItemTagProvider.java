package io.github.lounode.ae2cs.datagen;

import appeng.api.ids.AETags;
import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
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

        tag(Tags.Items.GEMS)
                .addTag(AECSTags.Items.GEM_RESONATING);
        tag(Tags.Items.DUSTS)
                .addTag(AECSTags.Items.DUST_RESONATING)
                .addTag(AECSTags.Items.DUST_QUARTZ);

        tag(AECSTags.Items.DUST_RESONATING)
                .add(AECSItems.RESONATING_DUST.get());
        tag(AECSTags.Items.GEM_RESONATING)
                .add(AECSItems.pureResonatingCrystal.get());

        tag(AECSTags.Items.DUST_QUARTZ)
                .add(AECSItems.NETHER_QUARTZ_DUST.get());
        tag(Tags.Items.GEMS_QUARTZ)
                .add(AECSItems.pureNetherQuartzCrystal.get());

        // 空白压印模板实际上不算压印模板，是故不计入
        tag(ConventionTags.INSCRIBER_PRESSES)
                .add(AECSItems.RESONATING_PRINT_PRESS.get());
    }
}
