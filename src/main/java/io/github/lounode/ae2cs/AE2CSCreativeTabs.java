package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.AE2CrystalSeedsAPI;
import io.github.lounode.ae2cs.common.item.AE2CrystalSeedsItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.BiConsumer;

import static io.github.lounode.ae2cs.common.util.resourcelocation.ResourceLocationUtil.prefix;

public class AE2CSCreativeTabs {
    public static final ResourceKey<CreativeModeTab> AE2_CRYSTAL_SEEDS_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            prefix(AE2CrystalSeedsAPI.MOD_ID));

    public static void registerCreativeTabs (BiConsumer<CreativeModeTab, ResourceLocation> r) {
        r.accept(CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.ae2_crystal_seeds"))
                        .icon(AE2CrystalSeedsItems.pureCertusQuartzCrystal::getDefaultInstance)
                        .build(),
                AE2_CRYSTAL_SEEDS_TAB_KEY.location());
    }
}
