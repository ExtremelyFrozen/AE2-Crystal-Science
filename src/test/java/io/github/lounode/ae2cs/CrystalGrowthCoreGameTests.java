package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.common.init.AECSItems;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

@GameTestHolder("ae2cs")
@PrefixGameTestTemplate(false)
public class CrystalGrowthCoreGameTests {

    /**
     * 走真实的服务端右键流程：对着母岩长按右键后，母岩应当被持续催生，
     * 且朝向玩家的一面长出晶芽后（视线被晶芽挡住）催生不应中断。
     */
    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void holdingCoreAcceleratesBuddingBlock(GameTestHelper helper) {
        BlockPos rock = new BlockPos(1, 3, 1);
        helper.setBlock(rock, Blocks.BUDDING_AMETHYST);
        BlockPos absolute = helper.absolutePos(rock);

        // 独立的假玩家，避免与其他并行测试共用同一个实例
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "growth_core_test"));
        Vec3 target = Vec3.atCenterOf(absolute);
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.STONE);
        player.setPos(target.x, absolute.getY() - 1, target.z + 2.0);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        player.setItemInHand(InteractionHand.MAIN_HAND, AECSItems.CRYSTAL_GROWTH_CORE.toStack());

        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        helper.assertTrue(hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(absolute),
                "Player looks at the budding block");
        var result = player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(),
                InteractionHand.MAIN_HAND, (BlockHitResult) hit);
        helper.assertTrue(result.consumesAction(), "Right-clicking a budding block with the core is consumed, got " + result);
        helper.assertTrue(player.isUsingItem(), "Player keeps using the core after the click");

        helper.onEachTick(player::doTick);
        helper.succeedWhen(() -> {
            helper.assertTrue(player.isUsingItem(), "Player is still using the core");
            // 玩家站在母岩南侧：南面先长出晶芽挡住视线，之后其他面仍要继续生长
            helper.assertFalse(helper.getBlockState(rock.relative(Direction.SOUTH)).isAir(), "No bud facing the player yet");
            int buds = 0;
            for (Direction direction : Direction.values()) {
                if (!helper.getBlockState(rock.relative(direction)).isAir()) buds++;
            }
            helper.assertTrue(buds >= 3, "Growth continues behind the bud that blocks the view, buds=" + buds);
        });
    }
}
