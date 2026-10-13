---
navigation:
  parent: index.md
  title: Crystal Pulverizer
  icon: ae2cs:crystal_pulverizer
  position: 70
item_ids:
  - ae2cs:crystal_pulverizer
  - ae2cs:resonating_pulverizer_factory
---

# Crystal Pulverizer

<Row gap="16">
  <BlockImage id="ae2cs:crystal_pulverizer" scale="2" />
  <BlockImage id="ae2cs:resonating_pulverizer_factory" scale="2" />
</Row>

The **Crystal Pulverizer** is a mid-game crystal processing machine provided by AECS.
It is designed to efficiently convert ores or crystal-based raw materials into their corresponding dusts.
Functionally, it inherits the processing system of the Quartz Grindstone,
while significantly improving operating efficiency and automation capabilities.

---

## Functional Role

The Crystal Pulverizer is positioned as an upgraded replacement for the Quartz Grindstone:

- Executes the same recipe types as the Quartz Grindstone
- Designed for mid-game and later automated production environments

Under identical recipe conditions, the Crystal Pulverizer completes pulverizing operations at a much higher processing speed.

---

## Power and Efficiency

The Crystal Pulverizer supports **electric power only** and does not allow manual operation.

- Installing **Speed Cards** can greatly increase processing throughput per unit time

With multiple Speed Cards installed, the Crystal Pulverizer is capable of meeting the demands of large-scale dust production.

---

## Machine Characteristics

The Crystal Pulverizer has the following general machine properties:

- Supports the **[Side Configuration](side_config.md)** system
- Can automatically draw power from the connected **ME network**
- Can also receive power via external energy cables
- **Participates in channel transmission**

---

## Standard and Upgraded Variants

The Crystal Pulverizer and the Resonating Pulverizer Factory share the same recipes,
as well as the same power, upgrade and automation behavior.
They differ only in slot configuration and processing capacity:

- **Crystal Pulverizer**
  - 1 input slot and 4 output slots
  - Processes 1 item per operation

- **Resonating Pulverizer Factory**
  - 3x3 input and output grids
  - Processes up to **32** items per operation

The Resonating Pulverizer Factory does not shorten a single operation;
it completes a full batch in approximately the time the Crystal Pulverizer requires for one item.
Processing capacity therefore scales with the batch size rather than with the cycle speed.
Speed Cards retain their standard effect and additionally increase the batch size by 4 each;
the Meteorite Overclock Card follows the same rule at 16 each, up to a maximum of **32**.
