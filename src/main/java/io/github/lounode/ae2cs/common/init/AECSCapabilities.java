package io.github.lounode.ae2cs.common.init;

import appeng.api.parts.RegisterPartCapabilitiesEvent;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.me.part.IntegratedInterfacePart;
import io.github.lounode.ae2cs.common.me.part.MeteoritePatternProviderPart;
import io.github.lounode.ae2cs.common.me.part.SimplePatternProviderPart;
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
        MeteoritePatternProviderBlockEntity.onRegisterCaps(event);
        SimplePatternProviderBlockEntity.onRegisterCaps(event);
    }

    @SubscribeEvent
    public static void registerPartCaps(RegisterPartCapabilitiesEvent event)
    {
        IntegratedInterfacePart.onRegisterCaps(event);
        MeteoritePatternProviderPart.onRegisterCaps(event);
        SimplePatternProviderPart.onRegisterCaps(event);
    }
}
