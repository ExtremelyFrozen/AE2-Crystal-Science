package io.github.lounode.ae2cs.core;

import appeng.core.AppEng;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Mod(value = AE2CS.MOD_ID, dist = Dist.CLIENT)
public class AE2CSClient extends AE2CSBase {
    private static final Logger LOG = LoggerFactory.getLogger(AE2CSClient.class);
    private static AE2CSClient INSTANCE;

    public AE2CSClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                postClientSetup(minecraft);
            } catch (Throwable e) {
                LOG.error("AE2CS failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });
    }

    private void postClientSetup(Minecraft minecraft) {

    }

    public static AE2CSClient instance() {
        return Objects.requireNonNull(INSTANCE, "AE2CSClient is not initialized");
    }
}
