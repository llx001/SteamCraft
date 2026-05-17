# Machines

This page is a reference for all SteamCraft machines and their mechanics.

---

## Tree Tap

The Tree Tap extracts **Rubber Sap** from any log — no energy required.

| Property | Detail |
|---|---|
| Input | Any log (placed adjacent) |
| Output | Rubber Sap |
| Sap per log | 4 |
| Energy required | None |

**How to use:**
1. Place the Tree Tap against the side of any log (not the top or bottom).
2. Wait for the Tap to drill through its cycles and fill the output slot.
3. Right-click the Tap to collect the sap, or place a **hopper underneath** to collect automatically.
4. After 4 collections the log is destroyed and the Tap stops.

**Note:** Keep a forest nearby or replant trees regularly for a steady rubber supply. The Tree Tap is the entry point for the entire [rubber and plastic processing chain](Materials-and-Crafting).

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Recycler

The Recycler converts waste — old tools, armour, and rotten food — back into useful resources.

| Property | Detail |
|---|---|
| Energy required | None |
| Processing time | ~2 seconds per item |
| Success rate | 70% (30% chance the item is lost) |

### What Can Be Recycled

| Input | Output |
|---|---|
| Plastic tools / Plastic armour | Plastic Sheet |
| Steel tools | Steel Ingot |
| Rotten Food | Biomass |
| Wooden tools | Oak Planks |
| Stone tools | Cobblestone |
| Iron tools | Iron Ingot |
| Golden tools | Gold Ingot |
| Diamond tools | Diamond |
| Netherite tools | Netherite Ingot |

**Important:** Items at **zero durability (broken)** cannot be recycled.

**Automation:** Use a **hopper** to feed items into the input slot and another hopper to collect from the output slot. Right-click the Recycler to inspect its contents.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Air Scrubber

The Air Scrubber actively removes all three pollutants from the air in a **4-block radius** around it.

| Property | Value |
|---|---|
| Energy capacity | 20,000 FE |
| Energy cost | 40 FE per cleaning cycle (every 20 ticks) |
| Cleaning radius | 4 blocks |
| Pollutants removed | CO₂, Toxins, Microplastics |

**How to use:**
- Place the Air Scrubber anywhere and connect a Battery adjacent to it.
- It runs automatically as long as it has power.
- Use the Environmental Monitor to confirm it is working.
- Combine multiple scrubbers for faster clean-up in large spaces.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Environmental Monitor

The Environmental Monitor scans air quality across the surrounding area and displays a colour-coded map.

| Property | Value |
|---|---|
| Energy capacity | 5,000 FE |
| Energy cost per scan | 200 FE |
| Scan area | 8×8 chunks around the monitor |

**How to use:**
1. Place the Environmental Monitor and connect a Battery adjacent to it.
2. Right-click the Monitor to trigger a scan.
3. A colour-coded grid appears on your screen for a few seconds, showing pollution levels per area (from clean to heavily polluted).

Use the Monitor to find pollution hotspots and decide where to place Air Scrubbers for maximum effectiveness.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Fridge

The Fridge preserves food indefinitely while powered, preventing any ageing of stored food items.

| Property | Value |
|---|---|
| Energy capacity | 20,000 FE |
| Energy cost | 16 FE/t |
| Storage slots | 54 |
| Accepted items | Food items only |

**How to use:**
- Right-click to open the 54-slot inventory.
- Connect a Battery adjacent to the Fridge.
- A quiet hum indicates it is running.
- If power runs out, food resumes ageing normally.
- The Fridge retains its energy when picked up.

See [Food & Preservation](Food-and-Preservation) for more on food mechanics.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Battery (Power Bank)

See [Energy System](Energy-System) for full Battery documentation.

| Property | Battery | Advanced Battery |
|---|---|---|
| Capacity | 50,000 FE | 200,000 FE |
| Solar charge | 5 FE/t | 20 FE/t |
| Item charging | 100 FE/t | 100 FE/t |

---

## Automation Notes

| Machine | Hopper Input | Hopper Output |
|---|---|---|
| Tree Tap | No | Yes (below) |
| Recycler | Yes | Yes |
| Fridge | Yes (food only) | Yes |
