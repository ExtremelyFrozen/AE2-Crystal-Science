package io.github.lounode.ae2cs.common.me.logic;

import org.jspecify.annotations.NullMarked;

/**
 * Native lifecycle callbacks supplied once by an optional integration. Retain only with the owning logic instance;
 * invoke on its logical thread after construction. Each non-null callback reads current state and may mutate it.
 */
@NullMarked
public record OptionalProviderDispatch(Runnable inputChanged, Runnable pushed, Runnable afterWork) {}
