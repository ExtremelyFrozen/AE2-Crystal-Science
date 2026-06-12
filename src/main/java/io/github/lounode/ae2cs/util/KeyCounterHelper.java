package io.github.lounode.ae2cs.util;

import appeng.api.stacks.KeyCounter;

public class KeyCounterHelper {

    public static KeyCounter deepCopy(KeyCounter src) {
        KeyCounter copy = new KeyCounter();
        copy.addAll(src);
        return copy;
    }
}
