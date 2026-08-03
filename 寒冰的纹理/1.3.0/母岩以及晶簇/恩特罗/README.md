# 恩特罗母岩

材料 ID：`entro`，对应 ExtendedAE。AECS 只提供并注册母岩；小型、中型、大型晶芽与最终晶簇直接使用 ExtendedAE 已有方块，不为它们制作纹理或重复注册。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `entro_mother_rock.png` | `block/entro_mother_rock.png` | 母岩。 |

## 外部成长链

| 阶段 | 使用的 ExtendedAE 方块 ID |
| --- | --- |
| 小型晶芽 | `extendedae:entro_cluster_small` |
| 中型晶芽 | `extendedae:entro_cluster_medium` |
| 大型晶芽 | `extendedae:entro_cluster_large` |
| 最终晶簇 | `extendedae:entro_cluster` |

## 实施结论

- AECS 无条件注册 `entro_mother_rock` 方块和方块物品，并只为该母岩生成模型、战利品、标签、语言与创造栏条目。
- 母岩生长时只使用 ExtendedAE 提供的四阶段方块，AECS 不重复注册对应阶段，也不复制其晶簇纹理、方块模型或战利品。
- 外部方块必须通过兼容层或运行时注册表安全解析，禁止在公共类的静态初始化阶段硬引用 ExtendedAE 注册对象。ExtendedAE 未加载时，恩特罗母岩保持惰性，不尝试生长，也不影响 AECS 启动。
- 当前不做任何配方或世界生成。
