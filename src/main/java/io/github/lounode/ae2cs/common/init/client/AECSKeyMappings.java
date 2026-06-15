package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import com.mojang.blaze3d.platform.InputConstants;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class AECSKeyMappings {

    public static final KeyMapping TOGGLE_RESONANT_TEMPLATE_CODING_SLOT_MODE = new KeyMapping(
            "key.ae2cs.toggle_resonant_template_coding_slot_mode",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.ae2cs");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_RESONANT_TEMPLATE_CODING_SLOT_MODE);
    }
}
