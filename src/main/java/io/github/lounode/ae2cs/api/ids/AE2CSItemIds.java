package io.github.lounode.ae2cs.api.ids;

import net.minecraft.resources.ResourceLocation;

public class AE2CSItemIds {

    public static final ResourceLocation PURE_CERTUS_QUARTZ_CRYSTAL = id("purified_certus_quartz_crystal");

    private static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(AE2CSConstants.MOD_ID, id);
    }
}
