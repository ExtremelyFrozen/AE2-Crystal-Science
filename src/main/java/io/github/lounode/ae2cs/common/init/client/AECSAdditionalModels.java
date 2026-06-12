package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class AECSAdditionalModels {

    // 广播装置
    public static final ModelResourceLocation BROADCASTER_OFF_CORE = ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/off_core"));
    public static final ModelResourceLocation BROADCASTER_SENDER_CORE_MODEL = ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/sender_core"));
    public static final ModelResourceLocation BROADCASTER_RECEIVER_CORE_MODEL = ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_broadcaster/receiver_core"));

    // 发信器
    public static final ModelResourceLocation EMITTER_TOP_ON_MODEL = ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_emitter/on_top"));

    public static final ModelResourceLocation EMITTER_TOP_OFF_MODEL = ModelResourceLocation.standalone(AE2CrystalScience.makeId("block/me_ender_emitter/off_top"));

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(BROADCASTER_OFF_CORE);
        event.register(BROADCASTER_SENDER_CORE_MODEL);
        event.register(BROADCASTER_RECEIVER_CORE_MODEL);
        event.register(EMITTER_TOP_ON_MODEL);
        event.register(EMITTER_TOP_OFF_MODEL);
    }
}
