package io.github.lounode.ae2cs.common.util.resourcelocation;

import io.github.lounode.ae2cs.api.AE2CrystalSeedsAPI;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {

    public static ResourceLocation prefix(String id) {
        return ResourceLocation.fromNamespaceAndPath(AE2CrystalSeedsAPI.MOD_ID, id);
    }
}
