package io.github.lounode.ae2cs.core;

import appeng.core.AELog;
import io.github.lounode.ae2cs.common.item.AE2CSItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public abstract class AE2CSBase implements AE2CS {
    static AE2CSBase INSTANCE;

    public AE2CSBase(IEventBus modEventBus, ModContainer container) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        //AE2CSBlocks.DR.register(modEventBus);
        AE2CSItems.DR.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
                registerCreativeTabs(BuiltInRegistries.CREATIVE_MODE_TAB);
            }
        });
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                AELog.warn(err);
            }
        });
    }

    public void postRegistrationInitialization() {

    }

    public void registerCreativeTabs(Registry<CreativeModeTab> registry) {
        AE2CSMainCreativeTab.init(registry);
    }
}
