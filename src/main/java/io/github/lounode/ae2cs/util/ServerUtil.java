package io.github.lounode.ae2cs.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;

import com.mojang.authlib.GameProfile;

import java.util.Optional;
import java.util.UUID;

public class ServerUtil {

    /**
     * 从UUID查玩家名称
     */
    public static String getPlayerNameByUUID(UUID uuid, MinecraftServer infoProvider) {
        ServerPlayer onlinePlayer = infoProvider.getPlayerList().getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getGameProfile().getName();
        }

        GameProfileCache profileCache = infoProvider.getProfileCache();
        if (profileCache != null) {
            Optional<GameProfile> profileInfo = profileCache.get(uuid);
            if (profileInfo.isPresent()) {
                return profileInfo.get().getName();
            }
        }

        return "Unknown";
    }
}
