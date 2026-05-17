# For Teachers

This page is a reference for educators running SteamCraft sessions. It covers server setup, all available commands, game rule configuration, ecosystem difficulty presets, and practical classroom tips.

---

## Quick Setup Checklist

1. Download the **premade modpack and server package** from the [Releases](../../releases) page — this is the fastest path for a classroom deployment.
2. If running a custom server: install Fabric Loader, Fabric API, Patchouli, and the SteamCraft JAR as described on [Getting Started](Getting-Started).
3. Give yourself operator level 2 (`/op <name>` on the server console) to access all teacher commands.
4. Run `/steamcraft ecosystem normal` at the start of a session to confirm default difficulty settings.
5. Distribute the guidebook to all students with `/steamcraft guide` (students can also run this themselves — no operator required).

---

## Command Reference

All SteamCraft commands follow the pattern `/steamcraft <subcommand>`. Commands that require **permission level 2** (operator) are marked with 🔒.

---

### `/steamcraft guide`

Gives the executing player a copy of the *Book of SteamCraft*.

```
/steamcraft guide
```

- **Permission:** Any player (no operator required)
- **Use case:** Distribute the guidebook to students at the start of a session. Students can also run this themselves.
- **Note:** Requires Patchouli to be installed. The command will display an error message if Patchouli is missing.

---

### `/steamcraft ecosystem` 🔒

Sets a named difficulty preset for the plant-and-air simulation. Adjusts multiple game rules at once.

```
/steamcraft ecosystem easy
/steamcraft ecosystem normal
/steamcraft ecosystem extinction
```

| Preset | Description | Best For |
|---|---|---|
| `easy` | High pollution thresholds, low decay chances. Plants are resilient. | Younger students, introduction sessions |
| `normal` | Balanced defaults. Realistic challenge. | Standard classroom sessions |
| `extinction` | All thresholds at 0, all death chances at 100%. Catastrophic pollution scenario. | Advanced students, demonstration of worst-case outcomes |

**Preset details:**

| Game Rule | Easy | Normal | Extinction |
|---|---|---|---|
| CO₂ Slowed threshold | 60 | 45 | 0 |
| CO₂ Blocked threshold | 75 | 65 | 0 |
| CO₂ Severe threshold | 90 | 80 | 0 |
| Toxin Slowed threshold | 45 | 20 | 0 |
| Toxin Blocked threshold | 60 | 35 | 0 |
| Toxin Severe threshold | 80 | 55 | 0 |
| Slowed cancel chance (%) | 25 | 60 | 100 |
| Severe crop death chance (%) | 5 | 35 | 100 |
| Severe grass decay chance (%) | 5 | 35 | 100 |
| Severe leaf decay chance (%) | 5 | 20 | 100 |

---

### `/steamcraft air` 🔒

Subcommands for reading and manipulating the air quality system. Useful for demonstrations and setup.

#### Scan current air quality

```
/steamcraft air
```

Prints the current O₂, CO₂, microplastics, and toxin values at your position to the chat.

#### Set a pollutant to a specific value

```
/steamcraft air set co2 <0–100>
/steamcraft air set toxins <0–100>
/steamcraft air set plastics <0–100>
```

**Example — create a heavily polluted area for a demonstration:**
```
/steamcraft air set co2 80
/steamcraft air set toxins 60
```

#### Add to a pollutant (relative change)

```
/steamcraft air add co2 <value>
/steamcraft air add toxins <value>
/steamcraft air add plastics <value>
```

Values can be negative to remove pollution. Useful for gradually increasing or decreasing pollution during a lesson.

#### Visualize air quality in 3D

```
/steamcraft air visualize
```

Renders a colour-coded 3D wireframe of pollution levels around you, covering roughly 5 chunks in each direction. The visualisation lasts ~18 seconds.

```
/steamcraft air visualize clear
```

Clears the 3D visualisation immediately.

#### Debug cell data

```
/steamcraft air debug
```

Prints internal air cell geometry data (openness, leaf density, sink permeability) for the cell you are standing in. Intended for advanced troubleshooting.

---

### `/steamcraft food` 🔒

Subcommands for manipulating food expiry on held items. Useful for classroom demonstrations of the food freshness system.

#### Set the expiry of the held food item

```
/steamcraft food set <days>
```

Sets the held food item to expire in the given number of in-game days from now.

**Examples:**
```
/steamcraft food set 0    # expires today
/steamcraft food set 7    # expires in a week
/steamcraft food set 365  # expires in a year (effectively permanent for a session)
```

#### Instantly expire the held food item

```
/steamcraft food expire
```

Sets the held food to already-expired status (−1 day). The item will appear dark and cause negative effects when eaten.

**Use case:** Demonstrate food spoilage effects live — hold a piece of bread, run `/steamcraft food expire`, then eat it in front of students to show the Nausea and Slowness effects.

---

### `/steamcraft energy` 🔒

Subcommands for reading and adjusting energy levels. Useful for starting demonstrations with fully-charged equipment.

#### Read energy levels

```
/steamcraft energy
```

Prints the energy level of the item in your main hand (if it is an energy item) and the energy level of the block you are looking at (if it is a Battery or Electric Torch).

#### Set energy on a held item

```
/steamcraft energy set <amount>
```

Sets the energy of the item in your main hand to the specified amount. The value is clamped to the item's maximum capacity.

**Example — fully charge a Portable Scanner before handing it to a student:**
```
# Hold the Portable Scanner
/steamcraft energy set 5000
```

#### Set energy on a targeted block

```
/steamcraft energy setblock <amount>
```

Look at a Battery or Electric Torch block, then run this command to set its energy directly.

