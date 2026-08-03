# 下界石英母岩与晶簇

材料 ID：`nether_quartz`。预期进入 `textures/block/`，目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `nether_quartz_mother_rock.png` | `block/nether_quartz_mother_rock.png` | 母岩。 |
| `nether_quartz_small_crystal_bud.png` | `block/nether_quartz_small_crystal_bud.png` | 小型晶芽。 |
| `nether_quartz_medium_crystal_bud.png` | `block/nether_quartz_medium_crystal_bud.png` | 中型晶芽。 |
| `nether_quartz_large_crystal_bud.png` | `block/nether_quartz_large_crystal_bud.png` | 大型晶芽。 |
| `nether_quartz_crystal_cluster.png` | `block/nether_quartz_crystal_cluster.png` | 晶簇。 |

## 实施结论

- 完整注册五个方块和方块物品：`nether_quartz_mother_rock`、`nether_quartz_small_crystal_bud`、`nether_quartz_medium_crystal_bud`、`nether_quartz_large_crystal_bud`、`nether_quartz_crystal_cluster`。联动与否不影响这些注册。
- 母岩随机 tick 按 AE2 方式依次将小、中、大晶芽和晶簇替换到相邻空气或水源位置；每个阶段方块独立持有 `FACING` 与 `WATERLOGGED`，不使用 `AGE`。
- 最终晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_NETHER_QUARTZ_CRYSTAL`（`purified_nether_quartz_crystal`）；全部阶段均生成模型、战利品、镐挖掘标签、语言和创造栏条目。当前不做任何配方。
