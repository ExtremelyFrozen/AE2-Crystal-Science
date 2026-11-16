package io.github.lounode.ae2cs.common.item.upgrades;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.util.BlockStateAligner;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 谐振工厂升级：对着普通机器右键，将其原地升级为对应的谐振工厂。
 * <p>
 * 普通机器与工厂的物品栏规格、可用升级卡都不同，因此这里不做整份 NBT 照搬：
 * 能量、流体、面配置等通用数据通过 NBT 继承，物品与升级卡逐个转移，放不进工厂的部分归还玩家。
 */
public class ResonatingFactoryUpgradeItem extends Item {

    /**
     * 普通机器与工厂之间格式/语义不一致的数据，不随 NBT 继承
     */
    private static final List<String> DISCARDED_KEYS = List.of(
            "upgrades", "interface_upgrades",
            "inv_input", "inv_work", "inv_output",
            "recipe_progress", "active_recipe_id", "accelerator_target_stage");

    public ResonatingFactoryUpgradeItem(Properties properties) {
        super(properties);
    }

    /**
     * @return 该机器对应的谐振工厂，不可升级时返回 null
     */
    public static @Nullable Block getFactoryFor(Block machine) {
        if (machine == AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get()) return AECSBlocks.RESONATING_PULVERIZER_FACTORY_BLOCK.get();
        if (machine == AECSBlocks.CIRCUIT_ETCHER_BLOCK.get()) return AECSBlocks.RESONATING_CIRCUIT_ETCHER_FACTORY_BLOCK.get();
        if (machine == AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get()) return AECSBlocks.RESONATING_AGGREGATOR_FACTORY_BLOCK.get();
        if (machine == AECSBlocks.CRYSTAL_GROWTH_ACCELERATOR_BLOCK.get()) return AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK.get();
        return null;
    }

    // 机器方块自身的右键会打开界面，所以要抢在方块交互之前处理
    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState oldState = level.getBlockState(pos);
        Block factory = getFactoryFor(oldState.getBlock());
        if (factory == null || !(level.getBlockEntity(pos) instanceof IMachineHost)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!upgrade(level, pos, factory, context.getPlayer())) {
            return InteractionResult.PASS;
        }

        stack.consume(1, context.getPlayer());
        level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    /**
     * 将 pos 处的普通机器原地替换为 factory，并尽可能保留其内容。
     *
     * @return 是否完成升级
     */
    public static boolean upgrade(Level level, BlockPos pos, Block factory, @Nullable Player player) {
        BlockState oldState = level.getBlockState(pos);
        BlockEntity oldBe = level.getBlockEntity(pos);
        if (!(oldBe instanceof IMachineHost oldHost)) {
            return false;
        }

        // 快照旧机器内容
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        if (oldHost.getMachineComponents().hasService(AppEngInvComponent.class)) {
            var invs = oldHost.getMachineComponents().getService(AppEngInvComponent.class);
            InternalInventory input = invs.port(InvPort.INPUT);
            InternalInventory work = invs.port(InvPort.WORK);
            copyStacks(input, inputs);
            if (work != input) copyStacks(work, inputs);
            copyStacks(invs.port(InvPort.OUTPUT), outputs);
        }
        List<ItemStack> cards = new ArrayList<>();
        if (oldBe instanceof IUpgradeableObject upgradeable) {
            copyStacks(upgradeable.getUpgrades(), cards);
        }
        CompoundTag data = oldBe.saveWithoutMetadata(level.registryAccess());
        DISCARDED_KEYS.forEach(data::remove);

        // 替换方块
        BlockState newState = BlockStateAligner.align(oldState, factory.defaultBlockState());
        level.removeBlockEntity(pos); // 先行移除掉对应的BE，防止物品掉落
        level.removeBlock(pos, false);
        level.setBlock(pos, newState, Block.UPDATE_ALL);

        // 转移内容，放不下的归还玩家
        List<ItemStack> leftovers = new ArrayList<>();
        BlockEntity newBe = level.getBlockEntity(pos);
        if (newBe != null) {
            newBe.loadWithComponents(data, level.registryAccess());
        }
        InternalInventory newInput = null;
        InternalInventory newOutput = null;
        if (newBe instanceof IMachineHost newHost && newHost.getMachineComponents().hasService(AppEngInvComponent.class)) {
            var invs = newHost.getMachineComponents().getService(AppEngInvComponent.class);
            newInput = invs.port(InvPort.INPUT);
            newOutput = invs.port(InvPort.OUTPUT);
        }
        // 升级卡要先装，堆叠卡会影响工厂槽位容量
        moveStacks(cards, newBe instanceof IUpgradeableObject upgradeable ? upgradeable.getUpgrades() : null, leftovers);
        moveStacks(inputs, newInput, leftovers);
        moveStacks(outputs, newOutput, leftovers);

        for (ItemStack stack : leftovers) {
            if (player != null) {
                player.getInventory().placeItemBackInInventory(stack);
            } else {
                Block.popResource(level, pos, stack);
            }
        }

        if (newBe != null) {
            newBe.setChanged();
            level.sendBlockUpdated(pos, newState, newState, Block.UPDATE_ALL);
        }
        return true;
    }

    private static void copyStacks(@Nullable InternalInventory inv, List<ItemStack> target) {
        if (inv == null) return;
        for (ItemStack stack : inv) {
            if (!stack.isEmpty()) target.add(stack.copy());
        }
    }

    private static void moveStacks(List<ItemStack> stacks, @Nullable InternalInventory target, List<ItemStack> leftovers) {
        for (ItemStack stack : stacks) {
            ItemStack remainder = target == null ? stack : target.addItems(stack);
            if (!remainder.isEmpty()) leftovers.add(remainder);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("ae2cs.item.resonating_factory_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
