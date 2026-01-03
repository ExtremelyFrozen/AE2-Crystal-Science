package io.github.lounode.ae2cs.common.init;

import com.mojang.serialization.Codec;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.linker.broadcast.MemoryCardBandInfo;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class AECSDataComponents
{
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AECSConstants.MODID);

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder
    )
    {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    // 生长进度
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GROW_PROCESS =
            register("grow_process", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
            );

    // 频段名称-给内存卡记录用
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MemoryCardBandInfo>> MEMORY_CARD_BAND_INFO =
            register("band_name", b -> b
                    .persistent(MemoryCardBandInfo.CODEC)
                    .networkSynchronized(MemoryCardBandInfo.STREAM_CODEC)
                    .cacheEncoding()
            );

    // 记录发信器信息坐标位置
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> ENDER_EMITTER_POS =
            register("ender_emitter_pos", b -> b
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .cacheEncoding()
            );

    /**
     * 用于存储谐振样板信息
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EncodedResonatingPattern>> ENCODED_RESONATING_PATTERN =
            register("encoded_resonating_pattern", b -> b
                    .persistent(EncodedResonatingPattern.CODEC)
                    .networkSynchronized(EncodedResonatingPattern.STREAM_CODEC)
                    .cacheEncoding()
            );

    /**
     * 用于记录当前谐振样板的目标资源索引
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RESONATING_PATTERN_SELECTED_INPUT =
            register("resonating_pattern_selected_input", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
            );


    public static void register(IEventBus eventBus)
    {
        DATA_COMPONENTS.register(eventBus);
    }
}
