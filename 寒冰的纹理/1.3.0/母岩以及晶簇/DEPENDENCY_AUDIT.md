# 前置模组内容审计

本审计依据 `dependencies.gradle`、`gradle/forge.versions.toml`、AECS 当前注册内容与兼容 RecipeProvider，范围仅为已确认的 1.3.0 纹理。这里只判断方块注册边界和素材用途，不实现配方；`充能共振发电机`不在本次审计范围。

## 结论

- 当前确认范围内不存在缺失 PNG。目录中已有的母岩和晶簇纹理可以覆盖对应 AECS 自有方块；不得再根据前置模组或纯净水晶/种子列表推导额外素材缺口。
- 恩特罗仅由 AECS 无条件注册 `entro_mother_rock`。四阶段成长链复用 ExtendedAE 的 `entro_cluster_small`、`entro_cluster_medium`、`entro_cluster_large` 和 `entro_cluster`，不制作或注册 AECS 重复方块。
- `充能赛特斯`实际对应 NeoECO 的 energized certus，纹理 ID 已统一为 `energized_certus_quartz_*`，避免与 AE2 原生 charged certus 概念混淆。
- 除恩特罗外，当前目录已有阶段纹理对应的 AECS 方块、方块物品、模型、战利品、标签与语言均无条件注册。只有后续合成、加工和前置物品互转配方按前置模组加载状态可选生成；当前不编写配方。

## 逐依赖判断

| 前置模组 | AECS 对应内容 | 1.3.0 结论 |
| --- | --- | --- |
| Applied Energistics 2 | 下界石英、福鲁伊克斯及通用晶簇行为 | 现有素材完整；参考 AE2 的母岩随机生长、六向附着和含水行为，不覆盖 AE2 已有注册。 |
| ExtendedAE | 恩特罗 | AECS 只注册母岩；小、中、大晶芽与最终晶簇使用 ExtendedAE 已有方块。 |
| AdvancedAE | 量子 | 现有母岩、四阶段晶簇和成熟晶簇素材完整。 |
| Applied Flux | 红石水晶 | 现有母岩与四阶段晶簇素材完整。 |
| Neo ECO AE Extension | 充能赛特斯、盈能福鲁伊克斯 | 现有素材完整；充能赛特斯使用 `energized_certus_quartz_*` ID。 |
| 现有 AECS 内容 | 末影、谐振、链接、陨石 | 现有母岩、四阶段晶簇和成熟晶簇素材完整。 |
| 其他 `dependencies.gradle` 前置 | 无本轮确认的新族系 | 不据依赖存在推断或补注册内容。 |

## 恩特罗兼容边界

| 阶段 | 外部方块 ID |
| --- | --- |
| 小型晶芽 | `extendedae:entro_cluster_small` |
| 中型晶芽 | `extendedae:entro_cluster_medium` |
| 大型晶芽 | `extendedae:entro_cluster_large` |
| 最终晶簇 | `extendedae:entro_cluster` |

`entro_mother_rock` 的生长目标必须通过兼容层或运行时注册表解析，不能在公共类静态初始化时直接引用 ExtendedAE 的注册对象。ExtendedAE 未加载时，该母岩保持惰性；这不应阻断 AECS 的注册或启动。
