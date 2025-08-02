package io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation;

import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {

    public static ResourceLocation prefix(String id) {
        return ResourceLocation.fromNamespaceAndPath(AE2CrystalSeedsAPI.MOD_ID, id);
    }
}
