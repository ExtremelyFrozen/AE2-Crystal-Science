package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.init.AECSBlockEntities;

import appeng.api.config.AccessRestriction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 充能共振发电机的方块实体。
 *
 * <p>
 * 能力来自父类：{@link AENetworkedSelfPoweredBlockEntity} 已经接了 AE 网络能量存储与 Forge 能量存储，
 * 无需在这里重复声明。
 * </p>
 *
 * <p>
 * 产能规则尚未确定，因此这里还没有自己的 tick 逻辑；等玩法定下来再补。
 * </p>
 */
public class ChargedResonatingGeneratorBlockEntity extends AENetworkedSelfPoweredBlockEntity {

    public ChargedResonatingGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(AECSBlockEntities.CHARGED_RESONATING_GENERATOR_BLOCK_ENTITY.get(), pos, blockState,
                1000000, false, AccessRestriction.READ);
        // AE2 的网格节点默认空闲功耗是 1 AE/t，而本模组的机器都是零空闲功耗；
        // 不置 0 的话，这台还没有产能逻辑的机器会白拿网络 1 AE/t。
        this.getMainNode().setIdlePowerUsage(0);
    }
}
