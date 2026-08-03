# 福鲁伊克斯母岩与晶簇

材料 ID：`fluix`。目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `fluix_mother_rock.png` | `block/fluix_mother_rock.png` | 母岩。 |
| `fluix_small_crystal_bud.png` | `block/fluix_small_crystal_bud.png` | 小型晶芽。 |
| `fluix_medium_crystal_bud.png` | `block/fluix_medium_crystal_bud.png` | 中型晶芽。 |
| `fluix_large_crystal_bud.png` | `block/fluix_large_crystal_bud.png` | 大型晶芽。 |
| `fluix_crystal_cluster.png` | `block/fluix_crystal_cluster.png` | 晶簇。 |

## 实施结论

- 完整注册五个方块和方块物品：`fluix_mother_rock`、`fluix_small_crystal_bud`、`fluix_medium_crystal_bud`、`fluix_large_crystal_bud`、`fluix_crystal_cluster`。
- 母岩随机 tick 依次放置和替换四个独立阶段方块；每个阶段使用 AE2 风格的 `FACING`、`WATERLOGGED`、附着面生存检查和水 tick，不使用 `AGE`。
- 最终晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_FLUIX_CRYSTAL`（`purified_fluix_crystal`）。五个方块完整生成模型、战利品、镐挖掘标签、语言和创造栏；当前不做任何配方。
