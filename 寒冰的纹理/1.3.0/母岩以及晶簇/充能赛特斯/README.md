# 充能赛特斯母岩与晶簇

材料 ID：`energized_certus_quartz`。依据 NeoECO 的 `energized_crystal` 与 AECS 现有 `energized_certus_quartz_seed` 命名确定；母岩为六帧竖向动画条，落地资源时需补同名 `.mcmeta`，目录位置不变。

| 当前文件名 | 预期资源 ID | 阶段 |
| --- | --- | --- |
| `energized_certus_quartz_mother_rock.png` | `block/energized_certus_quartz_mother_rock.png` | 动画母岩。 |
| `energized_certus_quartz_small_crystal_bud.png` | `block/energized_certus_quartz_small_crystal_bud.png` | 小型晶芽。 |
| `energized_certus_quartz_medium_crystal_bud.png` | `block/energized_certus_quartz_medium_crystal_bud.png` | 中型晶芽。 |
| `energized_certus_quartz_large_crystal_bud.png` | `block/energized_certus_quartz_large_crystal_bud.png` | 大型晶芽。 |
| `energized_certus_quartz_crystal_cluster.png` | `block/energized_certus_quartz_crystal_cluster.png` | 晶簇。 |

## 实施结论

- 完整注册五个方块和方块物品：`energized_certus_quartz_mother_rock`、`energized_certus_quartz_small_crystal_bud`、`energized_certus_quartz_medium_crystal_bud`、`energized_certus_quartz_large_crystal_bud`、`energized_certus_quartz_crystal_cluster`。
- 母岩复制到资源目录后保留六帧动画并新增 `energized_certus_quartz_mother_rock.png.mcmeta`；母岩随机 tick 依次放置和替换四个独立阶段方块，不使用 `AGE`，不要将动画条拆成六面机器材质。
- 最终晶簇的普通挖掘产物应接入既有 `AECSItems.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL`（`purified_energized_certus_quartz_crystal`）。五个方块均要有模型、战利品、镐挖掘标签、语言和创造栏条目；当前不做任何配方。
