# PR #1: 1.20.1 Forge — 修复集成接口无法被外部管道连接

**分支**: `fix/integrated-interface-external-pipe-1.20` → `upstream/1201-test`

## 问题描述

集成接口（方块和线缆部件两种形态）配置标记物品后，ME 网络能将物品送入存储槽位，但 Mekanism、Pipez 等外部管道无法连接提取，只能手动取出。管道连接时有时无，更新附近方块后可能断开。

## 原因分析

外部管道通过 `ForgeCapabilities.ITEM_HANDLER` / `FLUID_HANDLER` 连接设备，但集成接口只暴露了 AE2 内部的 `GENERIC_INTERNAL_INV`。AE2 在 `InitCapabilities.registerGenericInvWrapper()` 中注册了从 `IItemHandler` / `IFluidHandler` 到 `GENERIC_INTERNAL_INV` 的桥接器，但该桥接器附着在 `BlockEntity` 层级。对于线缆部件形式，能力查询链路为 `CableBusBlockEntity.getCapability()` → `part.getCapability()`，部件直接返回空结果，桥接器未被触发。

## 修复内容

- `IntegratedInterfaceBlockEntity.getCapability()`: 新增 `ForgeCapabilities.ITEM_HANDLER` 和 `FLUID_HANDLER` 的处理，直接返回 `GenericStackItemStorage` / `GenericStackFluidStorage` 包装器
- `IntegratedInterfacePart.getCapability()`: 同上，使用与 AE2 `InterfaceLogic.getCapability()` 一致的模式创建包装器

## 已知问题：构建环境差异导致的渲染异常

在本地构建测试中，集成接口方块和线缆部件出现纹理颜色丢失（变为灰色）。经隔离测试和对比分析，确认：

1. 该问题与本次 PR 的代码修改**无关**——即使回退到未修改的基础提交（`dd10530`）构建，纹理问题依然存在
2. 原始发布版 JAR（v1.1.4.514）纹理正常，与从当前源码构建的 JAR 对比，所有资源文件（纹理、模型、blockstates）MD5 完全一致，但 class 文件大小不同，判断为**构建环境差异**（JDK / MDK / ForgeGradle 版本等）导致
3. 使用 1.21.1 纹理移植资源包后，1.20.1 中纹理可正常渲染为 1.21.1 的纹理效果
4. 1.21.1 NeoForge 版本无此问题

**结论**：此 PR 仅提交源代码修改，纹理问题不影响功能正确性，在原作者构建环境中发布应不受影响。

---

# PR #2: 1.21.1 NeoForge — 修复集成接口无法被外部管道连接

**分支**: `fix/integrated-interface-external-pipe-1.21` → `upstream/1.21.1`

## 问题描述

与 1.20.1 版本相同：集成接口的方块和线缆部件形式均无法被外部管道连接。

## 原因分析

1.21.1 NeoForge 使用事件驱动的能力注册系统（`RegisterCapabilitiesEvent` / `RegisterPartCapabilitiesEvent`）。集成接口只注册了 `GENERIC_INTERNAL_INV` 和 `ME_STORAGE`。AE2 在 `InitCapabilityProviders.registerGenericAdapters()` 中会为注册了 `GENERIC_INTERNAL_INV` 的方块添加 `IItemHandler` / `IFluidHandler` 适配器，但该机制：

1. 仅遍历**方块注册表**（`BuiltInRegistries.BLOCK`），对线缆部件层级覆盖不全
2. 适配器通过 `level.getCapability()` 反向查询，部件层级的链路可能未被正确解析

## 修复内容

在 `IntegratedInterfaceBlockEntity.onRegisterCaps()` 和 `IntegratedInterfacePart.onRegisterCaps()` 中额外注册：
- `Capabilities.ItemHandler.BLOCK` → `GenericStackItemStorage`
- `Capabilities.FluidHandler.BLOCK` → `GenericStackFluidStorage`

普通和扩展两种方块/部件类型均已覆盖。此版本无纹理问题。
