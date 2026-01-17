---
navigation:
  parent: index.md
  title: 谐振样板供应器
  icon: ae2cs:resonating_pattern_provider
  position: 150
item_ids:
  - ae2cs:resonating_pattern_provider
  - ae2cs:extended_resonating_pattern_provider
  - ae2cs:resonating_pattern_provider_part
  - ae2cs:extended_resonating_pattern_provider_part
  - ae2cs:resonating_pattern
  - ae2cs:resonating_pattern_converter
  - ae2cs:resonating_pattern_provider_upgrade
  - ae2cs:extended_resonating_pattern_provider_upgrade

---

# 谐振样板供应器

## 供应器

<Row>
  <BlockImage id="resonating_pattern_provider" scale="4" />
  <BlockImage id="extended_resonating_pattern_provider" scale="4" />
  <ItemImage id="resonating_pattern_provider_part" scale="4" />
  <ItemImage id="extended_resonating_pattern_provider_part" scale="4" />
  <ItemImage id="resonating_pattern_provider_upgrade" scale="4" />
  <ItemImage id="extended_resonating_pattern_provider_upgrade" scale="4" />
</Row>

谐振样板供应器是AECS最为强大的机器之一，其需要配合谐振样板使用，每一个材料都可以隔空，甚至是跨纬度地发配到任何指定位置。并且能主动抽取其面对的容器，不必依靠管道送回产物。
你可以使用它将材料同时发配到产线上的不同位置，也能用来轻松完成各种奇奇怪怪的魔法祭坛或者是龙之研究注入装置的自动化
当然，即使不使用谐振样板，它也能当作一个普通的样板供应器使用。

## 谐振样板

<Row>
  <ItemImage id="resonating_pattern" scale="4" />
  <ItemImage id="resonating_pattern_converter" scale="4" />
</Row>

谐振样板承担着为谐振样板供应器指明材料发配位置的重任。
你有两种方式获取谐振样板：
1. 在工作台上将一个<ItemLink id="resonating_crystal_dust" />与一个已编码的处理样板混合
2. 使用<ItemLink id="resonating_pattern_converter" />

无论哪一种方式都会得到一样的结果，不过<ItemLink id="resonating_pattern_converter" />可以一次性转换多个处理样板，且不消耗<ItemLink id="resonating_crystal_dust" />
将谐振样板拿在手上，shift+滚轮切换当前目标材料，右键任意方块即可标记当前材料的目标发配位置和发配面，再次右键取消标记。
当前目标材料的标记位置会被渲染为绿色，其他材料的标记位置会被渲染为蓝色。
没有标记的材料将会按照原版样板供应器的发配方式发送。

需要注意的是，为了防止切换材料时误触，谐振样板不允许使用shift右键清空，而是可以在工作台上合成为一个空白样板