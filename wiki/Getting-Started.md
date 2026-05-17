# Getting Started

This page covers how to install SteamCraft, how to take your first in-game steps, and how to obtain the in-game guidebook.

---

## Installation

### Option A — Premade Modpack (Recommended for Classrooms)

Download the ready-to-play modpack and preconfigured server package from the [Releases](../../releases) page. No additional setup is required — everything is bundled and pre-configured for an out-of-the-box classroom experience.

### Option B — Standalone Mod

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft **1.21.1**.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and [Patchouli](https://modrinth.com/mod/patchouli) and place their `.jar` files in your `.minecraft/mods/` folder.
3. Place the SteamCraft `.jar` file in the same `mods/` folder.
4. **Team Reborn Energy** is bundled inside the SteamCraft JAR — no separate download is needed.

### Dependency Summary

| Dependency | Where to Get |
|---|---|
| Fabric Loader | [fabricmc.net](https://fabricmc.net/use/installer/) |
| Fabric API | [modrinth.com/mod/fabric-api](https://modrinth.com/mod/fabric-api) |
| Patchouli | [modrinth.com/mod/patchouli](https://modrinth.com/mod/patchouli) |
| Team Reborn Energy | Bundled in SteamCraft JAR |

---

## The Book of SteamCraft

The *Book of SteamCraft* is an in-game Patchouli guidebook that explains every system with crafting recipes and tips written for students.

**To obtain a copy**, type the following command in-game (no operator privileges required):

```
/steamcraft guide
```

The book will appear in your inventory. If Patchouli is not installed the command will notify you, so make sure the dependency is present.

---

## Your First Steps

The Book of SteamCraft recommends these starting actions:

1. **Craft a Portable Scanner** to check the air quality around you. The scanner shows live CO₂, oxygen, and toxin readings.
2. **Craft a Power Bank (Battery)** and place it somewhere with open sky above it. It will charge automatically from solar power during the day.
3. **Craft a Tree Tap** and place it against a log to start collecting Rubber Sap — the foundation of the plastic and electronics crafting chains.

From there, work through the guidebook categories: Air Quality → Energy → Food → Materials → Machines.

---

## How the Systems Connect

```
Burning / Mob Presence  →  Air Pollution (CO₂, Toxins, Microplastics)
                             ↓ hurts players, kills torches, harms crops
Solar Power Banks       →  Energy for Air Scrubbers, Fridge, Monitors
Tree Tap → Rubber → Plastic / Steel  →  Tools, Hazmat Gear, Machines
Food Spoilage  →  Rotten Food  →  Recycler  →  Biomass  →  Crop Growth
```

Every decision has consequences. Build sustainably to keep your environment healthy.

---

## Building the Mod from Source

### Prerequisites

- Java Development Kit (JDK) 21 or later
- Git

### Build

```bash
git clone <repository-url>
cd SteamCraft/source

# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

The compiled JAR is placed at `source/build/libs/steamcraft-<version>.jar`.

### Running in Development

```bash
./gradlew runClient    # Linux / macOS
gradlew.bat runClient  # Windows
```

Import the `source/` directory as a Gradle project in IntelliJ IDEA. The project uses Fabric Loom and targets Java 21.

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.18.4 |
| Fabric API | 0.116.8+1.21.1 |
| Team Reborn Energy | 4.1.0 (bundled) |
| Patchouli | 1.21.1-93-FABRIC |
