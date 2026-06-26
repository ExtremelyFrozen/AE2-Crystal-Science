package io.github.lounode.ae2cs.client.render;

import io.github.lounode.ae2cs.common.item.EnderLinkerItem;

import net.minecraft.client.Minecraft;

public final class EnderEmitterClientRenderHelper {

    private EnderEmitterClientRenderHelper() {}

    public static boolean shouldRenderLinkStatus() {
        return EnderLinkerItem.isHoldingLinker(Minecraft.getInstance().player);
    }
}
