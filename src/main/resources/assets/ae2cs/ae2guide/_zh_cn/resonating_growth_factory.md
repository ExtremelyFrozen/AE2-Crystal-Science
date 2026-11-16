---
navigation:
  parent: crystal_growth_chamber.md
  title: 谐振晶体催生工厂
  icon: ae2cs:resonating_growth_factory
  position: 10
item_ids:
  - ae2cs:resonating_growth_factory
---

# 谐振晶体催生工厂

<Row gap="16">
  <BlockImage id="ae2cs:resonating_growth_factory" scale="8" />
</Row>

**谐振晶体催生工厂**是[晶体催生仓](crystal_growth_chamber.md)的谐振版本。

## 母岩催生

- 第 2、4、6 行放母岩，产物进入同一列正上方的第 1、3、5 行。
- 左侧按钮在小晶芽、中晶芽、大晶芽、晶簇、粉和水晶之间切换，默认产出晶簇。粉和水晶直接产出对应物品，缺少对应物品的母岩会跳过该目标。
- 母岩按催生器的映射和概率衰退。衰退产物留在母岩栏，终态方块可手动取出。部分衰退时优先合并到同类母岩栏，其次使用空栏。
- 安装 0、1、2 张谐振堆叠卡时，每个输入槽可放 1、32、64 块母岩。

| 陨石超频卡 | 每轮母岩上限 | 每块母岩产量 | 周期 |
| --- | --- | --- | --- |
| 0 | 8 | 1 | 4 秒 |
| 1 | 16 | 2 | 3 秒 |
| 2 | 64 | 3 | 2 秒 |
| 3 | 128 | 4 | 1 秒 |
| 4 | 256 | 6 | 4 tick |

每块参与催生的母岩消耗 100 AE。

## 界面与自动化

左侧“**?**”按钮打开本机指南，相邻按钮打开[面配置](side_config.md)。可在面配置中设置各面的输入、输出权限及自动输入、自动输出。
