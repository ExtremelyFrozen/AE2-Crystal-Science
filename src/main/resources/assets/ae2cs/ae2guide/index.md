---
navigation:
  title: OMNI Storage Cellsasd
  position: 210
---

# OMNI Storage Cellsasd

> Store **any AE-supported resource types**—**items, fluids**, and other AE2 resource keys—**together in a single storage cell**.

<Row>
  <ItemImage id="omni_cell_256m" scale="4" />
  <ItemImage id="complex_omni_cell_256m" scale="4" />
  <ItemImage id="quantum_omni_cell_256m" scale="4" />
</Row>

## Overview
This mod provides three major series with capacity tiers from **1k → 256m**.  
All series can store any AE-supported resource types; the difference lies in the **maximum number of distinct types that can be stored simultaneously**.

### Series & Type Limits
| Series | Type Limit (distinct types stored at once) | Available Capacities |
|---|---:|---|
| **OMNI Storage Cells** | **63** types | 1k / 4k / 16k / 64k / 256k / 1m / 4m / 16m / 64m / 256m |
| **Complex OMNI Storage Cells** | Increases with capacity, **up to ~6400** types | 1k → 256m (same as above) |
| **Quantum Storage Cells** | **Unlimited** | 1k → 256m (same as above) |

> Tip: All three series share the **same capacity tiers**; only their “type-limit” strategy differs. Choose based on your network scale and the diversity of your resources.

## Details
- **OMNI Storage Cells**  
  Fixed limit of **up to 63** types stored simultaneously. Simple and easy to manage—ideal for typical networks.
- **Complex OMNI Storage Cells**  
  Start with fewer types but increase the type limit as capacity scales, **reaching up to ~6400** types at the top tier.
- **Quantum Storage Cells**  
  **No type limit**—well-suited for ultra-large / highly diverse networks and late-game stages in modpacks.

## Data Management
- **Cell data stored independently**: the item only keeps a small amount of data for client-side tooltip display, meaning **no risk of oversized NBT**.
- **Data location** (saved separately; see [Storage Cells](cells.md) for details).
