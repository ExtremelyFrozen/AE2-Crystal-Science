# 机器外部抽取一致性修复实施计划

> **面向 AI 代理的工作者：** 必须使用 `subagent-driven-development`（推荐）或 `executing-plans` 逐任务实现此计划。步骤使用复选框语法跟踪进度。

**目标：** 使机器物品能力在模拟抽取与正式抽取时返回一致结果，避免外部管道因错误模拟结果刷物品。

**架构：** 修复共享的 `AppEngInvComponent` 输入端口包装器和 `SideConfigComponent` 侧面包装器。前者负责端口级输入不可抽取，后者负责侧面策略。通过 `PulseCentrifugeBlockEntity` 的 `GameTest` 从 NeoForge 物品能力边界验证行为。

**技术栈：** Java 21、NeoForge GameTest、Applied Energistics 2 内部库存。

---

### 任务 1：锁定物品能力的模拟与执行契约

**文件：**
- 新建：`src/test/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponentTests.java`

- [ ] **步骤 1：编写失败的 `GameTest`**

在 `SideConfigComponentTests` 中放置 `PulseCentrifugeBlockEntity`，经 `Capabilities.ItemHandler.BLOCK` 获取北侧 `IItemHandler`。添加以下测试：

```java
handler.extractItem(0, 1, true);   // 输入库存：预期 ItemStack.EMPTY
handler.extractItem(0, 1, false);  // 输入库存：预期 ItemStack.EMPTY
```

另一测试向输出槽放入铁锭并将北侧配置为 `SidePolicy.INSERT` 与 `SidePolicy.NONE`；每种策略都断言模拟、执行均返回 `ItemStack.EMPTY`，且输出计数不变。第三个测试将北侧设为 `SidePolicy.ALL`，断言模拟返回铁锭而不改变计数，执行返回同等铁锭并将计数减少 1。

- [ ] **步骤 2：运行测试并确认失败**

运行：`./gradlew runGameTestServer`

预期：新增测试失败，因为输入端口或禁止抽取侧在模拟模式下返回物品。

### 任务 2：使共享包装器在模拟和执行时应用相同的抽取规则

**文件：**
- 修改：`src/main/java/io/github/lounode/ae2cs/common/machine/component/AppEngInvComponent.java:76-86`
- 修改：`src/main/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponent.java:172-178`
- 修改：`src/main/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponent.java:217-225`
- 测试：`src/test/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponentTests.java`

- [ ] **步骤 1：最小化实现端口规则**

将 `AppEngInvComponent` 的 `INPUT` 包装器改为始终返回 `ItemStack.EMPTY`，不再对 `simulate == true` 调用 `rawInv.extractItem`。

- [ ] **步骤 2：最小化实现侧面规则**

让 `SideConfigComponent` 的通用库存包装器及 `appEngInvForSide` 的 `extractItem` 对模拟和执行共用 `policy.allowExtract()` 判断。禁止抽取时直接返回 `0` 或 `ItemStack.EMPTY`；允许抽取时将传入的模式或 `simulate` 值原样委托给底层库存。

- [ ] **步骤 3：运行定向回归测试**

运行：`./gradlew runGameTestServer`

预期：三个新增能力测试通过，且输入、禁止侧和输出侧都满足设计文档的抽取规则。

- [ ] **步骤 4：运行格式与完整回归验证**

运行：`./gradlew spotlessCheck runGameTestServer`

预期：格式检查和全部游戏测试通过。

- [ ] **步骤 5：提交修复**

```bash
git add src/main/java/io/github/lounode/ae2cs/common/machine/component/AppEngInvComponent.java \
        src/main/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponent.java \
        src/test/java/io/github/lounode/ae2cs/common/machine/component/SideConfigComponentTests.java
git commit -m "fix: 保持机器抽取模拟与执行一致"
```
