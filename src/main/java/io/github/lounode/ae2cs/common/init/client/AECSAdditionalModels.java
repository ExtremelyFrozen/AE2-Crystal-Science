package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public final class AECSAdditionalModels
{

    // 广播装置
    public static Identifier BROADCASTER_OFF_CORE_ID = AE2CrystalScience.makeId("block/me_ender_broadcaster/off_core");
    public static final StandaloneModelKey<BlockStateModelPart> BROADCASTER_OFF_CORE =
            new StandaloneModelKey<>(BROADCASTER_OFF_CORE_ID::toString);

    public static Identifier BROADCASTER_SENDER_CORE_MODEL_ID = AE2CrystalScience.makeId("block/me_ender_broadcaster/sender_core");
    public static final StandaloneModelKey<BlockStateModelPart> BROADCASTER_SENDER_CORE_MODEL =
            new StandaloneModelKey<>(BROADCASTER_SENDER_CORE_MODEL_ID::toString);

    public static Identifier BROADCASTER_RECEIVER_CORE_MODEL_ID = AE2CrystalScience.makeId("block/me_ender_broadcaster/receiver_core");
    public static final StandaloneModelKey<BlockStateModelPart> BROADCASTER_RECEIVER_CORE_MODEL =
            new StandaloneModelKey<>(BROADCASTER_RECEIVER_CORE_MODEL_ID::toString);

    // 发信器
    public static Identifier EMITTER_TOP_ON_MODEL_ID = AE2CrystalScience.makeId("block/me_ender_emitter/on_top");
    public static final StandaloneModelKey<BlockStateModelPart> EMITTER_TOP_ON_MODEL =
            new StandaloneModelKey<>(EMITTER_TOP_ON_MODEL_ID::toString);

    public static Identifier EMITTER_TOP_OFF_MODEL_ID = AE2CrystalScience.makeId("block/me_ender_emitter/off_top");
    public static final StandaloneModelKey<BlockStateModelPart> EMITTER_TOP_OFF_MODEL =
            new StandaloneModelKey<>(EMITTER_TOP_OFF_MODEL_ID::toString);

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterStandalone event)
    {
        event.register(BROADCASTER_OFF_CORE, SimpleUnbakedStandaloneModel.simpleModelWrapper(BROADCASTER_OFF_CORE_ID));
        event.register(BROADCASTER_SENDER_CORE_MODEL, SimpleUnbakedStandaloneModel.simpleModelWrapper(BROADCASTER_SENDER_CORE_MODEL_ID));
        event.register(BROADCASTER_RECEIVER_CORE_MODEL, SimpleUnbakedStandaloneModel.simpleModelWrapper(BROADCASTER_RECEIVER_CORE_MODEL_ID));
        event.register(EMITTER_TOP_ON_MODEL, SimpleUnbakedStandaloneModel.simpleModelWrapper(EMITTER_TOP_ON_MODEL_ID));
        event.register(EMITTER_TOP_OFF_MODEL, SimpleUnbakedStandaloneModel.simpleModelWrapper(EMITTER_TOP_OFF_MODEL_ID));
    }
}