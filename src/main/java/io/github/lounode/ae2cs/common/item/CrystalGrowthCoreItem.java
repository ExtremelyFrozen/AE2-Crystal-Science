package io.github.lounode.ae2cs.common.item;

import appeng.api.ids.AETags;
import appeng.core.AEConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 晶体催生核心：手持并对着可催生方块（母岩、作物等）长按右键时，
 * 该方块获得等同于一台晶体催生器的加速。
 */
public class CrystalGrowthCoreItem extends Item {

    private static final int USE_DURATION = 72000;
    private static final int PARTICLE_INTERVAL = 4;

    public CrystalGrowthCoreItem(Properties properties) {
        super(properties);
    }

    public static boolean canAccelerate(BlockState state) {
        return state.is(AETags.GROWTH_ACCELERATABLE);
    }

    /**
     * 解析准星实际想催生的方块。
     * <p>
     * 除了直接指向可催生方块外，还允许指向它身上长出的晶芽（母岩在晶芽背后），
     * 或指向作物脚下的耕地（作物在点击面的外侧），否则母岩一长出晶芽就会被挡住视线。
     */
    public static @Nullable BlockPos findTarget(Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        if (canAccelerate(level.getBlockState(pos))) {
            return pos;
        }
        BlockPos behind = pos.relative(hit.getDirection().getOpposite());
        if (canAccelerate(level.getBlockState(behind))) {
            return behind;
        }
        BlockPos front = pos.relative(hit.getDirection());
        if (canAccelerate(level.getBlockState(front))) {
            return front;
        }
        return null;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        if (findTarget(context.getLevel(), hit) == null) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity living, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof Player player)) {
            living.releaseUsingItem();
            return;
        }

        // 以玩家当前准星所指的方块为准，移开视线即停止催生
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        BlockPos pos = hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit ? findTarget(level, blockHit) : null;
        if (pos == null) {
            living.releaseUsingItem();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int elapsed = getUseDuration(stack, living) - remainingUseDuration;
        if (elapsed % PARTICLE_INTERVAL == 0) {
            // 粒子撒在方块外沿一圈，避免生成在实心方块内部看不见
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    4, 0.55, 0.55, 0.55, 0.0);
        }

        // 与晶体催生器保持同一节拍：每 N tick 对目标方块追加一次随机刻
        int interval = Math.max(1, AEConfig.instance().getGrowthAcceleratorSpeed());
        if (elapsed > 0 && elapsed % interval == 0) {
            level.getBlockState(pos).randomTick(serverLevel, pos, serverLevel.getRandom());
            if (elapsed % (interval * 2) == 0) {
                serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.4F,
                        0.8F + serverLevel.getRandom().nextFloat() * 0.4F);
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("ae2cs.item.crystal_growth_core.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
