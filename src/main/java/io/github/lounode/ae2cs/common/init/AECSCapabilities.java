package io.github.lounode.ae2cs.common.init;

import appeng.api.parts.RegisterPartCapabilitiesEvent;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.component.EnergyComponent;
import io.github.lounode.ae2cs.common.me.part.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
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
        CrystalAggregatorBlockEntity.onRegisterCaps(event);
        EnderBroadcasterBlockEntity.onRegisterCaps(event);
        EnderEmitterBlockEntity.onRegisterCaps(event);
        EnderInterfaceBlockEntity.onRegisterCaps(event);
        ResonatingPatternProviderBlockEntity.onRegisterCaps(event);
        EntropyVariationReactionChamberBlockEntity.onRegisterCaps(event);
        QuartzOscillatorClockBlockEntity.onRegisterCaps(event);

        for (BlockEntityType<?> beType : AECSBlockEntities.getImplementorsOf(IMachineHost.class))
        {
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IMachineHost host && host.getMachineComponents().hasService(EnergyComponent.class))
                        {
                            return host.getMachineComponents().getService(EnergyComponent.class).getForgeEnergyAdapter();
                        }
                        return null;
                    }
            );
        }
    }

    @SubscribeEvent
    public static void registerPartCaps(RegisterPartCapabilitiesEvent event)
    {
        IntegratedInterfacePart.onRegisterCaps(event);
        MeteoritePatternProviderPart.onRegisterCaps(event);
        SimplePatternProviderPart.onRegisterCaps(event);
        EnderInterfacePart.onRegisterCaps(event);
        ResonatingPatternProviderPart.onRegisterCaps(event);
    }
}
