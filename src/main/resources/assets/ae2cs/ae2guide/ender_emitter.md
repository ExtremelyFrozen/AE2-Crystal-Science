---
navigation:
  parent: index.md
  title: Ender Emitter
  icon: ae2cs:ender_emitter
  position: 200
item_ids:
  - ae2cs:ender_emitter
  - ae2cs:ender_linker
---

# Ender Emitter

<Row gap="16">
  <ItemImage id="ae2cs:ender_emitter" scale="8" />
  <ItemImage id="ae2cs:ender_linker" scale="8" />
</Row>

The **Ender Emitter** is a short-range wireless network connection component provided by AECS.
It is used to automatically establish ME network links within a limited area,
making it suitable for scenarios where reducing cable complexity is desirable.

---

## Functional Role

Without a frequency band, the five base faces accept separate channel inputs and distribute their combined capacity.
The top does not accept an input. Selecting a band in the main screen disconnects all physical inputs and uses that band exclusively.
Encrypted bands require a password. The **-10, -1, +1, +10** buttons adjust the requested channel count, initially 32.
The actual allocation is limited by the channels remaining in the band; requesting 0 pauses reception.
Clearing the band link restores the five physical inputs.

The Ender Emitter is primarily used to:

- Automatically link newly placed network nodes within a limited range
- Reduce reliance on traditional AE cables

When used together with the **<ItemLink id="ae2cs:ender_broadcaster" />**,
it is possible to construct network layouts that are almost entirely independent of physical cables.

---

## Automatic Linking Behavior

Once the Ender Emitter is connected to a network cable:

- It will automatically link newly placed blocks or parts within its effective range
- Automatic linking continues until all available channels are exhausted
- The linking process requires no additional player interaction

This behavior is well suited for automation environments
where devices are frequently placed or replaced.

---

## Automatic Linking Controls

To better support complex network structures,
the Ender Emitter provides multiple control options:

- Automatic linking to **cables and cable-like parts** can be disabled  
  to avoid interfering with sub-network layouts
- Automatic linking mode can be temporarily disabled  
  for situations where manual network adjustments are required

These options help prevent automatic linking from disrupting existing network designs.

---

## Manual Linking

Even when automatic linking mode is fully disabled,
the Ender Emitter still supports manual linking:

- Use the **<ItemLink id="ae2cs:ender_linker" />**
- **Shift + Right-click** the emitter to bind it
- **Right-click** the target block to complete the link

This method is intended for precise and controlled network connections.

---

## Link Range Configuration

The Ender Emitter allows configuration of its automatic linking range:

- The area is centered on the emitter's chunk and covers the full build height without loading chunks.
- Without boosters, select **1×1 or 3×3 chunks**.
- The lower-right slot holds up to **64 wireless boosters**. **32** unlock **5×5** chunks; **64** unlock **7×7**.
- The range buttons add or remove a ring of chunks. The white outline marks the selection; dim cells are locked.
- Reducing the area or removing boosters disconnects automatic links outside the new area.
- Automatic linking only applies within the configured range
- Devices outside the automatic linking range but still within the maximum effective range:
  - Will not be linked automatically
  - Can still be connected manually

This mechanism allows automatic linking and fine-grained manual control
to coexist within the same area.

---

## Multiple Emitter Connections

Multiple Ender Emitters can be linked manually with an Ender Linker
to enable channel transmission over longer distances.

However, note that:
for very long distances or large-scale network scenarios,
the **<ItemLink id="ae2cs:ender_broadcaster" />**
is generally more suitable for centralized channel transmission.

Automatic linking and manual scans skip controllers, Ender Broadcasters, other Ender Emitters,
wireless receivers/transceivers, wireless connectors/hubs, and overloaded controllers, including wireless and advanced variants.
These exclusions do not prevent physical connections to the base inputs.
