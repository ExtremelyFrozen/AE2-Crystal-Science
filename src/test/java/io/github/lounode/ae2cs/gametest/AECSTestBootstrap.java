package io.github.lounode.ae2cs.gametest;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;

import java.util.Objects;

@EventBusSubscriber(modid = AECSConstants.MODID)
public final class AECSTestBootstrap {

    private static final MutableTestFramework FRAMEWORK = FrameworkConfiguration
            .builder(AE2CrystalScience.makeId("tests"))
            .build()
            .create();

    private AECSTestBootstrap() {}

    @SubscribeEvent
    public static void initialize(FMLConstructModEvent event) {
        ModContainer container = ModList.get()
                .getModContainerById(AECSConstants.MODID)
                .orElseThrow(() -> new IllegalStateException("AECS mod container is unavailable during test setup"));
        IEventBus modBus = Objects.requireNonNull(container.getEventBus(), "AECS mod event bus");
        FRAMEWORK.init(modBus, container);
    }
}
