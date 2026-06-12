package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;

import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public record SideConfigMenuOpenPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SideConfigMenuOpenPacket> TYPE = new CustomPacketPayload.Type<>(AE2CrystalScience.makeId("side_config_menu_open_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SideConfigMenuOpenPacket> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public @NotNull SideConfigMenuOpenPacket decode(@NotNull RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new SideConfigMenuOpenPacket();
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull SideConfigMenuOpenPacket toggleMagnetPacket) {}
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void handleInServer(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        MenuType<?> beforeType = null;
        if (sp.containerMenu instanceof AEBaseMenu menu) {
            beforeType = menu.getType();
            MenuOpener.open(AECSMenus.SIDE_CONFIG_MENU.get(), sp, menu.getLocator());
        }
        if (sp.containerMenu instanceof SideConfigMenu menu) {
            menu.setReturnToMenuType(beforeType);
        }
    }

    private void handleInClient(Player player) {}

    public static void handle(final SideConfigMenuOpenPacket packet, final IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.flow().isServerbound())
                        packet.handleInServer(context.player());
                    else if (context.flow().isClientbound())
                        packet.handleInClient(context.player());
                });
    }
}
