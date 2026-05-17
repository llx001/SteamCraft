# Energy System

SteamCraft uses the [Team Reborn Energy](https://github.com/TechReborn/Energy) API. The primary energy source is **solar power**, collected passively by Battery blocks placed under open sky during the day. Energy is distributed to adjacent machines automatically.

---

## Energy Unit

All energy values use **FE (Forge Energy / RF)**. The same unit is used by many popular Minecraft tech mods, so existing knowledge transfers directly.

---

## Batteries (Power Banks)

Batteries are the core of the energy system. They charge from sunlight and distribute power to neighbouring machines.

### Battery (Power Bank)

| Property | Value |
|---|---|
| Energy capacity | 50,000 FE |
| Solar charge rate | 5 FE per tick |
| Max input | 512 FE/t |
| Max output | 256 FE/t |
| Item charging rate | 100 FE/t |

**How to use:**
- Place the Battery in an open area with a clear sky above it (no blocks directly overhead).
- It charges automatically during the day — free, clean energy, no fuel required.
- Place machines directly adjacent to share energy automatically.
- Right-click to open the Battery GUI and place items (Electric Torch, Portable Scanner) in the charging slot.
- The Battery **retains its energy** when picked up.

**Crafting:** See the *Book of SteamCraft* in-game for the recipe.

### Advanced Battery (Advanced Power Bank)

| Property | Value |
|---|---|
| Energy capacity | 200,000 FE |
| Solar charge rate | 20 FE per tick (4× faster) |
| Max input | 2,048 FE/t |
| Max output | 1,024 FE/t |

The Advanced Battery stores four times as much energy and charges four times faster. Use it to power multiple machines simultaneously or to carry a larger reserve through the night.

---

## Energy-Consuming Blocks & Items

| Block / Item | Capacity | Consumption | Notes |
|---|---|---|---|
| Battery | 50,000 FE | — | 5 FE/t solar charge |
| Advanced Battery | 200,000 FE | — | 20 FE/t solar charge |
| Electric Torch | 10,000 FE | 1 FE/t while lit | Stays lit regardless of air quality |
| Fridge | 20,000 FE | 16 FE/t | Preserves stored food |
| Air Scrubber | 20,000 FE | 40 FE per 20-tick cycle | Cleans air in 4-block radius |
| Environmental Monitor | 5,000 FE | 200 FE per scan | Displays chunk air quality |
| Portable Scanner (item) | 5,000 FE | 2 FE/t while active | Handheld air quality reader |

---

## Electric Torch

The Electric Torch is a powered light source that functions like a regular torch but has two key advantages:

1. It **never extinguishes from CO₂** — as long as it has power.
2. It goes dark when energy runs out, rather than emitting light permanently.

**Usage:**
- Place a Battery adjacent to it to keep it charged automatically.
- Right-click to check its remaining energy.
- Sneak + right-click to toggle it on or off without breaking it.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Portable Scanner

The Portable Scanner is a handheld device that displays a live air quality HUD while held.

**HUD display:** CO₂ bar, Oxygen bar, Toxin bar — identical to the Hazmat Helmet HUD.

| Property | Value |
|---|---|
| Energy capacity | 5,000 FE |
| Drain rate | 2 FE/t while active |

**Usage:**
- Hold in your main hand to activate the HUD automatically.
- Sneak + right-click to toggle the scanner on or off.
- When energy runs out, it turns off automatically.
- Recharge by placing it in a Battery's item slot (100 FE/t).

The Portable Scanner is most useful early in the game before you have a full Hazmat Suit.

---

## Tips

- **Place Batteries on rooftops** for maximum solar exposure.
- **Chain machines next to a Battery** — energy is shared to all directly adjacent blocks.
- **The Nether has no solar charging** — bring pre-charged batteries or avoid relying on power underground.
- The Advanced Battery is the ideal central hub for powering an Air Scrubber, Fridge, and Environmental Monitor simultaneously.
