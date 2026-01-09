package io.github.lounode.ae2cs.common.init;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.component.EnergyComponent;
import io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent;
import io.github.lounode.ae2cs.common.me.part.*;
import net.minecraft.core.Direction;
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
        IntegratedInterfaceBlockEntity.onRegisterCaps(event);
        QuartzGrindstoneBlockEntity.onRegisterCaps(event);
        MeteoritePatternProviderBlockEntity.onRegisterCaps(event);
        SimplePatternProviderBlockEntity.onRegisterCaps(event);
        EnderInterfaceBlockEntity.onRegisterCaps(event);
        ResonatingPatternProviderBlockEntity.onRegisterCaps(event);

        for (BlockEntityType<?> beType : AECSBlockEntities.getImplementorsOf(IInWorldGridNodeHost.class))
        {
            event.registerBlockEntity(
                    AECapabilities.IN_WORLD_GRID_NODE_HOST,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IInWorldGridNodeHost inWorldGridNodeHost) return inWorldGridNodeHost;
                        else return null;
                    }
            );
        }
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
        for (BlockEntityType<?> beType : AECSBlockEntities.getImplementorsOf(IMachineHost.class))
        {
            event.registerBlockEntity(
                    AECapabilities.GENERIC_INTERNAL_INV,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IMachineHost host && host.getMachineComponents().hasService(GenericStackInvComponent.class))
                        {
                            return host.getMachineComponents().getService(GenericStackInvComponent.class).combined();
                        }
                        return null;
                    }
            );
        }

        event.registerBlockEntity(
                AECapabilities.CRANKABLE,
                AECSBlockEntities.QUARTZ_GRINDSTONE_BLOCK_ENTITY.get(),
                (be, direction) -> {
                    if (direction == Direction.UP) return be;
                    else return null;
                }
        );
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
