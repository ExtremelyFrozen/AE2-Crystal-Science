package io.github.lounode.ae2cs.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ServerUtil
{
    /**
     * 从UUID查玩家名称
     */
    public static String getPlayerNameByUUID(UUID uuid, MinecraftServer infoProvider)
    {

        ServerPlayer onlinePlayer = infoProvider.getPlayerList().getPlayer(uuid);
        if (onlinePlayer != null)
        {
            return onlinePlayer.getGameProfile().name();
        }

        var gameProfile = infoProvider.services().profileResolver().fetchById(uuid);

        if(gameProfile.isPresent()) return gameProfile.get().name();

        return "Unknown";
    }
}
