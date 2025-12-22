package io.github.lounode.ae2cs.api.linker.broadcast;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** 用于管理当前服务端内所有频段 */
public class FrequencyBandManager extends SavedData
{
    private static final String MANAGER_PATH = "AECS/FrequencyBandManager";

    private static final SavedData.Factory<FrequencyBandManager> FACTORY =
            new SavedData.Factory<>(FrequencyBandManager::new, FrequencyBandManager::load);


    private final Map<String, BroadcastFrequencyBand> frequencyBands = new HashMap<>();

    private FrequencyBandManager() {}

    @Nullable
    public static BroadcastFrequencyBand getTestBand()
    {
        return tryCreateBand("test", "", true, true);
    }

    @Nullable
    public static BroadcastFrequencyBand getBand(String bandName)
    {
        FrequencyBandManager manager = resolveManager();
        if(manager == null) return null;

        return manager.frequencyBands.get(bandName);
    }

    @Nullable
    public static BroadcastFrequencyBand tryCreateBand(String bandName, String password, boolean isPublic, boolean allowedMemoryCardCopy)
    {
        FrequencyBandManager manager = resolveManager();
        if(manager == null) return null;

        BroadcastFrequencyBand band = manager.frequencyBands.get(bandName);
        if(band == null)
        {
            band = new BroadcastFrequencyBand(bandName, password, isPublic, allowedMemoryCardCopy);
            manager.frequencyBands.put(bandName, band);
        }
        return band;
    }

    @Nullable
    private static FrequencyBandManager resolveManager()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return null;

        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, MANAGER_PATH);
    }

    public static FrequencyBandManager load(CompoundTag tag, HolderLookup.Provider registries)
    {
        FrequencyBandManager manager = new FrequencyBandManager();

        ListTag bandsTag = tag.getList("bands", 10);
        for(Tag bandTag : bandsTag)
        {
            if(!(bandTag instanceof CompoundTag compoundBandTag)) continue;

            BroadcastFrequencyBand band = null;
            boolean completed = false;
            try
            {
                band = new BroadcastFrequencyBand("", "", false, false);
                band.deserializeNBT(registries, compoundBandTag);
                completed = true;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(completed)
                {
                    manager.frequencyBands.put(band.getName(), band);
                }
            }
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider)
    {
        ListTag bandTags = new ListTag();
        for(BroadcastFrequencyBand band : frequencyBands.values())
        {
            bandTags.add(band.serializeNBT(provider));
        }
        tag.put("bands", bandTags);
        return tag;
    }
}
