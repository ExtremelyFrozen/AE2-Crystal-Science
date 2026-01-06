package io.github.lounode.ae2cs.api.settings;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 用于给自定义设置添加外观
 */
public final class AECSSettingAppearances
{
    private static final Map<AppearanceKey<?>, Appearance> APPEARANCES = new HashMap<>();

    static
    {
        // REDSTONE_CONTROLLED_NO_PULSE
        register(AdaptedAE2Icon.REDSTONE_IGNORE,
                AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.IGNORE,
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.title"),
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.ignore.desc"));
        register(AdaptedAE2Icon.REDSTONE_LOW,
                AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.LOW_SIGNAL,
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.title"),
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.low_signal.desc"));
        register(AdaptedAE2Icon.REDSTONE_HIGH,
                AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.HIGH_SIGNAL,
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.title"),
                Component.translatable("ae2cs.machine_settings.redstone_controlled_no_pulse.high_signal.desc"));

        register(AdaptedAE2Icon.OVERLAY_ON,
                AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.SHOW_RANGE,
                Component.translatable("ae2cs.machine_settings.show_range_mode.title"),
                Component.translatable("ae2cs.machine_settings.show_range_mode.show.desc"));
        register(AdaptedAE2Icon.OVERLAY_OFF,
                AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE,
                Component.translatable("ae2cs.machine_settings.show_range_mode.title"),
                Component.translatable("ae2cs.machine_settings.show_range_mode.hide.desc"));

        register(AECSIcon.WHITE_LIST_MODE,
                AECSSettings.BLACK_LIST_MODE, BlackListMode.WHITELIST,
                Component.translatable("ae2cs.machine_settings.black_list_mode.title"),
                Component.translatable("ae2cs.machine_settings.black_list_mode.white_list.desc"));
        register(AECSIcon.BLACK_LIST_MODE,
                AECSSettings.BLACK_LIST_MODE, BlackListMode.BLACKLIST,
                Component.translatable("ae2cs.machine_settings.black_list_mode.title"),
                Component.translatable("ae2cs.machine_settings.black_list_mode.black_list.desc"));

    }

    private AECSSettingAppearances()
    {
    }

    public static <T extends Enum<T>> void register(IButtonIcon icon, Setting<T> setting, T val,
                                                    Component title, Component... extraLines)
    {

        var lines = new ArrayList<Component>(1 + (extraLines == null ? 0 : extraLines.length));
        lines.add(title);
        if (extraLines != null && extraLines.length > 0)
        {
            Collections.addAll(lines, extraLines);
        }

        APPEARANCES.put(new AppearanceKey<>(setting, val),
                new Appearance(icon, null, List.copyOf(lines)));
    }

    public static <T extends Enum<T>> void register(ItemLike item, Setting<T> setting, T val,
                                                    Component title, Component... extraLines)
    {

        var lines = new ArrayList<Component>(1 + (extraLines == null ? 0 : extraLines.length));
        lines.add(title);
        if (extraLines != null && extraLines.length > 0)
        {
            Collections.addAll(lines, extraLines);
        }

        APPEARANCES.put(new AppearanceKey<>(setting, val),
                new Appearance(null, item.asItem(), List.copyOf(lines)));
    }

    public static <T extends Enum<T>> void register(IButtonIcon icon, Setting<T> setting, T val,
                                                    Component title, Component hintLine)
    {
        register(icon, setting, val, title, new Component[]{hintLine});
    }

    @Nullable
    public static <T extends Enum<T>> Appearance getOrNull(Setting<T> setting, T value)
    {
        return APPEARANCES.get(new AppearanceKey<>(setting, value));
    }

    public static <T extends Enum<T>> Appearance getOrDefault(Setting<T> setting, T value, Appearance fallback)
    {
        var a = getOrNull(setting, value);
        return a != null ? a : fallback;
    }

    public static void clear()
    {
        APPEARANCES.clear();
    }

    public static Map<AppearanceKey<?>, Appearance> view()
    {
        return Collections.unmodifiableMap(APPEARANCES);
    }

    public record AppearanceKey<T extends Enum<T>>(@NotNull Setting<T> setting, @NotNull T value)
    {
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (!(obj instanceof AppearanceKey<?> other)) return false;
            return this.setting == other.setting && this.value == other.value;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(setting) ^ System.identityHashCode(value);
        }
    }

    public record Appearance(@Nullable IButtonIcon icon, @Nullable Item item, @NotNull List<Component> tooltipLines)
    {
    }
}