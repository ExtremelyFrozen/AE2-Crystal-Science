package io.github.lounode.ae2cs.api.util;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PatternHelper
{

    /**
     * 给定一个可能包含 null 的堆叠数组，其中可能包含多个相同类型，
     * 生成一个新的数组：不包含 null 元素，并且每种输入类型只出现一次，同时保持原有的顺序不变。
     * <p>
     * 这段代码复制自AE2原版
     */
    public static List<GenericStack> condenseStacks(List<GenericStack> sparseInput)
    {
        var map = new LinkedHashMap<AEKey, Long>();

        for (var input : sparseInput)
        {
            if (input != null)
            {
                map.merge(input.what(), input.amount(), Long::sum);
            }
        }

        if (map.isEmpty())
        {
            throw new IllegalStateException("No pattern here!");
        }

        List<GenericStack> out = new ArrayList<>(map.size());
        for (var entry : map.entrySet())
        {
            out.add(new GenericStack(entry.getKey(), entry.getValue()));
        }
        return out;
    }
}
