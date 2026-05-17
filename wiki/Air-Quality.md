# Air Quality

SteamCraft adds a per-chunk air quality simulation to Minecraft. The air around you can become polluted, and pollution has real consequences for players, crops, and the environment.

---

## The Three Pollutants

Air quality is tracked using three independent values, each measured as a percentage (0–100).

### CO₂ (Carbon Dioxide)

| Detail | Value |
|---|---|
| Damage threshold | Above 60% |
| Effect | Continuous damage every 2 seconds |
| Additional effect | Vanilla torches extinguish when CO₂ is too high |

**Sources of CO₂:** Player breathing, nearby mobs breathing, furnaces running, campfires, lava, brewing stands, monster spawners, and Nether blocks placed in the Overworld.

**Natural removal:** Trees and leaves slowly absorb CO₂ over time.

### Toxins

| Detail | Value |
|---|---|
| Effect threshold | Above 40% |
| Effect | Applies the Poison status effect |
| Additional effect | Harms crops — slows or kills them |

**Sources of toxins:** Lava, polluted biomes, and industrial processes such as smelting.

### Microplastics

| Detail | Value |
|---|---|
| Effect threshold | Above 50% |
| Effect | Applies the Slowness status effect |

**Sources:** Excessive plastic use and environmental contamination.

**Note:** Microplastics decay more slowly than CO₂ and toxins, so accumulation is harder to reverse.

---

## Pollution Spread

Pollutants diffuse between adjacent chunks over time, meaning that heavily polluted areas can spread pollution to neighbouring areas. The **Nether** is always at maximum pollution — always wear protection there.

---

## Measuring Air Quality

### Portable Scanner

Hold the **Portable Scanner** in your hand to display a live HUD showing CO₂, oxygen, and toxin levels at your current position. See [Energy System](Energy-System) for technical details.

### Environmental Monitor

Place the **Environmental Monitor** block and right-click it to trigger a chunk-wide scan. It displays a colour-coded grid on your screen for a few seconds, allowing you to identify pollution hotspots. Each scan costs **200 FE**. See [Machines](Machines) for the crafting recipe.

### Hazmat Helmet HUD

Wearing the Hazmat Helmet also shows the air quality HUD — identical to the Portable Scanner display.

---

## Protecting Yourself

### Hazmat Suit

The full Hazmat Suit (Helmet, Chestplate, Leggings, Boots) grants **complete immunity** to all air quality effects:

- No CO₂ damage
- No Toxin Poison
- No Microplastic Slowness

Even just wearing the **Hazmat Helmet** alone provides full air protection and enables the HUD display.

**Crafting:** All four pieces are crafted from Rubber and related materials. See the *Book of SteamCraft* in-game for the recipes.

| Piece | Slot |
|---|---|
| Hazmat Helmet | Head |
| Hazmat Chestplate | Chest |
| Hazmat Leggings | Legs |
| Hazmat Boots | Feet |

---

## Cleaning the Air

### Natural Recovery

Air quality improves naturally over time through decay and diffusion. Planting trees accelerates CO₂ absorption.

### Air Scrubber

The **Air Scrubber** machine actively cleans all three pollutants in a **4-block radius**, consuming **40 FE every 20 ticks** (once per second). Connect it to a Battery to keep it running continuously.

**Placement tips:**
- Place scrubbers near furnaces and in enclosed rooms.
- Use the Environmental Monitor to verify scrubbers are effective.
- Multiple scrubbers cover larger spaces faster.

See [Machines](Machines) for the crafting recipe.

---

## Effect on the Environment

| Condition | Effect |
|---|---|
| CO₂ > threshold | Vanilla torches extinguish |
| Toxins > threshold | Crops slow or stop growing |
| Severe toxins | Crops can die; Grass decays; Leaves decay |
| Heavy pollution | Rich Soil degrades to ordinary Dirt |
| Heavy pollution | Dirt near Poisoned Soil converts to Poisoned Soil |

See [Soil](Soil) for more details on how pollution affects the ground.
