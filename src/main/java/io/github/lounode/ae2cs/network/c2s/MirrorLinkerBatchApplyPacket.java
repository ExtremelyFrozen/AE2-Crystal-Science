package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.common.item.MirrorLinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MirrorLinkerBatchApplyPacket(BlockPos pos, double hitX, double hitY, double hitZ)
{
    public static void encode(MirrorLinkerBatchApplyPacket packet, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(packet.pos);
        buf.writeDouble(packet.hitX);
        buf.writeDouble(packet.hitY);
        buf.writeDouble(packet.hitZ);
    }

    public static MirrorLinkerBatchApplyPacket decode(FriendlyByteBuf buf)
    {
        BlockPos pos = buf.readBlockPos();
        return new MirrorLinkerBatchApplyPacket(pos, buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private void handleInServer(NetworkEvent.Context context)
    {
        ServerPlayer sp = context.getSender();
        if (sp == null) return;

        var stack = sp.getMainHandItem();
        if (!(stack.getItem() instanceof MirrorLinkerItem))
        {
            return;
        }

        MirrorLinkerItem.applyStoredTargetToCluster(stack, sp, sp.level(), pos, new Vec3(hitX, hitY, hitZ));
    }

    private void handleInClient(NetworkEvent.Context context)
    {
    }

    public static void handle(MirrorLinkerBatchApplyPacket packet, Supplier<NetworkEvent.Context> cxt)
    {
        if (packet == null)
        {
            return;
        }

        NetworkEvent.Context context = cxt.get();
        NetworkDirection direction = context.getDirection();
        if (direction == NetworkDirection.PLAY_TO_CLIENT)
        {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packet.handleInClient(context)));
        }
        else if (direction == NetworkDirection.PLAY_TO_SERVER)
        {
            context.enqueueWork(() -> packet.handleInServer(context));
        }
        context.setPacketHandled(true);
    }
}
