# 盈能福鲁伊克斯母岩与晶簇

材料 ID：`energized_fluix`。母岩为六帧竖向动画条，落地资源时需补同名 `.mcmeta`；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `energized_fluix_mother_rock.png` | `block/energized_fluix_mother_rock.png` | 动画母岩。 |
| `energized_fluix_small_crystal_bud.png` | `block/energized_fluix_small_crystal_bud.png` | 小型晶芽。 |
| `energized_fluix_medium_crystal_bud.png` | `block/energized_fluix_medium_crystal_bud.png` | 中型晶芽。 |
| `energized_fluix_large_crystal_bud.png` | `block/energized_fluix_large_crystal_bud.png` | 大型晶芽。 |
| `energized_fluix_crystal_cluster.png` | `block/energized_fluix_crystal_cluster.png` | 晶簇。 |

## 实施结论

- 完整注册五个方块和方块物品：`energized_fluix_mother_rock`、`energized_fluix_small_crystal_bud`、`energized_fluix_medium_crystal_bud`、`energized_fluix_large_crystal_bud`、`energized_fluix_crystal_cluster`。
- 母岩复制到资源目录后保留六帧动画并新增 `energized_fluix_mother_rock.png.mcmeta`。母岩动画与四个独立晶簇阶段方块分别处理，不能把动画帧当成长状态。
- 最终晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_ENERGIZED_FLUIX_CRYSTAL`（`purified_energized_fluix_crystal`）。五个方块均补齐模型、战利品、镐挖掘标签、语言和创造栏；当前不做任何配方。
