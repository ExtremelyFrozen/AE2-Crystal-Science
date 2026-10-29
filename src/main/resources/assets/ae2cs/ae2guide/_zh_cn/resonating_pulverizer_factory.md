---
navigation:
  parent: crystal_pulverizer.md
  title: 谐振晶能粉碎工厂
  icon: ae2cs:resonating_pulverizer_factory
  position: 10
item_ids:
  - ae2cs:resonating_pulverizer_factory
---

# 谐振晶能粉碎工厂

<Row gap="16">
  <BlockImage id="ae2cs:resonating_pulverizer_factory" scale="8" />
</Row>

**谐振晶能粉碎工厂**是[晶能粉碎机](crystal_pulverizer.md)的批量加工版本，沿用普通粉碎机的配方，需要供能运行。

## 库存与加工

- 左右各有一组 **3×3 槽位**，共 **9 个输入槽、9 个输出槽**。
- 两侧分别为输入、输出流体槽，容量各 **16,000 mB**，供带流体的配方使用。
- 中央箭头显示当前批次进度，点击可查看粉碎配方。
- 不安装陨石卡时每批处理 **1 份配方**；陨石卡按每张 16 份计算批量上限，整批最多 **32 份**。
- 批量加工按实际份数消耗材料、流体和能量，不减免单份配方成本。

谐振堆叠卡仅扩大库存容量，不直接提高加工并行上限。

---

## 升级卡与存储容量

- 四个专用槽支持最多 **4 张 <ItemLink id="ae2cs:overload_card" />**。
- 两个专用槽支持最多 **2 张 <ItemLink id="ae2cs:resonating_stacking_card" />**。
- 每格限放一张卡，不支持普通加速卡。
- 悬停升级栏可查看“**可用升级**”列表与整机安装上限。

对原本最多堆叠 64 个的物品，安装 0、1、2 张堆叠卡时，每格容量分别为 **64、256、1024 个**。原本堆叠上限较小的物品按比例扩容，不可堆叠物品仍限一个。若取下堆叠卡会导致任意库存超出新上限，机器会阻止取卡；请先取出多余物品。

## 界面与自动化

左侧“**?**”按钮打开本机指南，相邻按钮打开[面配置](side_config.md)。可在面配置中设置各面的输入、输出权限及自动输入、自动输出。
