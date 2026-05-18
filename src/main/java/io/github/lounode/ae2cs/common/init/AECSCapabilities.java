package io.github.lounode.ae2cs.common.init;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import io.github.lounode.ae2cs.common.me.part.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class AECSCapabilities
{
    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        IntegratedInterfaceBlockEntity.onRegisterCaps(event);
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
        for (BlockEntityType<?> beType : AECSBlockEntities.getAnnotatedWith(EnergyHandler.class))
        {
            event.registerBlockEntity(
                    Capabilities.Energy.BLOCK,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IMachineHost host && host.getMachineComponents().hasService(EnergyHandler.class))
                        {
                            return host.getMachineComponents().getService(EnergyHandler.class);
                        }
                        return null;
                    }
            );
        }
        for (BlockEntityType<?> beType : AECSBlockEntities.getAnnotatedWith(ItemResource.class))
        {
            event.registerBlockEntity(
                    Capabilities.Item.BLOCK,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IMachineHost host && host.getMachineComponents().hasService(AppEngInvComponent.class))
                        {
                            if (host.getMachineComponents().hasService(SideConfigComponent.class))
                            {
                                BaseInternalInventory inv = host.getMachineComponents().getService(SideConfigComponent.class).appEngInvForSide(direction);
                                if (inv != null)
                                    return inv.toResourceHandler();
                                else
                                    return null;
                            }
                            else
                            {
                                return host.getMachineComponents().getService(AppEngInvComponent.class).combined().toResourceHandler();
                            }
                        }
                        return null;
                    }
            );
        }
        for (BlockEntityType<?> beType : AECSBlockEntities.getAnnotatedWith(GenericInternalInventory.class))
        {
            event.registerBlockEntity(
                    AECapabilities.GENERIC_INTERNAL_INV,
                    beType,
                    (be, direction) -> {
                        if (be instanceof IMachineHost host && host.getMachineComponents().hasService(GenericStackInvComponent.class))
                        {
                            if (host.getMachineComponents().hasService(SideConfigComponent.class))
                            {
                                return host.getMachineComponents().getService(SideConfigComponent.class).genericInvForSide(direction);
                            }
                            else
                            {
                                return host.getMachineComponents().getService(GenericStackInvComponent.class).combined();
                            }
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
