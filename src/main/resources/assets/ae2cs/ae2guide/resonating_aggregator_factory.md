---
navigation:
  parent: crystal_aggregator.md
  title: Resonating Aggregator Factory
  icon: ae2cs:resonating_aggregator_factory
  position: 10
item_ids:
  - ae2cs:resonating_aggregator_factory
---

# Resonating Aggregator Factory

<Row gap="16">
  <BlockImage id="ae2cs:resonating_aggregator_factory" scale="8" />
</Row>

The **Resonating Aggregator Factory** is the batch-processing variant of the [Crystal Aggregator](crystal_aggregator.md). It uses the same recipes to produce advanced components and Crystal Seeds.

## Inventory and processing

- **9 input slots** arranged in a 3×3 grid and **3 output slots**.
- Separate fluid input and output tanks, each holding **16,000 mB**. Use the tank controls to transfer fluid containers.
- Every completed recipe pays its full material, fluid and energy cost.

Meteorite Overclock Cards control the maximum batch size:

| Installed cards | Maximum batch | Target cycle |
| --- | --- | --- |
| 0 | 8 | Based on recipe energy |
| 1 | 32 | 64 ticks |
| 2 | 64 | 16 ticks |
| 3 | 256 | 4 ticks |
| 4 | 1024 | 1 tick |

Actual batches depend on available ingredients, fluid, energy and output space. Missing resources pause processing; energy already paid remains attached to the batch.

---

## Upgrade cards and storage

- Four dedicated slots accept up to **4 <ItemLink id="ae2cs:overload_card" />**.
- Two dedicated slots accept up to **2 <ItemLink id="ae2cs:resonating_stacking_card" />**.
- Each slot holds one card. Standard Speed Cards are not accepted.
- Hover over the upgrade panel to see **Compatible Upgrades** and the machine-wide card limits.

For items that normally stack to 64, installing 0, 1 or 2 Stacking Cards gives **64, 256 or 1024 items per slot**. Items with smaller stack limits scale proportionally; unstackable items remain limited to one. A Stacking Card cannot be removed while any stored stack would exceed the reduced limit.

## Controls and automation

Use the left **?** button to open this factory's guide. The adjacent button opens [Side Configuration](side_config.md). Configure per-side insertion/extraction and automatic import/export there.
