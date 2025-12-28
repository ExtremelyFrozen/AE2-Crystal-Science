package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class AECSAdditionalModels
{

    // 广播装置
    public static final ModelResourceLocation OFF_CORE_MODEL =
            ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/off_core"));
    public static final ModelResourceLocation SENDER_CORE_MODEL =
            ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/sender_core"));
    public static final ModelResourceLocation RECEIVER_CORE_MODEL =
            ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/receiver_core"));

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
    {
        event.register(OFF_CORE_MODEL);
        event.register(SENDER_CORE_MODEL);
        event.register(RECEIVER_CORE_MODEL);
    }
}