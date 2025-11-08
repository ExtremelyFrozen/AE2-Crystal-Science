package io.github.lounode.ae2cs.core;

import io.github.lounode.ae2cs.api.ids.AE2CSConstants;
import net.minecraft.resources.ResourceLocation;

public interface AE2CS {
    String MOD_NAME = "AE2: Crystal Science";
    String MOD_ID = AE2CSConstants.MOD_ID;

    static AE2CS instance() {
        return AE2CSBase.INSTANCE;
    }

    static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

}
