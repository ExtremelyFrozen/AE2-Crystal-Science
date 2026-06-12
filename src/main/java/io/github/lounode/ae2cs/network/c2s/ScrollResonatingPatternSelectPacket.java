package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.item.ResonatingPatternItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

/**
 * @param next 向后翻还是向前翻，next表示向后翻
 */
public record ScrollResonatingPatternSelectPacket(boolean next) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ScrollResonatingPatternSelectPacket> TYPE = new CustomPacketPayload.Type<>(AE2CrystalScience.makeId("scroll_resonating_pattern_select_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollResonatingPatternSelectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ScrollResonatingPatternSelectPacket::next,
            ScrollResonatingPatternSelectPacket::new);

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void handleInServer(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        var stack = sp.getMainHandItem();
        if (!(stack.getItem() instanceof ResonatingPatternItem)) return;

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null) return;

        ResonatingPatternItem.scrollSelectedInputAndToast(sp, stack, encoded, this.next());
    }

    private void handleInClient(Player player) {}

    public static void handle(final ScrollResonatingPatternSelectPacket packet, final IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.flow().isServerbound())
                        packet.handleInServer(context.player());
                    else if (context.flow().isClientbound())
                        packet.handleInClient(context.player());
                });
    }
}
