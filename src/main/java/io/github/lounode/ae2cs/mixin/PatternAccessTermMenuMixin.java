package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.api.util.PatternAccessTermQuickMoveHelper;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.util.inv.FilteredInternalInventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = PatternAccessTermMenu.class, remap = false)
public abstract class PatternAccessTermMenuMixin extends AEBaseMenu {

    @Final
    @Shadow
    private Long2ObjectOpenHashMap<PatternAccessTermMenu.ContainerTracker> byId;

    @Shadow
    protected abstract boolean isVisible(PatternContainer container);

    public PatternAccessTermMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    /**
     * 使我们的陨石合成仓以及类似的方块也可以被作为Shift左键移动样板的目的地
     */
    @Inject(method = "quickMovePattern", at = @At("HEAD"), cancellable = true)
    public void quickMovePattern(ServerPlayer player, int clickedSlot, List<Long> allowedPatternContainers, CallbackInfo ci) {
        if (clickedSlot < 0 || clickedSlot >= this.slots.size()) {
            return;
        }
        Slot sourceSlot = getSlot(clickedSlot);
        if (!isPlayerSideSlot(sourceSlot)) {
            return;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (sourceStack.getCount() != 1) {
            return;
        }
        IPatternDetails pattern = PatternDetailsHelper.decodePattern(sourceStack, player.level());
        if (pattern == null) {
            return;
        }
        boolean molecularAssemblerPattern = pattern instanceof IMolecularAssemblerSupportedPattern;

        List<PatternAccessTermMenu.ContainerTracker> targets = new ArrayList<>();
        for (Long id : allowedPatternContainers) {
            PatternAccessTermMenu.ContainerTracker inv = this.byId.get(id.longValue());
            if (inv != null && isVisible(inv.container)) {
                AEItemKey icon = inv.group.icon();
                // 在这里，我们将自己的可快速转移物添加进去
                boolean molecularAssembler = icon != null && !icon.is(AEBlocks.MOLECULAR_ASSEMBLER) && PatternAccessTermQuickMoveHelper.contains(icon.getItem());
                if (molecularAssemblerPattern == molecularAssembler) {
                    targets.add(inv);
                }
            }
        }

        if (targets.stream().map(t -> t.group).distinct().count() != 1) {
            return;
        }

        for (PatternAccessTermMenu.ContainerTracker target : targets) {
            FilteredInternalInventory targetContainer = new FilteredInternalInventory(target.server, new PatternAccessTermMenu.PatternSlotFilter());
            if (targetContainer.addItems(sourceStack).isEmpty()) {
                // 成功放入一个后就结束，并取消原版逻辑
                sourceSlot.set(ItemStack.EMPTY);
                ci.cancel();
                return;
            }
        }
    }
}
