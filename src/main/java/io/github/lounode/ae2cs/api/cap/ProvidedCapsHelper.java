package io.github.lounode.ae2cs.api.cap;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProvidedCapsHelper {

    public static Set<Class<?>> getProvidedClasses(Class<?> cls) {
        Set<Class<?>> out = new LinkedHashSet<>();
        collect(cls, out, new HashSet<>());
        return out;
    }

    private static void collect(Class<?> type, Set<Class<?>> out, Set<Class<?>> visited) {
        if (type == null || type == Object.class || !visited.add(type)) {
            return;
        }

        // 只取本类型声明的
        for (ProvideCaps a : type.getDeclaredAnnotationsByType(ProvideCaps.class)) {
            out.add(a.value());
        }

        // 父类链
        collect(type.getSuperclass(), out, visited);

        // 接口链（含接口的父接口）
        for (Class<?> itf : type.getInterfaces()) {
            collect(itf, out, visited);
        }
    }
}
