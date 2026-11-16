package io.github.lounode.ae2cs.integration.dataenergistics.production;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.helpers.externalstorage.GenericStackInv;

import net.minecraft.world.item.ItemStack;

import com.fish_dan_.data_energistics.api.upgrade.production.MultiplicationCardOutputs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/** Optional server-side production wrapper. Native method signatures do not depend on DE classes. */
@NullMarked
public final class CrystalScienceProduction {

    private CrystalScienceProduction() {}

    public static ItemStack boost(ItemStack output, IUpgradeInventory upgrades) {
        return MultiplicationCardOutputs.boost(output, upgrades);
    }

    public static double boostPower(double power, IUpgradeInventory upgrades) {
        return MultiplicationCardOutputs.boostPower(power, upgrades);
    }

    public static List<GenericStack> boost(List<GenericStack> outputs, IUpgradeInventory upgrades) {
        if (!MultiplicationCardOutputs.isInstalled(upgrades)) {
            return outputs;
        }
        var boosted = new ObjectArrayList<GenericStack>(outputs.size());
        for (var output : outputs) {
            boosted.add(new GenericStack(output.what(), MultiplicationCardOutputs.boostAmount(output.amount(), upgrades)));
        }
        return boosted;
    }

    public static ItemStack insert(InternalInventory inventory, ItemStack output, boolean simulate,
                                   IUpgradeInventory upgrades) {
        return MultiplicationCardOutputs.isInstalled(upgrades) ? CrystalScienceItemOutput.insert(inventory, output, simulate, upgrades) : inventory.addItems(output, simulate);
    }

    public static ItemStack insert(InternalInventory inventory, int slot, ItemStack output, boolean simulate,
                                   IUpgradeInventory upgrades) {
        return MultiplicationCardOutputs.isInstalled(upgrades) ? MultiplicationCardOutputs.insert(inventory, slot, output, simulate, upgrades) : inventory.insertItem(slot, output, simulate);
    }

    public static boolean canInsertAll(GenericStackInv inventory, List<GenericStack> outputs,
                                       IActionSource source, IUpgradeInventory upgrades) {
        return !MultiplicationCardOutputs.isInstalled(upgrades) || CrystalScienceOutputBatch.canInsertAll(inventory, outputs, source);
    }

    public static void restore(InternalInventory inventory, int slot, ItemStack remainder) {
        MultiplicationCardOutputs.restore(inventory, slot, remainder, false);
    }
}
