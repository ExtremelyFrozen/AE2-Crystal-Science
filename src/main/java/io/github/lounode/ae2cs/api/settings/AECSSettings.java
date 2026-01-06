package io.github.lounode.ae2cs.api.settings;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;

public class AECSSettings
{
    public static final Setting<RedstoneMode> REDSTONE_CONTROLLED_NO_PULSE = Settings.register("aecs_redstone_controlled_no_pulse",
            RedstoneMode.IGNORE, RedstoneMode.HIGH_SIGNAL, RedstoneMode.LOW_SIGNAL);
}
