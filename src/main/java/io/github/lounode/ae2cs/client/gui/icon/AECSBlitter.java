package io.github.lounode.ae2cs.client.gui.icon;

import io.github.lounode.ae2cs.AE2CrystalScience;

import appeng.client.gui.style.Blitter;

public final class AECSBlitter {

    private AECSBlitter() {}

    public static final Blitter energyProgress = Blitter.texture(AE2CrystalScience.makeId("textures/gui/circuit_etcher_menu.png"))
            .src(176, 34, 6, 18);

    public static final Blitter circuitEtcherProgress = Blitter.texture(AE2CrystalScience.makeId("textures/gui/circuit_etcher_menu.png"))
            .src(199, 0, 22, 33);

    public static final Blitter crystalAggregatorProgress = Blitter.texture(AE2CrystalScience.makeId("textures/gui/crystal_aggregator_menu.png"))
            .src(199, 0, 22, 33);

    public static final Blitter crystalPulverizerProgress = Blitter.texture(AE2CrystalScience.makeId("textures/gui/crystal_pulverizer_menu.png"))
            .src(176, 16, 22, 16);
}
