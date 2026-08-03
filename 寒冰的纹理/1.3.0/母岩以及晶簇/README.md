# 母岩以及晶簇

每个材料目录均保留原有位置。文件名采用“材料名在前、类别在后”的 AECS 风格：`<material>_mother_rock`、`<material>_small_crystal_bud`、`<material>_medium_crystal_bud`、`<material>_large_crystal_bud`、`<material>_crystal_cluster` 和可选的 `<material>_mature_crystal_cluster`。

| 材料 | 材料 ID | 晶簇阶段 | 目标方块注册数 | 素材状态 |
| --- | --- | ---: | ---: | --- |
| [下界石英](下界石英/README.md) | `nether_quartz` | 4 | 5 | 母岩和四阶段晶簇。 |
| [充能赛特斯](充能赛特斯/README.md) | `energized_certus_quartz` | 4 | 5 | NeoECO 联动，动画母岩和四阶段晶簇。 |
| [恩特罗](恩特罗/README.md) | `entro` | ExtendedAE 4 阶段 | 1 | AECS 只注册母岩，晶芽与晶簇复用 ExtendedAE。 |
| [末影](末影/README.md) | `ender_quartz` | 5 | 6 | 含成熟晶簇。 |
| [盈能福鲁伊克斯](盈能福鲁伊克斯/README.md) | `energized_fluix` | 4 | 5 | 动画母岩和四阶段晶簇。 |
| [福鲁伊克斯](福鲁伊克斯/README.md) | `fluix` | 4 | 5 | 母岩和四阶段晶簇。 |
| [红石水晶](红石水晶/README.md) | `redstone` | 4 | 5 | 母岩和四阶段晶簇。 |
| [谐振](谐振/README.md) | `resonating` | 5 | 6 | 含成熟晶簇。 |
| [量子](量子/README.md) | `quantum` | 5 | 6 | 动画母岩和成熟晶簇。 |
| [链接](链接/README.md) | `link` | 5 | 6 | 含成熟晶簇。 |
| [陨石](陨石/README.md) | `meteor` | 5 | 6 | 含成熟晶簇。 |

## 项目内推断与共用实现

- 当前项目只有 `CrystalSeedItem` 的水中物品生长，未注册任何母岩或晶簇方块。它不能承担世界方块的阶段替换、朝向、含水和战利品逻辑，因此本组纹理必须新增方块实现，不能复用种子 Item。
- 首次接入时建立无 BlockEntity 的通用 `CrystalMotherRockBlock` 与 `CrystalClusterBlock`：母岩在相邻空气或水源位置放置小型晶芽，并用方块替换推进到后续阶段；每个阶段方块持有 `FACING`、`WATERLOGGED`。不得扫描或加载邻区块。
- 除恩特罗外，每个有本地晶簇纹理的材料都在 `AECSBlockIds` 与 `AECSBlocks` 完整注册母岩、小型晶芽、中型晶芽、大型晶芽、晶簇和可选成熟晶簇。每个本地阶段都是独立 Block 和 BlockItem，不能用 `AGE` 合并，也不能因其来源于联动内容而省略注册。
- 恩特罗是外部成长链例外：AECS 仅无条件注册 `entro_mother_rock`。生长时按顺序使用 `extendedae:entro_cluster_small`、`extendedae:entro_cluster_medium`、`extendedae:entro_cluster_large`、`extendedae:entro_cluster`；ExtendedAE 未加载时母岩保持惰性，类初始化阶段不得直接引用外部注册对象。
- 参照当前 AE2 1.21.1 的 `BuddingCertusQuartzBlock` 和 `CertusQuartzClusterBlock`：生长目标仅允许空气或水源；每阶段检查附着面的坚固性、正确安排水 tick，并在支撑面被破坏时不产生非玩家掉落。
- 在 `AECSBlockStateProvider` 增加专用 `genCrystalFamily`。母岩用 cube_all；每个晶芽和晶簇方块分别生成六个附着面模型、blockstate 和方块物品模型。不得调用机器专用的 `genSixFaceLike`。
- 在 `AECSBlockLootTableProvider` 定义全部阶段的战利品，在 `AECSBlockTagProvider` 加入 `BlockTags.MINEABLE_WITH_PICKAXE`。所有阶段都要有语言条目和创造栏策略；自然生成、精准采集和普通掉落均要显式定义。
- 当前不编写合成、加工或联动配方。后续只有配方会按前置模组存在性选择生成；AECS 自有方块的注册、模型、战利品、标签和语言始终完整加载。
- 本目录只覆盖当前确认的 1.3.0 母岩与晶簇素材，不根据 `AECSItems` 的全部纯净水晶、种子或已安装前置模组反推额外族系。
- 自然生成不是纹理接入的默认步骤。仅在确定玩法需要时，按 `AECSConfiguredFeatures`、`AECSPlacedFeatures`、`AECSBiomeModifiers` 三层新增；禁止在区块加载事件中生成。

## 原版与 AE2 边界

- 晶簇行为应遵循原版 `BuddingAmethystBlock` 与 `AmethystClusterBlock`，并以 AE2 的 `BuddingCertusQuartzBlock`、`CertusQuartzClusterBlock` 为当前版本直接参考：母岩随机 tick 推进阶段独立方块，晶芽和晶簇具有附着方向、含水与生存面检查。由于材料、阶段数和掉落不同，使用 AECS 自己的通用实现，不直接绑定原版或 AE2 的具体方块实例。
- 晶簇是普通世界方块，不是 AE2 网络设备：不继承 `AEBaseEntityBlock`，不创建 BlockEntity，不添加 `@ProvideCaps`，也不接入 `AENetworkedSelfPoweredBlockEntity`、菜单或 AE 网格节点。
- 含水状态必须使用原版 `WATERLOGGED` 语义并安排流体 tick；附着方向必须支持六个面。四阶段和五阶段差异由独立注册的阶段方块数量决定，不使用 `AGE` 属性。
- 最终接入时，复制本目录 PNG 到 `src/main/resources/assets/ae2cs/textures/block/`。随后由 `AECSBlockStateProvider` 的专用生成器生成模型；`AECSBlockModelProvider` 当前为空，不能期待它自动补模型。
- `DataGenerators` 已注册 blockstate、loot、block tag、item tag 和 worldgen provider。实现提交前必须运行数据生成并审阅 `src/generated/resources` 中仅与晶簇相关的输出。
