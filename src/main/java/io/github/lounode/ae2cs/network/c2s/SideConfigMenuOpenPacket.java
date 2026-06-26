package io.github.lounode.ae2cs.network.c2s;

import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;

import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SideConfigMenuOpenPacket() {

    public static void encode(SideConfigMenuOpenPacket packet, FriendlyByteBuf buf) {}

    public static SideConfigMenuOpenPacket decode(FriendlyByteBuf buf) {
        return new SideConfigMenuOpenPacket();
    }

    private void handleInServer(NetworkEvent.Context context) {
        ServerPlayer sp = context.getSender();
        if (sp == null) return;

        MenuType<?> beforeType = null;
        if (sp.containerMenu instanceof AEBaseMenu menu) {
            beforeType = menu.getType();
            MenuOpener.open(AECSMenus.SIDE_CONFIG_MENU.get(), sp, menu.getLocator());
        }
        if (sp.containerMenu instanceof SideConfigMenu menu) {
            menu.setReturnToMenuType(beforeType);
        }
    }

    private void handleInClient(NetworkEvent.Context context) {}

    public static void handle(SideConfigMenuOpenPacket packet, Supplier<NetworkEvent.Context> cxt) {
        if (packet != null) {
            NetworkEvent.Context context = cxt.get();
            NetworkDirection direction = context.getDirection();
            if (direction == NetworkDirection.PLAY_TO_CLIENT) {
                context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packet.handleInClient(context)));
                context.setPacketHandled(true);
            } else if (direction == NetworkDirection.PLAY_TO_SERVER) {
                context.enqueueWork(() -> packet.handleInServer(context));
                context.setPacketHandled(true);
            }
        }
    }
}
