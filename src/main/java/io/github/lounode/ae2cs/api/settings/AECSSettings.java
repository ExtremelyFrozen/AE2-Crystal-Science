package io.github.lounode.ae2cs.api.settings;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.recipes.entropy.EntropyMode;

public class AECSSettings
{
    public static final Setting<RedstoneMode> REDSTONE_CONTROLLED_NO_PULSE = Settings.register("aecs_redstone_controlled_no_pulse",
            RedstoneMode.IGNORE, RedstoneMode.HIGH_SIGNAL, RedstoneMode.LOW_SIGNAL);

    public static final Setting<ShowRangeMode> SHOW_RANGE_MODE = Settings.register("aecs_show_range_mode", ShowRangeMode.class);

    public static final Setting<BlackListMode> BLACK_LIST_MODE = Settings.register("aecs_black_list_mode", BlackListMode.class);

    public static final Setting<PullMode> PULL_MODE = Settings.register("aecs_pull_mode", PullMode.class);

    public static final Setting<EntropyMode> ENTROPY_CHANGE_MODE = Settings.register("aecs_entropy_change_mode", EntropyMode.class);
}
