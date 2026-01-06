package io.github.lounode.ae2cs.common.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AECSBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AECSConstants.MODID);

    /**
     * 注册的所有方块实体
     */
    private static final List<DeferredBlockEntityType<?>> ALL = new ArrayList<>();

    /**
     * 水晶催生仓
     */
    public static final DeferredBlockEntityType<CrystalGrowthChamberBlockEntity> CRYSTAL_GROWTH_CHAMBER = create(
            AECSBlockIds.CRYSTAL_GROWTH_CHAMBER,
            CrystalGrowthChamberBlockEntity.class,
            CrystalGrowthChamberBlockEntity::new,
            AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK
    );

    /**
     * 集成接口
     */
    public static final DeferredBlockEntityType<IntegratedInterfaceBlockEntity> INTEGRATED_INTERFACE_BLOCK_ENTITY = create(
            AECSBlockIds.INTEGRATED_INTERFACE,
            IntegratedInterfaceBlockEntity.class,
            IntegratedInterfaceBlockEntity::new,
            AECSBlocks.INTEGRATED_INTERFACE_BLOCK
    );

    /**
     * 晶能谐振器
     */
    public static final DeferredBlockEntityType<CrystalVibrationChamberBlockEntity> CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY = create(
            AECSBlockIds.CRYSTAL_VIBRATION_CHAMBER,
            CrystalVibrationChamberBlockEntity.class,
            CrystalVibrationChamberBlockEntity::new,
            AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK
    );

    /**
     * 电路蚀刻器
     */
    public static final DeferredBlockEntityType<CircuitEtcherBlockEntity> CIRCUIT_ETCHER_BLOCK_ENTITY = create(
            AECSBlockIds.CIRCUIT_ETCHER,
            CircuitEtcherBlockEntity.class,
            CircuitEtcherBlockEntity::new,
            AECSBlocks.CIRCUIT_ETCHER_BLOCK
    );

    /**
     * 陨石样板供应器
     */
    public static final DeferredBlockEntityType<MeteoritePatternProviderBlockEntity> METEORITE_PATTERN_PROVIDER_BLOCK_ENTITY = create(
            AECSBlockIds.METEORITE_PATTERN_PROVIDER,
            MeteoritePatternProviderBlockEntity.class,
            MeteoritePatternProviderBlockEntity::new,
            AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK
    );

    /**
     * 晶能粉碎机
     */
    public static final DeferredBlockEntityType<CrystalPulverizerBlockEntity> CRYSTAL_PULVERIZER_BLOCK_ENTITY = create(
            AECSBlockIds.CRYSTAL_PULVERIZER,
            CrystalPulverizerBlockEntity.class,
            CrystalPulverizerBlockEntity::new,
            AECSBlocks.CRYSTAL_PULVERIZER_BLOCK
    );

    /**
     * 石英磨具
     */
    public static final DeferredBlockEntityType<QuartzGrindstoneBlockEntity> QUARTZ_GRINDSTONE_BLOCK_ENTITY = create(
            AECSBlockIds.QUARTZ_GRINDSTONE,
            QuartzGrindstoneBlockEntity.class,
            QuartzGrindstoneBlockEntity::new,
            AECSBlocks.QUARTZ_GRINDSTONE_BLOCK
    );

    /**
     * 初级样板供应器
     */
    public static final DeferredBlockEntityType<SimplePatternProviderBlockEntity> SIMPLE_PATTERN_PROVIDER_BLOCK_ENTITY = create(
            AECSBlockIds.SIMPLE_PATTERN_PROVIDER,
            SimplePatternProviderBlockEntity.class,
            SimplePatternProviderBlockEntity::new,
            AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK
    );

    /**
     * 水晶聚合器
     */
    public static final DeferredBlockEntityType<CrystalAggregatorBlockEntity> CRYSTAL_AGGREGATOR_BLOCK_ENTITY = create(
            AECSBlockIds.CRYSTAL_AGGREGATOR,
            CrystalAggregatorBlockEntity.class,
            CrystalAggregatorBlockEntity::new,
            AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK
    );

    /**
     * 末影广播装置
     */
    public static final DeferredBlockEntityType<EnderBroadcasterBlockEntity> ENDER_BROADCASTER_BLOCK_ENTITY = create(
            AECSBlockIds.ENDER_BROADCASTER,
            EnderBroadcasterBlockEntity.class,
            EnderBroadcasterBlockEntity::new,
            AECSBlocks.ENDER_BROADCASTER_BLOCK
    );

    /**
     * 末影广播装置
     */
    public static final DeferredBlockEntityType<EnderEmitterBlockEntity> ENDER_EMITTER_BLOCK_ENTITY = create(
            AECSBlockIds.ENDER_EMITTER,
            EnderEmitterBlockEntity.class,
            EnderEmitterBlockEntity::new,
            AECSBlocks.ENDER_EMITTER_BLOCK
    );

    /**
     * 末影接口
     */
    public static final DeferredBlockEntityType<EnderInterfaceBlockEntity> ENDER_INTERFACE_BLOCK_ENTITY = create(
            AECSBlockIds.ENDER_INTERFACE,
            EnderInterfaceBlockEntity.class,
            EnderInterfaceBlockEntity::new,
            AECSBlocks.ENDER_INTERFACE_BLOCK
    );

    /**
     * 谐振样板供应器
     */
    public static final DeferredBlockEntityType<ResonatingPatternProviderBlockEntity> RESONATING_PATTERN_PROVIDER_BLOCK_ENTITY = create(
            AECSBlockIds.RESONATING_PATTERN_PROVIDER,
            ResonatingPatternProviderBlockEntity.class,
            ResonatingPatternProviderBlockEntity::new,
            AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK
    );

    /**
     * 熵变反应仓
     */
    public static final DeferredBlockEntityType<EntropyVariationReactionChamberBlockEntity> ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK_ENTITY = create(
            AECSBlockIds.ENTROPY_VARIATION_REACTION_CHAMBER,
            EntropyVariationReactionChamberBlockEntity.class,
            EntropyVariationReactionChamberBlockEntity::new,
            AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK
    );

    /**
     * 石英震荡器
     */
    public static final DeferredBlockEntityType<QuartzOscillatorClockBlockEntity> QUARTZ_OSCILLATOR_CLOCK_BLOCK_ENTITY = create(
            AECSBlockIds.QUARTZ_OSCILLATOR_CLOCK,
            QuartzOscillatorClockBlockEntity.class,
            QuartzOscillatorClockBlockEntity::new,
            AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK
    );

    /**
     * 注册监听
     */
    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    // 工具方法

    /**
     * 用于AEBaseBlockEntity注册，自动附加ticker并绑定物品型态以用于AE的一些机制
     */
    @SafeVarargs
    public static <T extends AEBaseBlockEntity> DeferredBlockEntityType<T> create(
            String id,
            Class<T> entityClass,
            BlockEntityFactory<T> factory,
            Supplier<? extends AEBaseEntityBlock<?>>... blockSuppliers
    )
    {
        Preconditions.checkArgument(blockSuppliers.length > 0, "At least one block is required");

        var deferred = BLOCK_ENTITY_TYPES.register(id, () -> {
            // 把 BlockEntityType 反向塞入 BE 构造用
            AtomicReference<BlockEntityType<T>> typeRef = new AtomicReference<>();

            BlockEntityType.BlockEntitySupplier<T> supplier = (pos, state) ->
                    factory.create(typeRef.get(), pos, state);

            // 解包方块
            AEBaseEntityBlock<?>[] blocks = Arrays.stream(blockSuppliers)
                    .map(Supplier::get)
                    .toArray(AEBaseEntityBlock[]::new);

            // 构造 BE 类型
            @SuppressWarnings("unchecked")
            Block[] vanillaBlocks = Arrays.stream(blocks).toArray(Block[]::new);

            BlockEntityType<T> type = BlockEntityType.Builder.of(supplier, vanillaBlocks).build(null);
            // 让上面的 supplier 拿得到 type
            typeRef.set(type);

            // 把“物品形态”绑定给这个 BE 类型（AE 用于记忆卡/设置导入导出/tooltip 等）
            try
            {
                var item = blocks[0].asItem();
                AEBaseBlockEntity.registerBlockEntityItem(type, item);
            }
            catch (Throwable ignored)
            {
            }

            // 自动生成 tickers（如果实现了 AE 的标记接口）
            BlockEntityTicker<T> serverTicker = null;
            if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass))
            {
                serverTicker = (lvl, p, st, be) -> ((ServerTickingBlockEntity) be).serverTick();
            }
            BlockEntityTicker<T> clientTicker = null;
            if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass))
            {
                clientTicker = (lvl, p, st, be) -> ((ClientTickingBlockEntity) be).clientTick();
            }

            // 把 “type + tickers + 具体 BE 类” 绑定到每个方块上
            for (var b : blocks)
            {
                @SuppressWarnings("unchecked")
                AEBaseEntityBlock<T> base = (AEBaseEntityBlock<T>) b;
                base.setBlockEntity(entityClass, type, clientTicker, serverTicker);
            }

            return type;
        });

        var wrapped = new DeferredBlockEntityType<>(entityClass, deferred);
        ALL.add(wrapped);
        return wrapped;
    }

    /**
     * 便捷重载：如果 BE 构造是(BlockPos, BlockState)用这个。
     * 内部会忽略 type，把二参构造包装为三参工厂。
     */
    @SafeVarargs
    public static <T extends AEBaseBlockEntity> DeferredBlockEntityType<T> create(
            String id,
            Class<T> entityClass,
            BiFunction<BlockPos, BlockState, T> ctorPosState,
            Supplier<? extends AEBaseEntityBlock<?>>... blockSuppliers
    )
    {
        return create(id, entityClass, (type, pos, state) -> ctorPosState.apply(pos, state), blockSuppliers);
    }

    /**
     * 返回实现类是 baseClass 的所有 BlockEntityType
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> List<BlockEntityType<? extends T>> getSubclassesOf(Class<T> baseClass)
    {
        var result = new ArrayList<BlockEntityType<? extends T>>();
        for (var t : ALL)
        {
            if (baseClass.isAssignableFrom(t.getBlockEntityClass()))
            {
                result.add((BlockEntityType<? extends T>) t.get());
            }
        }
        return result;
    }

    /**
     * 返回实现了某接口的所有 BlockEntityType
     * AE用类似方法配合ALL列表统一注册能力，但我更喜欢手动管理
     */
    public static List<BlockEntityType<?>> getImplementorsOf(Class<?> iface)
    {
        var result = new ArrayList<BlockEntityType<?>>();
        for (var t : ALL)
        {
            if (iface.isAssignableFrom(t.getBlockEntityClass()))
            {
                result.add(t.get());
            }
        }
        return result;
    }


    /**
     * 包装类型
     */
    public static final class DeferredBlockEntityType<T extends BlockEntity> implements Supplier<BlockEntityType<T>>
    {
        private final Class<T> entityClass;
        private final Supplier<BlockEntityType<T>> delegate;

        private DeferredBlockEntityType(Class<T> cls, Supplier<BlockEntityType<T>> delegate)
        {
            this.entityClass = cls;
            this.delegate = delegate;
        }

        public BlockEntityType<T> get()
        {
            return delegate.get();
        }

        public Class<T> getBlockEntityClass()
        {
            return entityClass;
        }
    }

    /**
     * 三参构造版接口
     */
    @FunctionalInterface
    public interface BlockEntityFactory<T extends AEBaseBlockEntity>
    {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
