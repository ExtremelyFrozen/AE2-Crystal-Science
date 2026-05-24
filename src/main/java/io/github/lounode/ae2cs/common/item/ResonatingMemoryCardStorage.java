package io.github.lounode.ae2cs.common.item;

import appeng.api.ids.AEComponents;
import appeng.core.localization.GuiText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ResonatingMemoryCardStorage(int selectedSlot, List<Slot> slots)
{
    public static final int SLOT_COUNT = 9;

    public ResonatingMemoryCardStorage
    {
        selectedSlot = clampSlot(selectedSlot);
        slots = normalizeSlots(slots);
    }

    public static final ResonatingMemoryCardStorage EMPTY = new ResonatingMemoryCardStorage(0, List.of());

    public static final Codec<Slot> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("settings_name").forGetter(Slot::settingsName),
            DataComponentMap.CODEC.fieldOf("data").forGetter(Slot::data)
    ).apply(instance, Slot::new));

    public static final Codec<ResonatingMemoryCardStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("selected_slot").forGetter(ResonatingMemoryCardStorage::selectedSlot),
            SLOT_CODEC.listOf().fieldOf("slots").forGetter(ResonatingMemoryCardStorage::slots)
    ).apply(instance, ResonatingMemoryCardStorage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Slot> SLOT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Slot::settingsName,
            ByteBufCodecs.fromCodecWithRegistries(DataComponentMap.CODEC),
            Slot::data,
            Slot::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonatingMemoryCardStorage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ResonatingMemoryCardStorage::selectedSlot,
            SLOT_STREAM_CODEC.apply(ByteBufCodecs.list(SLOT_COUNT)),
            ResonatingMemoryCardStorage::slots,
            ResonatingMemoryCardStorage::new
    );

    public Slot selected()
    {
        return slots.get(selectedSlot);
    }

    public ResonatingMemoryCardStorage withSelectedSlot(int slot)
    {
        return new ResonatingMemoryCardStorage(slot, slots);
    }

    public ResonatingMemoryCardStorage withSelectedSlotData(String settingsName, DataComponentMap data)
    {
        var copy = new java.util.ArrayList<>(slots);
        copy.set(selectedSlot, new Slot(settingsName, data));
        return new ResonatingMemoryCardStorage(selectedSlot, copy);
    }

    public ResonatingMemoryCardStorage clearSelectedSlot()
    {
        var copy = new java.util.ArrayList<>(slots);
        copy.set(selectedSlot, Slot.EMPTY);
        return new ResonatingMemoryCardStorage(selectedSlot, copy);
    }

    public static int clampSlot(int slot)
    {
        return Math.max(0, Math.min(SLOT_COUNT - 1, slot));
    }

    private static List<Slot> normalizeSlots(List<Slot> input)
    {
        var out = new java.util.ArrayList<Slot>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++)
        {
            out.add(i < input.size() ? input.get(i) : Slot.EMPTY);
        }
        return List.copyOf(out);
    }

    public record Slot(String settingsName, DataComponentMap data)
    {
        public static final Slot EMPTY = new Slot(GuiText.Blank.getTranslationKey(), DataComponentMap.EMPTY);

        public Slot
        {
            if (settingsName == null || settingsName.isEmpty())
            {
                settingsName = GuiText.Blank.getTranslationKey();
            }
            if (data == null)
            {
                data = DataComponentMap.EMPTY;
            }
        }

        public boolean isEmpty()
        {
            return data.isEmpty() || !data.has(AEComponents.EXPORTED_SETTINGS_SOURCE);
        }
    }
}
