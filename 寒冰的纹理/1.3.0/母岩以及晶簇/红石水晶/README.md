# 红石水晶母岩与晶簇

材料 ID：`redstone`。目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `redstone_mother_rock.png` | `block/redstone_mother_rock.png` | 母岩。 |
| `redstone_small_crystal_bud.png` | `block/redstone_small_crystal_bud.png` | 小型晶芽。 |
| `redstone_medium_crystal_bud.png` | `block/redstone_medium_crystal_bud.png` | 中型晶芽。 |
| `redstone_large_crystal_bud.png` | `block/redstone_large_crystal_bud.png` | 大型晶芽。 |
| `redstone_crystal_cluster.png` | `block/redstone_crystal_cluster.png` | 晶簇。 |

## 实施结论

- 完整注册五个方块和方块物品：`redstone_mother_rock`、`redstone_small_crystal_bud`、`redstone_medium_crystal_bud`、`redstone_large_crystal_bud`、`redstone_crystal_cluster`。
- 母岩随机 tick 依次放置和替换四个独立阶段方块；每个阶段使用 AE2 风格的 `FACING`、`WATERLOGGED`、附着面生存检查和水 tick，不使用 `AGE`。
- 最终晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_REDSTONE_CRYSTAL`（`purified_redstone_crystal`）。五个方块完整生成模型、战利品、镐挖掘标签、语言和创造栏；不要为红石信号或能量功能凭纹理新增 BlockEntity，当前不做任何配方。
