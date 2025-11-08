package io.github.lounode.ae2cs.core;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AE2CS.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class AE2CSServer extends AE2CSBase {
    public AE2CSServer(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }
}
