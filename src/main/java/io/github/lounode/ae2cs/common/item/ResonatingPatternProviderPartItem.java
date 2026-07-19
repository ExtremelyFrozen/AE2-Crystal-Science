package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.part.ResonatingPatternProviderPart;

import appeng.items.parts.PartItem;

public class ResonatingPatternProviderPartItem extends PartItem<ResonatingPatternProviderPart> {

    public ResonatingPatternProviderPartItem(Properties properties) {
        super(properties, ResonatingPatternProviderPart.class, ResonatingPatternProviderPart::new);
    }
}
