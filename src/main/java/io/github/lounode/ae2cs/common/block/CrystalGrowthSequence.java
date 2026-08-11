package io.github.lounode.ae2cs.common.block;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines the ordered block IDs used by one mother rock when advancing crystal growth.
 */
public final class CrystalGrowthSequence {

    private final List<ResourceLocation> stageIds;

    public CrystalGrowthSequence(List<ResourceLocation> stageIds) {
        Objects.requireNonNull(stageIds, "stageIds");
        if (stageIds.isEmpty()) {
            throw new IllegalArgumentException("A crystal growth sequence must contain at least one stage");
        }
        if (stageIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Crystal growth stages cannot contain null IDs");
        }
        if (new HashSet<>(stageIds).size() != stageIds.size()) {
            throw new IllegalArgumentException("Crystal growth stages must be unique: " + stageIds);
        }
        this.stageIds = List.copyOf(stageIds);
    }

    public ResourceLocation firstStageId() {
        return stageIds.getFirst();
    }

    public Optional<ResourceLocation> nextStageId(ResourceLocation currentStageId) {
        int currentIndex = stageIds.indexOf(currentStageId);
        if (currentIndex < 0 || currentIndex == stageIds.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(stageIds.get(currentIndex + 1));
    }

    public List<ResourceLocation> stageIds() {
        return stageIds;
    }
}
