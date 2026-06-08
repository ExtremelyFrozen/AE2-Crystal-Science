package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MirrorLinkerBatchApplyPacket(BlockPos pos, boolean mainHand, double hitX, double hitY, double hitZ) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<MirrorLinkerBatchApplyPacket> TYPE =
            new CustomPacketPayload.Type<>(AE2CrystalScience.makeId("mirror_linker_batch_apply_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MirrorLinkerBatchApplyPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MirrorLinkerBatchApplyPacket::pos,
            ByteBufCodecs.BOOL,
            MirrorLinkerBatchApplyPacket::mainHand,
            ByteBufCodecs.DOUBLE,
            MirrorLinkerBatchApplyPacket::hitX,
            ByteBufCodecs.DOUBLE,
            MirrorLinkerBatchApplyPacket::hitY,
            ByteBufCodecs.DOUBLE,
            MirrorLinkerBatchApplyPacket::hitZ,
            MirrorLinkerBatchApplyPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    private void handleInServer(Player player)
    {
        if (!(player instanceof ServerPlayer sp)) return;

        InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var stack = sp.getItemInHand(hand);
        if (!(stack.getItem() instanceof MirrorLinkerItem))
        {
            return;
        }

        MirrorLinkerItem.applyStoredTargetToCluster(stack, sp, sp.level(), pos, new Vec3(hitX, hitY, hitZ));
    }

    public static void handle(final MirrorLinkerBatchApplyPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound())
            {
                packet.handleInServer(context.player());
            }
        });
    }
}
