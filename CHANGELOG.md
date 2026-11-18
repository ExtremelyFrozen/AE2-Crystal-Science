# ChangeLog

## Version [v](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/compare/v1.2.1-1.21.1...1.3.0+alpha-1.21.1)
### Added

- feat: add crystal infuser machine by @rulanup in [#63](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/63)
- GUI更新、JEI/EMI同步适配且添加流体槽位输入输出支持 by @y271727uy in [#64](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/64)
- feat: add pulse centrifuge machine by @rulanup in [#65](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/65)
- 补充 1.3.0 水晶族系与纹理注册实现 by @QiuYe-QAQ in [#61](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/61)
- 修正emi/jei 并增加一个emi布尔值 by @y271727uy in [#70](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/70)
- 熵变反应器新增选择模式与zh_hk by @y271727uy in [#74](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/74)
- 修复水晶生长、解决迁移时带来的bug by @y271727uy in [#79](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/79)
- 更新末影发信器UI by @Yang120231 in [#87](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/87)
- fix: 修复镜像供应器断联、调整绑定交互并兼容 AdvancedAE by @QiuYe-QAQ in [#92](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/92)
- 更新很多 by @y271727uy in [#98](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/98)

### Fixed

- fix: 防止自动输入输出回流刷物品 by @QiuYe-QAQ in [#62](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/62)
- feat: add pulse centrifuge machine by @rulanup in [#65](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/65)
- fix: 防止自动输入输出跨 tick 回流刷物品 by @QiuYe-QAQ in [#66](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/66)
- fix: 修复机器槽位暴露和自动输入输出 by @QiuYe-QAQ in [#67](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/67)
- fix: 修复末影广播残留网络身份 by @QiuYe-QAQ in [#69](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/69)
- 修正emi/jei 并增加一个emi布尔值 by @y271727uy in [#70](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/70)
- fix: 修复镜像重连与谐振残留状态 by @QiuYe-QAQ in [#71](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/71)
- fix: 修复末影设备重进存档后断联 by @QiuYe-QAQ in [#72](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/72)
- 熵变反应器新增选择模式与zh_hk by @y271727uy in [#74](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/74)
- fix: 修复谐振终端通用终端合成、快捷键翻译与锻造台消耗 by @lhy512103 in [#78](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/78)
- 修复水晶生长、解决迁移时带来的bug by @y271727uy in [#79](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/79)
- fix: 修复末影发信器四个侧面无法接入 AE2 网络 by @Yang120231 in [#88](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/88)
- fix: 修复末影发信器新设备自动连接并兼容旧范围存档 by @Yang120231 in [#93](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/93)
- fix: 修复镜像供应器断联、调整绑定交互并兼容 AdvancedAE by @QiuYe-QAQ in [#92](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/92)

### Changed

- fix: 修复谐振终端通用终端合成、快捷键翻译与锻造台消耗 by @lhy512103 in [#78](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/78)
- 更新很多 by @y271727uy in [#98](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/98)

 
## Version [v1.2.1](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/compare/v1.2.0-1.21.1...v1.2.1-1.21.1)

### Added

- 持有已设置目标的镜像绑定器时，高亮所有绑定到相同目标的镜像样板供应器。普通方块供应器会高亮全部表面，ME 部件供应器会高亮安装面。 by @QiuYe-123 in [#42](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/42)
- 镜像绑定器支持按组解绑：Shift 右键已绑定的镜像样板供应器，可解除当前维度所有已加载同组供应器的绑定。 by @QiuYe-123 in [#43](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/43)
- 新增无线谐振合成编码终端的 AE2WTLib 集成，以及安装到无线通用终端、与其他无线终端合并的配方。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 新增晶能聚合器和闪电模拟室的过载水晶种子配方，并补充种子、水晶处理、处理器、充能、切石及跨模组机器配方。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54) and [#56](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/56)
- 新增 AECS 处理机器的 JEI/EMI 配方跳转和材料转移支持。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 新增镜像样板供应器和谐振合成编码终端指南，并修正陨石样板供应器指南。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 新增高纯过载水晶、高纯盈能水晶和高纯盈能福鲁伊克斯水晶的 2x2 方块压缩配方。 by @lhy512103 in [#58](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/58)

### Fixed

- 修复无线谐振合成编码终端无法通过 Forge Energy 充电的问题。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 修复末影发信器频段菜单的滑块贴图错误。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 修复谐振合成编码终端与 Data Energistics 的空白样板代理交互及网络库存显示问题。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 修复谐振样板编码终端菜单的重复注册问题。 by @lhy512103 in [#58](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/58)

### Changed

- 将谐振处理样板容量扩展至 144 个输入槽和 36 个输出槽，并将谐振样板供应器的可标记输入数量从 81 扩展至 144。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 扩展陨石超频卡对兼容 AECS 机器的支持；自装配式样板供应器最多可安装四张陨石超频卡，每张增加 128 个并行合成请求。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 调整末影发信器、水晶格块、充能过载水晶块和电路蚀刻器配方；数据处理器与过载处理器的电路蚀刻配方改用正确的方块输入。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54) and [#58](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/58)
- 数据水晶粉碎配方统一使用 `c:gems/data_crystal` 标签，移除晶能粉碎机与 Mekanism 粉碎机中重复的高纯数据水晶制粉配方。 by @lhy512103 in [#56](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/56) and [#58](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/58)
- 移除旧有的盈能水晶、盈能福鲁伊克斯水晶和数据水晶种子工作台配方。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54)
- 更新高纯过载水晶与熵变反应室模式图标贴图，优化谐振合成编码终端模式按钮对齐，并调整末影发信器界面。 by @lhy512103 in [#54](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/54) and [#58](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/58)
- 优化全部简体中文指南的措辞与语气，保留原有功能说明。 by @QianChang-official in [#45](https://github.com/ExtremelyFrozen/AE2-Crystal-Science/pull/45)