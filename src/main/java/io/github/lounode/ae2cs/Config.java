package io.github.lounode.ae2cs;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public final Config.StartUpConfig startUpConfig = new Config.StartUpConfig();
    public final Config.CommonConfig commonConfig = new Config.CommonConfig();

    public static Config INSTANCE;

    private Config(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, startUpConfig.spec);
        container.registerConfig(ModConfig.Type.COMMON, commonConfig.spec);
        container.getEventBus().addListener((ModConfigEvent.Loading evt) -> {
            if (evt.getConfig().getSpec() == commonConfig.spec) {
                commonConfig.onLoaded();
            }
        });
        container.getEventBus().addListener((ModConfigEvent.Reloading evt) -> {
            if (evt.getConfig().getSpec() == commonConfig.spec) {
                commonConfig.onLoaded();
            }
        });
    }

    public static void register(ModContainer container) {
        INSTANCE = new Config(container);
    }

    public static class StartUpConfig {

        public final ModConfigSpec spec;

        // 末影发信器自动范围系数
        public final ModConfigSpec.IntValue enderEmitterAutoAreaFactor;
        // 高纯水晶能量系数
        public final ModConfigSpec.DoubleValue pureCrystalBurnMultiplier;

        public StartUpConfig() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            enderEmitterAutoAreaFactor = builder.comment(
                    "Controls the auto-link search area for the Ender Emitter.",
                    "Final area size is computed as: 16 * value.")
                    .defineInRange("ender_emitter_auto_area_factor", 1, 1, 100);

            pureCrystalBurnMultiplier = builder.comment(
                    "Global multiplier applied to Pure Crystal energy generation rate (AE/t).",
                    "Final AE/t = baseAEPerTick * multiplier.",
                    "Range: 0.01..100 (default: 1).")
                    .defineInRange("pure_crystal_burn_multiplier", 1d, 0.01d, 100d);

            this.spec = builder.build();
        }
    }

    public static class CommonConfig {

        public final ModConfigSpec spec;

        public CommonConfig() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            this.spec = builder.build();
        }

        public void onLoaded() {}
    }
}