**Example — pre-charge a Battery at the start of class:**
```
# Look at the Battery block
/steamcraft energy setblock 50000
```

---

## Game Rules Reference

These game rules can be set manually with `/gamerule` if you need more control than the ecosystem presets provide.

| Game Rule | Type | Default | Description |
|---|---|---|---|
| `steamcraftPlantAirEnabled` | Boolean | `true` | Enable/disable the plant-air stress system entirely |
| `steamcraftPlantAirCo2Slowed` | Integer (0–100) | `45` | CO₂ level at which crops begin to slow |
| `steamcraftPlantAirCo2Blocked` | Integer (0–100) | `65` | CO₂ level at which crops stop growing |
| `steamcraftPlantAirCo2Severe` | Integer (0–100) | `80` | CO₂ level at which severe effects begin |
| `steamcraftPlantAirToxinSlowed` | Integer (0–100) | `20` | Toxin level at which crops begin to slow |
| `steamcraftPlantAirToxinBlocked` | Integer (0–100) | `35` | Toxin level at which crops stop growing |
| `steamcraftPlantAirToxinSevere` | Integer (0–100) | `55` | Toxin level at which severe effects begin |
| `steamcraftPlantAirSlowedCancelChance` | Integer (0–100) | `60` | % chance that a slowed growth tick is cancelled |
| `steamcraftPlantAirSevereCropDeathChance` | Integer (0–100) | `35` | % chance per tick that a crop dies under severe pollution |
| `steamcraftPlantAirSevereGrassDecayChance` | Integer (0–100) | `35` | % chance per tick that grass decays under severe pollution |
| `steamcraftPlantAirSevereLeafDecayChance` | Integer (0–100) | `20` | % chance per tick that leaves decay under severe pollution |

**Example — disable the plant stress system entirely while teaching another topic:**
```
/gamerule steamcraftPlantAirEnabled false
```

**Example — make pollution effects extremely visible for a demonstration:**
```
/gamerule steamcraftPlantAirCo2Severe 10
/gamerule steamcraftPlantAirSevereCropDeathChance 90
```

---

## Practical Classroom Scenarios

### Scenario 1 — Clean vs. Polluted World

1. Start with `/steamcraft ecosystem easy`.
2. Let students build a small farm and note that crops grow well.
3. Run `/steamcraft air set co2 85` and `/steamcraft air set toxins 70` to simulate heavy pollution.
4. Have students observe the effects on crops, torches, and themselves.
5. Challenge students to restore air quality using Air Scrubbers and trees.

### Scenario 2 — Food Waste & the Circular Economy

1. Give students food items and let them expire naturally (use `/steamcraft food set 0` for instant demonstration).
2. Show that expired food becomes Rotten Food.
3. Have students bring Rotten Food to a Recycler and observe Biomass output.
4. Apply Biomass to crops — close the loop.

### Scenario 3 — Solar Energy Challenge

1. Remove all pre-charged batteries from student inventories.
2. Challenge students to build a solar-powered base that keeps an Air Scrubber, Fridge, and Electric Torches running through the night.
3. Use `/steamcraft energy` to check battery levels at various times.

### Scenario 4 — Extinction Event

1. Switch to `/steamcraft ecosystem extinction`.
2. Run:
   ```
   /steamcraft air set co2 100
   /steamcraft air set toxins 100
   /steamcraft air set plastics 100
   ```
3. Observe rapid environmental collapse.
4. Challenge students to survive and reverse the damage.
5. Restore to `/steamcraft ecosystem normal` once the lesson is complete.

---

## Giving Items Directly

As an operator you can use standard Minecraft commands to give students items instantly:

```
/give @a steamcraft:portable_scanner 1
/give @a steamcraft:battery 1
/give @a steamcraft:hazmat_helmet 1
/give @a steamcraft:hazmat_chestplate 1
/give @a steamcraft:hazmat_leggings 1
/give @a steamcraft:hazmat_boots 1
/give @a steamcraft:recycler 1
/give @a steamcraft:air_scrubber 1
/give @a steamcraft:environmental_monitor 1
/give @a steamcraft:fridge 1
/give @a steamcraft:tree_tap 1
/give @a steamcraft:food_pouch 1
```

Useful for skipping the early crafting grind when class time is limited.

---

## Item ID Reference

| Item | Item ID |
|---|---|
| Battery (Power Bank) | `steamcraft:battery` |
| Advanced Battery | `steamcraft:battery_advanced` |
| Electric Torch | `steamcraft:electric_torch` |
| Portable Scanner | `steamcraft:portable_scanner` |
| Air Scrubber | `steamcraft:air_scrubber` |
| Environmental Monitor | `steamcraft:environmental_monitor` |
| Fridge | `steamcraft:fridge` |
| Recycler | `steamcraft:recycler` |
| Tree Tap | `steamcraft:tree_tap` |
| Food Pouch | `steamcraft:food_pouch` |
| Hazmat Helmet | `steamcraft:hazmat_helmet` |
| Hazmat Chestplate | `steamcraft:hazmat_chestplate` |
| Hazmat Leggings | `steamcraft:hazmat_leggings` |
| Hazmat Boots | `steamcraft:hazmat_boots` |
| Plastic Sheet | `steamcraft:plastic_sheet` |
| Rubber | `steamcraft:rubber` |
| Steel Ingot | `steamcraft:steel_ingot` |
| Circuit Board | `steamcraft:circuit_board` |
| Wire | `steamcraft:wire` |
| Biomass | `steamcraft:biomass` |
| Beef Jerky | `steamcraft:beef_jerky` |
| MRE | `steamcraft:mre` |
| Rich Soil | `steamcraft:rich_soil` |
| Poisoned Soil | `steamcraft:poisoned_soil` |
