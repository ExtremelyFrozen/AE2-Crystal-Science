package io.github.lounode.ae2cs.api.cap;

import java.lang.annotation.*;

@Repeatable(ProvideCaps.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProvideCaps
{
    Class<?> value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List
    {
        ProvideCaps[] value();
    }
}
