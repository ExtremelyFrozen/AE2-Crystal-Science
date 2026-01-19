package io.github.lounode.ae2cs;


import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class Config
{
    public final Config.CommonConfig commonConfig = new Config.CommonConfig();

    public static Config INSTANCE;

    private Config(ModLoadingContext container, IEventBus modEventBus)
    {
        container.registerConfig(ModConfig.Type.COMMON, commonConfig.spec);
        modEventBus.addListener((ModConfigEvent.Loading evt) ->
        {
            if (evt.getConfig().getSpec() == commonConfig.spec)
            {
                commonConfig.onLoaded();
            }
        });
        modEventBus.addListener((ModConfigEvent.Reloading evt) ->
        {
            if (evt.getConfig().getSpec() == commonConfig.spec)
            {
                commonConfig.onLoaded();
            }
        });
    }

    public static void register(ModLoadingContext container, IEventBus modEventBus)
    {
        INSTANCE = new Config(container, modEventBus);
    }

    public static class CommonConfig
    {
        public final ForgeConfigSpec spec;

        // 末影发信器自动范围系数
        public final ForgeConfigSpec.IntValue enderEmitterAutoAreaFactor;
        // 高纯水晶能量系数
        public final ForgeConfigSpec.DoubleValue pureCrystalBurnMultiplier;

        public CommonConfig()
        {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            enderEmitterAutoAreaFactor =
                    builder.comment(
                                    "Controls the auto-link search area for the Ender Emitter.",
                                    "Final area size is computed as: 16 * value."
                            )
                            .defineInRange("ender_emitter_auto_area_factor", 1, 1, 100);

            pureCrystalBurnMultiplier =
                    builder.comment(
                                    "Global multiplier applied to Pure Crystal energy generation rate (AE/t).",
                                    "Final AE/t = baseAEPerTick * multiplier.",
                                    "Range: 0.01..100 (default: 1)."
                            )
                            .defineInRange("pure_crystal_burn_multiplier", 1d, 0.01d, 100d);

            this.spec = builder.build();
        }

        public void onLoaded()
        {
            EnderEmitterBlockEntity.autoAreaFactor = enderEmitterAutoAreaFactor.get();
            PureCrystalItem.energyMultiplier = pureCrystalBurnMultiplier.get();
        }
    }
}
