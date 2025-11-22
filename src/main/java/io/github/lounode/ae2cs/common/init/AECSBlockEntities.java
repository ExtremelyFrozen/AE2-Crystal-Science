package io.github.lounode.ae2cs.common.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.ids.AECSBlockIds;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.block.entity.IntegratedInterfaceBlockEntity;
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

    /** 水晶催生仓 */
    public static final DeferredBlockEntityType<CrystalGrowthChamberBlockEntity> CRYSTAL_GROWTH_CHAMBER = create(
            AECSBlockIds.CRYSTAL_GROWTH_CHAMBER,
            CrystalGrowthChamberBlockEntity.class,
            CrystalGrowthChamberBlockEntity::new,
            AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK
    );

    /** 集成接口 */
    public static final DeferredBlockEntityType<IntegratedInterfaceBlockEntity> INTEGRATED_INTERFACE_BLOCK_ENTITY = create(
            AECSBlockIds.INTEGRATED_INTERFACE,
            IntegratedInterfaceBlockEntity.class,
            IntegratedInterfaceBlockEntity::new,
            AECSBlocks.INTEGRATED_INTERFACE_BLOCK
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
