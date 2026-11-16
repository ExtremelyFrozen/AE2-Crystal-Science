package io.github.lounode.ae2cs.common.machine;

public final class ResonatingFactoryParallelism {

    private static final int[] BY_OVERLOAD_CARDS = { 8, 16, 64, 128, 256 };

    private ResonatingFactoryParallelism() {}

    public static int forCards(int cards) {
        return BY_OVERLOAD_CARDS[cards];
    }
}
