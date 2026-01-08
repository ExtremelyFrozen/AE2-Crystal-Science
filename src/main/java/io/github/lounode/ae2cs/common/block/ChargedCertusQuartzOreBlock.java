package io.github.lounode.ae2cs.common.block;

import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ChargedCertusQuartzOreBlock extends CertusQuartzOreBlock
{
    public ChargedCertusQuartzOreBlock(Properties props)
    {
        super(props);
    }

    /**
     * 模仿旧版粒子特效
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull RandomSource random)
    {
        super.animateTick(state, level, pos, random);

        if (!AEConfig.instance().isEnableEffects()) return;
        if (!AppEngClient.instance().shouldAddParticles(random)) return;

        float xOff = random.nextFloat();
        float yOff = random.nextFloat();
        float zOff = random.nextFloat();

        float eps = 0.01f;
        switch (random.nextInt(6))
        {
            case 0 -> xOff = -eps;
            case 1 -> xOff = 1.0f + eps;
            case 2 -> yOff = -eps;
            case 3 -> yOff = 1.0f + eps;
            case 4 -> zOff = -eps;
            case 5 -> zOff = 1.0f + eps;
        }

        var color = new Vector3f(0.21f, 0.61f, 1.0f);
        var dust = new DustParticleOptions(color, 0.9f);

        level.addParticle(dust,
                pos.getX() + xOff, pos.getY() + yOff, pos.getZ() + zOff,
                0.0, 0.0, 0.0);
    }
}
