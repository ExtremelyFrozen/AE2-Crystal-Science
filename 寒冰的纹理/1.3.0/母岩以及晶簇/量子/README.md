# 量子母岩与晶簇

材料 ID：`quantum`。母岩为六帧竖向动画条；包含成熟晶簇，相关掉落与成长规则待玩法确认；目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `quantum_mother_rock.png` | `block/quantum_mother_rock.png` | 动画母岩。 |
| `quantum_small_crystal_bud.png` | `block/quantum_small_crystal_bud.png` | 小型晶芽。 |
| `quantum_medium_crystal_bud.png` | `block/quantum_medium_crystal_bud.png` | 中型晶芽。 |
| `quantum_large_crystal_bud.png` | `block/quantum_large_crystal_bud.png` | 大型晶芽。 |
| `quantum_crystal_cluster.png` | `block/quantum_crystal_cluster.png` | 晶簇。 |
| `quantum_mature_crystal_cluster.png` | `block/quantum_mature_crystal_cluster.png` | 成熟晶簇。 |

## 实施结论

- 完整注册六个方块和方块物品：`quantum_mother_rock`、`quantum_small_crystal_bud`、`quantum_medium_crystal_bud`、`quantum_large_crystal_bud`、`quantum_crystal_cluster`、`quantum_mature_crystal_cluster`。
- 母岩复制到资源目录后保留六帧动画并新增 `quantum_mother_rock.png.mcmeta`。动画帧与五个独立晶簇阶段方块不可混用；每个阶段使用 `FACING` 和 `WATERLOGGED`，不使用 `AGE`。
- 成熟晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_QUANTUM_CRYSTAL`（`purified_quantum_crystal`）。六个方块完整生成模型、战利品、镐挖掘标签、语言和创造栏；当前不做任何配方。
