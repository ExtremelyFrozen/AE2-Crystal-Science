package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AECSAdditionalModels {

    // 广播装置
    public static final ResourceLocation BROADCASTER_OFF_CORE = AE2CrystalScience.makeId("block/me_ender_broadcaster/off_core");
    public static final ResourceLocation BROADCASTER_SENDER_CORE_MODEL = AE2CrystalScience.makeId("block/me_ender_broadcaster/sender_core");
    public static final ResourceLocation BROADCASTER_RECEIVER_CORE_MODEL = AE2CrystalScience.makeId("block/me_ender_broadcaster/receiver_core");

    // 发信器
    public static final ResourceLocation EMITTER_TOP_ON_MODEL = AE2CrystalScience.makeId("block/me_ender_emitter/on_top");
    public static final ResourceLocation EMITTER_TOP_OFF_MODEL = AE2CrystalScience.makeId("block/me_ender_emitter/off_top");

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(BROADCASTER_OFF_CORE);
        event.register(BROADCASTER_SENDER_CORE_MODEL);
        event.register(BROADCASTER_RECEIVER_CORE_MODEL);
        event.register(EMITTER_TOP_ON_MODEL);
        event.register(EMITTER_TOP_OFF_MODEL);
    }
}
