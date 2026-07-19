package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.me.part.MirrorPatternProviderPart;

import appeng.items.parts.PartItem;

public class MirrorPatternProviderPartItem extends PartItem<MirrorPatternProviderPart> {

    public MirrorPatternProviderPartItem(Properties properties) {
        super(properties, MirrorPatternProviderPart.class, MirrorPatternProviderPart::new);
    }
}
