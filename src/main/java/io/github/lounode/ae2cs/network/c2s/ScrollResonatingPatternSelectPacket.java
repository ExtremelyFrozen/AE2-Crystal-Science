package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.ResonatingPatternItem;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @param next 向后翻还是向前翻，next表示向后翻
 */
public record ScrollResonatingPatternSelectPacket(boolean next)
{
    public static void encode(ScrollResonatingPatternSelectPacket packet, FriendlyByteBuf buf)
    {
        buf.writeBoolean(packet.next);
    }

    public static ScrollResonatingPatternSelectPacket decode(FriendlyByteBuf buf)
    {
        return new ScrollResonatingPatternSelectPacket(buf.readBoolean());
    }

    private void handleInServer(NetworkEvent.Context context)
    {
        ServerPlayer sp = context.getSender();
        if (sp == null) return;

        var stack = sp.getMainHandItem();
        if (!(stack.getItem() instanceof ResonatingPatternItem)) return;

        EncodedResonatingPattern encoded = AECSDataComponents.getEncodedResonatingPattern(stack);
        if (encoded == null) return;

        ResonatingPatternItem.scrollSelectedInputAndToast(sp, stack, encoded, this.next());
    }

    private void handleInClient(NetworkEvent.Context context)
    {

    }

    public static void handle(ScrollResonatingPatternSelectPacket packet, Supplier<NetworkEvent.Context> cxt)
    {
        if (packet != null)
        {
            NetworkEvent.Context context = cxt.get();
            NetworkDirection direction = context.getDirection();
            if (direction == NetworkDirection.PLAY_TO_CLIENT)
            {
                context.enqueueWork(() ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packet.handleInClient(context))
                );
                context.setPacketHandled(true);
            }
            else if (direction == NetworkDirection.PLAY_TO_SERVER)
            {
                context.enqueueWork(() -> packet.handleInServer(context));
                context.setPacketHandled(true);
            }
        }
    }
}