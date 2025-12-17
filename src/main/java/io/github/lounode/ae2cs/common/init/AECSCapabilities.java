package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class AECSCapabilities
{
    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        CrystalGrowthChamberBlockEntity.onRegisterCaps(event);
        IntegratedInterfaceBlockEntity.onRegisterCaps(event);
        CrystalVibrationChamberBlockEntity.onRegisterCaps(event);
        CircuitEtcherBlockEntity.onRegisterCaps(event);
        CrystalPulverizerBlockEntity.onRegisterCaps(event);
        QuartzGrindstoneBlockEntity.onRegisterCaps(event);
        MeteoriteCrafterBlockEntity.onRegisterCaps(event);
    }
}
