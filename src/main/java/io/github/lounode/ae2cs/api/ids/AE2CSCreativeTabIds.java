package io.github.lounode.ae2cs.api.ids;

import io.github.lounode.ae2cs.core.AE2CS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class AE2CSCreativeTabIds {
    private AE2CSCreativeTabIds() {}

    public static final ResourceKey<CreativeModeTab> MAIN = create("main");

    private static ResourceKey<CreativeModeTab> create(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, AE2CS.makeId(path));
    }
}
