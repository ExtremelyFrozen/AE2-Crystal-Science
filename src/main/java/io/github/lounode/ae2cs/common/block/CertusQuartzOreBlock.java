package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CertusQuartzOreBlock extends AEBaseBlock
{
    public CertusQuartzOreBlock(Properties props)
    {
        super(props);
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel)
    {
        if (!(level instanceof ServerLevelAccessor serverLevel)) return 0;

        if (silkTouchLevel > 0)
        {
            return 0;
        }
        RandomSource rand = serverLevel.getRandom();
        return Mth.nextInt(rand, 2, 5);
    }
}
