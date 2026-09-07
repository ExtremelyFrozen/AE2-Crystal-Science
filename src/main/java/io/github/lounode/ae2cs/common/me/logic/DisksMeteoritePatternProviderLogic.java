package io.github.lounode.ae2cs.common.me.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IManagedGridNode;

import io.github.lounode.ae2cs.common.block.entity.MeteoritePatternProviderBlockEntity;

/**
 * A {@link MeteoritePatternProviderLogic} whose available patterns are the union of vanilla patterns and
 * expanded <b>pattern disks</b> (AE2 Pattern Disk addon) — the "one slot, two uses" approach.
 *
 * <p><b>Sealed contract:</b> AECS carries its own copy of the disk content format ({@link DiskContents}).
 * The addon's {@code ae2_pattern_disk:disk_contents} data component value is read via the component's own
 * codec (encode to NBT, then decode with our format copy) — no reflective calls, no class dependency. The
 * two mods only share the <i>formats</i> ({@code type}/{@code capacity}/{@code patterns}), so either side
 * may evolve its parsing independently.</p>
 *
 * <p>The disk algorithm is active only when the addon is loaded ({@link #isAddonLoaded()}); otherwise the
 * provider behaves exactly like the vanilla one.</p>
 */
public class DisksMeteoritePatternProviderLogic extends MeteoritePatternProviderLogic {

    private static final ResourceLocation DISK_CONTENTS_ID = ResourceLocation.parse("ae2_pattern_disk:disk_contents");

    /** Contract copy of the addon's disk content format. */
    public record DiskContents(String type, int capacity, List<ItemStack> patterns) {
        public static final Codec<DiskContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("type").forGetter(c -> Optional.ofNullable(c.type)),
                Codec.INT.fieldOf("capacity").forGetter(DiskContents::capacity),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("patterns").forGetter(DiskContents::patterns))
                .apply(instance, (type, capacity, patterns) -> new DiskContents(type.orElse(null), capacity, patterns)));
    }

    private final List<ItemStack> diskPatterns = new ArrayList<>();
    private final MeteoritePatternProviderBlockEntity host;

    public DisksMeteoritePatternProviderLogic(
            IManagedGridNode mainNode,
            MeteoritePatternProviderBlockEntity host,
            int patternInventorySize) {
        super(mainNode, host, patternInventorySize);
        this.host = host;
    }

    private static boolean isAddonLoaded() {
        return ModList.get().isLoaded("ae2_pattern_disk");
    }

    /**
     * Re-scans the parent pattern inventory for pattern disks and refreshes expanded disk recipes.
     * Vanilla patterns are untouched.
     */
    public void refreshPatternsFromDisks() {
        diskPatterns.clear();
        if (!isAddonLoaded()) {
            return;
        }
        InternalInventory patternInv = getPatternInv();
        if (patternInv == null) {
            return;
        }
        for (int i = 0; i < patternInv.size(); i++) {
            DiskContents contents = readDiskContents(patternInv.getStackInSlot(i));
            if (contents != null) {
                diskPatterns.addAll(contents.patterns());
            }
        }
        updatePatterns();
    }

    /**
     * Reads the addon disk's content via the component's own codec (encode the value object to NBT, then
     * decode with our {@link DiskContents#CODEC} format copy). Returns {@code null} if not a pattern disk.
     */
    private DiskContents readDiskContents(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isAddonLoaded()) {
            return null;
        }
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(DISK_CONTENTS_ID);
        if (type == null) {
            return null;
        }
        Object value = stack.getComponents().get(type);
        if (value == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            var valueCodec = (Codec<Object>) ((DataComponentType<?>) type).codec();
            var encoded = valueCodec.encodeStart(NbtOps.INSTANCE, value).result();
            if (encoded.isEmpty()) {
                return null;
            }
            return DiskContents.CODEC.decode(NbtOps.INSTANCE, encoded.get()).result()
                    .map(pair -> pair.getFirst()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Merged patterns: parent-decoded vanilla patterns + expanded disk recipes. */
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        List<IPatternDetails> result = new ArrayList<>(super.getAvailablePatterns());
        if (!isAddonLoaded()) {
            return result;
        }
        // Lazily re-scan the pattern inventory for the latest disk recipes so the terminal always sees
        // patterns added at runtime (the inventory-onChange path does not re-trigger our refresh).
        reScanCurrentDisks();
        Level level = host == null ? null : host.getBlockEntity().getLevel();
        if (level == null) {
            return result;
        }
        for (ItemStack pattern : diskPatterns) {
            IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, level);
            if (details != null) {
                result.add(details);
            }
        }
        return result;
    }

    /**
     * Re-reads pattern disks currently in the parent pattern inventory into {@link #diskPatterns}.
     * Does not touch the parent's pattern list; we only refresh our expanded disk recipes cache.
     */
    private void reScanCurrentDisks() {
        diskPatterns.clear();
        InternalInventory patternInv = getPatternInv();
        if (patternInv == null) {
            return;
        }
        for (int i = 0; i < patternInv.size(); i++) {
            DiskContents contents = readDiskContents(patternInv.getStackInSlot(i));
            if (contents != null) {
                diskPatterns.addAll(contents.patterns());
            }
        }
    }
}
