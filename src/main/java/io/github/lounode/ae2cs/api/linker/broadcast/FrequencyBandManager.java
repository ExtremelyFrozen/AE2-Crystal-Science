package io.github.lounode.ae2cs.api.linker.broadcast;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 用于管理当前服务端内所有频段（持久化 + 运行时重算调度）
 */
@EventBusSubscriber(modid = AECSConstants.MODID)
public class FrequencyBandManager extends SavedData
{
    /**
     * SavedData存放的文件夹
     */
    private static final String SAVED_FOLDER_NAME = "aecs";

    /**
     * 持久化文件名
     */
    private static final String MANAGER_PATH = SAVED_FOLDER_NAME + "/frequency_band_manager";

    private static final SavedData.Factory<FrequencyBandManager> FACTORY =
            new SavedData.Factory<>(FrequencyBandManager::new, FrequencyBandManager::load);

    /**
     * name -> 频段实例
     */
    private final Map<String, BroadcastFrequencyBand> frequencyBands = new LinkedHashMap<>();

    /**
     * 运行时：被标记需要重算的 band（值为标记时的 gameTime），用于下一tick重算
     */
    private final transient Object2LongOpenHashMap<String> dirtyRuntimeAt = new Object2LongOpenHashMap<>();

    private FrequencyBandManager()
    {
        dirtyRuntimeAt.defaultReturnValue(Long.MIN_VALUE);
    }

    /**
     * 获取频段
     */
    @Nullable
    public static BroadcastFrequencyBand getBand(String bandName)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return null;
        return manager.frequencyBands.get(bandName);
    }

    /**
     * 查询是否存在此频段
     */
    public static boolean isBandPresent(String bandName)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return false;

        return manager.frequencyBands.containsKey(bandName);
    }

    /**
     * 将频段的大体信息包装后用以网络传输
     */
    @Nullable
    public static BroadcastBandsField getBandsInfo()
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return null;

        List<BroadcastBandsField.Entry> out = new ArrayList<>(manager.frequencyBands.size());

        for (BroadcastFrequencyBand band : manager.frequencyBands.values())
        {
            String name = band.getName();

            boolean isPublic = band.isPublic();
            boolean isEncrypted = !band.getPassword().isEmpty();

            byte flags = BroadcastBandsField.Entry.pack(isPublic, isEncrypted);
            out.add(new BroadcastBandsField.Entry(name, flags));
        }

        return new BroadcastBandsField(List.copyOf(out));
    }

    /**
     * 将频段的大体信息包装后用以网络传输，但剔除掉玩家不可见的部分
     */
    @Nullable
    public static BroadcastBandsField getBandsInfoByPlayer(Player player)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return null;

        UUID playerUUID = player.getUUID();
        List<BroadcastBandsField.Entry> out = new ArrayList<>(manager.frequencyBands.size());

        for (BroadcastFrequencyBand band : manager.frequencyBands.values())
        {
            String name = band.getName();

            boolean isPublic = band.isPublic();
            if (!isPublic && !band.validWhiteList(playerUUID)) continue;

            boolean isEncrypted = !band.getPassword().isEmpty();

            byte flags = BroadcastBandsField.Entry.pack(isPublic, isEncrypted);
            out.add(new BroadcastBandsField.Entry(name, flags));
        }

        return new BroadcastBandsField(List.copyOf(out));
    }

    /**
     * 获取频段，如果不存在则创建
     */
    @Nullable
    public static BroadcastFrequencyBand tryCreateBand(String bandName, String password, UUID ownerId, boolean isPublic, boolean allowedMemoryCardCopy)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return null;

        BroadcastFrequencyBand band = manager.frequencyBands.get(bandName);
        if (band == null)
        {
            band = new BroadcastFrequencyBand(bandName, password, ownerId, isPublic, allowedMemoryCardCopy);
            manager.frequencyBands.put(bandName, band);
            manager.setDirty();
        }
        return band;
    }

    /**
     * 标脏
     */
    public static void markDirty()
    {
        FrequencyBandManager manager = resolveManager();
        if (manager != null) manager.setDirty();
    }

    /**
     * 标记某个band需要在下一个tick重算链接
     */
    public static void markRuntimeDirty(MinecraftServer server, String bandName)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return;

        long now = server.overworld().getGameTime();

        if (manager.dirtyRuntimeAt.getLong(bandName) != now)
        {
            manager.dirtyRuntimeAt.put(bandName, now);
        }
    }

    /**
     * 统一调用服务端的频段链接重算
     */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event)
    {
        FrequencyBandManager manager = resolveManager();
        if (manager == null) return;

        MinecraftServer server = event.getServer();

        long now = server.overworld().getGameTime();

        var it = manager.dirtyRuntimeAt.object2LongEntrySet().iterator();
        while (it.hasNext())
        {
            var entry = it.next();
            String bandName = entry.getKey();
            long markedAt = entry.getLongValue();

            if (now > markedAt)
            {
                BroadcastFrequencyBand band = manager.frequencyBands.get(bandName);
                if (band != null)
                {
                    band.recomputeRuntime();
                }
                it.remove();
            }
        }
    }

    /**
     * 获取服务端中唯一的频段管理者
     */
    @Nullable
    private static FrequencyBandManager resolveManager()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ensureSaveDirExists(server);
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, MANAGER_PATH);
    }

    // ---------- 持久化 ----------

    public static FrequencyBandManager load(CompoundTag tag, HolderLookup.Provider registries)
    {
        FrequencyBandManager manager = new FrequencyBandManager();

        ListTag bandsTag = tag.getList("bands", 10);
        for (Tag bandTag : bandsTag)
        {
            if (!(bandTag instanceof CompoundTag compoundBandTag)) continue;

            try
            {
                BroadcastFrequencyBand band = new BroadcastFrequencyBand("", "", UUID.randomUUID(), false, false);
                band.deserializeNBT(registries, compoundBandTag);
                manager.frequencyBands.put(band.getName(), band);
            }
            catch (Throwable ignored)
            {
            }
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider)
    {
        ListTag bandTags = new ListTag();
        for (BroadcastFrequencyBand band : frequencyBands.values())
        {
            bandTags.add(band.serializeNBT(provider));
        }
        tag.put("bands", bandTags);
        return tag;
    }

    /**
     * 确保 world/data/aecs 目录存在，否则savedData没法正确创建在带目录的路径下
     */
    private static void ensureSaveDirExists(@NotNull MinecraftServer server)
    {
        Path dir = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(SAVED_FOLDER_NAME);
        try
        {
            Files.createDirectories(dir);
        }
        catch (IOException e)
        {
            // 静默
        }
    }
}