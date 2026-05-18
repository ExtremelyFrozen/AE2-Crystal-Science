package io.github.lounode.ae2cs.common.init.client;

import appeng.client.render.AERenderPipelines;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import io.github.lounode.ae2cs.AE2CrystalScience;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class AECSRenderTypes {
    private AECSRenderTypes() {
    }

    public static final RenderType RESONATING_MARK_FACE = RenderType.create(
            "ae2cs_resonating_mark_face",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_FACE.toBuilder()
                            .withLocation(AE2CrystalScience.makeId("pipeline/resonating_mark_face"))
                            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, false))
                            .build())
                    .bufferSize(RenderType.BIG_BUFFER_SIZE)
                    .createRenderSetup());
}
