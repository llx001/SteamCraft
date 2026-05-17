# SteamCraft

About the Project

In a dynamic online gathering, the STEAMCAFT Erasmus+ project consortium organizations set the stage for an innovative educational journey. Primary school education is about to undergo a remarkable transformation as students dive into the intricacies of renewable energy and smart city development through a groundbreaking approach.


STEM-Oriented Education: The project, titled "STEM-Oriented Education in the Virtual World with Customized Minecraft Modifications," promises to revolutionize primary school learning. 

Students will delve into the systemic functioning of renewable energy sources and green alternatives, alongside mastering the methodology and tools of smart city development.

Immersive Learning with Minecraft: Leveraging the power of Minecraft software, the project integrates embedded modifications tailored to enhance educational experiences. Through this platform, students will engage in interactive simulations and quests, fostering deeper understanding and critical thinking.

Pedagogical Innovation: The project's pedagogical framework emphasizes experiential pedagogy and gamification. By immersing students in hands-on activities and gamified challenges, learning becomes more engaging, impactful, and memorable.

Consortium Organizations: With three consortium organizations at the helm, the project brings together expertise from across Europe. Collaborative efforts ensure a holistic approach to curriculum development and implementation, maximizing the project's impact.

## Getting Started

There are two ways to experience SteamCraft:

- **Standalone mod** — install only the SteamCraft mod alongside its dependencies and forge your own journey. See *Installing as a Player* below for instructions.
- **Premade modpack & server** — grab the ready-to-play modpack and preconfigured server package from the [Releases](../../releases) page and jump straight into the curated experience with no extra setup.

For full documentation on features, crafting recipes, and classroom guides, visit the **[SteamCraft Wiki](../../wiki)** on GitHub.

If you have Pathcouli installed you can use the */steamcraft guide* command to get a copy of Book of SteamCraft which has a detailed description for students in-game
---

## Consortium:

| | |
|---|---|
| [![RCISD](assets/RCISD_logo.jpeg)](https://rcisd.eu/) | **Coordinator:** [Regional Centre for Information and Scientific Development Ltd.](https://rcisd.eu/) |
| [![JKU](assets/JKU_Logo.png)](https://www.jku.at/) | **Partner:** JKU – [Johannes Kepler Universität Linz](https://www.jku.at/) |
| [![JYU](assets/JYU_Logo.png)](https://www.jyu.fi/en) | **Partner:** [University of Jyväskylä](https://www.jyu.fi/en) |

| | |
|---|---|
| **Project name** | STEM oriented education in the virtual world with customised Minecraft modifications, STEAMCRAFT |
| **Project Reference Number** | 2023-1-HU01-KA220-SCH-000166153 |
| **Action type, Programme** | KA220-SCH - Cooperation partnerships in school education, Erasmus+ |
| **National Agency** | HU01 – Tempus Public Foundation |
| **Erasmus+ Result Platform** | [Link](https://erasmus-plus.ec.europa.eu/projects/search/details/2023-1-HU01-KA220-SCH-000166153) |

## Mod Features

SteamCraft is a Fabric mod for Minecraft 1.21.1 that teaches renewable energy, environmental responsibility, and sustainability through hands-on gameplay. The mod introduces four interconnected systems — air quality, energy, food preservation, and recycling — that react to how players treat the world. An in-game *Book of SteamCraft* (Patchouli guidebook) explains each system.

### Air Quality

A per-chunk simulation tracks three pollutants — **CO₂**, **toxins**, and **microplastics** — that diffuse between chunks over time. Pollution sources include player and mob breathing, lava, campfires, brewing stands, monster spawners, and Nether blocks placed in the Overworld. Trees and leaves slowly absorb CO₂.

- CO₂ above 60% causes continuous damage every 2 seconds
- Toxins above 40% apply Poison
- Microplastics above 50% apply Slowness
- Vanilla torches extinguish when local air quality deteriorates too far
- The Nether is always at maximum pollution
- A full **Hazmat Suit** protects against all air quality effects
- The **Environmental Monitor** block displays live pollutant readings for its chunk
- The **Portable Scanner** item shows current air quality at the player's position
- The **Air Scrubber** machine consumes energy to actively clean the air within a 4-block radius

### Energy

The mod uses the [Team Reborn Energy](https://github.com/TechReborn/Energy) API. Batteries charge passively from simulated solar energy when they have sky access and distribute power to adjacent machines.

| Block / Item | Capacity | Notes |
|---|---|---|
| Battery | 50,000 FE | 5 FE/t solar charge; max input 512 FE, output 256 FE/t |
| Advanced Battery | 200,000 FE | 20 FE/t solar charge; max input 2,048 FE, output 1,024 FE/t |
| Electric Torch | 10,000 FE | Stays lit regardless of air quality; goes dark without power |
| Fridge | 20,000 FE | Slows food expiry for stored items; costs 16 FE/t |
| Air Scrubber | 20,000 FE | 40 FE per cleaning cycle (every 20 ticks) |
| Environmental Monitor | 5,000 FE | 200 FE per air quality scan |

### Food & Preservation

Every food item has a shelf life tracked in in-game days. Opening any container (chest, furnace, etc.) automatically stamps unstamped food with the current day. Items display their freshness through a color tint: white when fresh, shifting to green as they age, and near-black once expired.

- Expired food inflicts scaled Nausea and Slowness; the longer it has been expired, the stronger the effect
- Nutrition from food near its expiry scales down to 75% of its base value
- The **Fridge** block (requires power) preserves stored food, extending shelf life
- The **Food Pouch** is a portable bag that keeps its contents fresh
- **Beef Jerky** (4 nutrition) and **MRE** (10 nutrition, 2× saturation) are shelf-stable mod-added foods; they never expire
- Livestock (cows, pigs, sheep, chickens, rabbits) have a 50% chance to drop **Biomass** on death

### Materials & Tools

The mod adds a rubber-to-plastic processing chain alongside a steel tier:

- **Tree Tap** — place against any log to slowly extract **Rubber Sap** (4 sap per log; the log breaks when fully tapped)
- Smelt Rubber Sap into **Rubber**, craft Rubber into **Plastic Sheet**
- **Plastic tools and armor** — lighter than iron but fully recyclable
- **Steel Ingot** — smelted from iron; used for **Steel tools** (stronger than iron) and machine components
- **Copper, Iron, and Steel Plates / Rods / Coils / Wire** — crafting components for machines, circuit boards, and electrical items

### Recycling

The **Recycler** machine recovers base materials from tools and armor (70% success rate per attempt):

| Input | Output |
|---|---|
| Plastic tools / Plastic armor | Plastic Sheet |
| Steel tools | Steel Ingot |
| Rotten Food | Biomass |
| Wooden tools | Oak Planks |
| Stone tools | Cobblestone |
| Iron tools | Iron Ingot |
| Golden tools | Gold Ingot |
| Diamond tools | Diamond |
| Netherite tools | Netherite Ingot |

Broken items (zero durability) cannot be recycled. Hoppers can automate both input and output slots.

### Soil

- **Rich Soil** — boosts crop growth rate above it; spreads to adjacent Dirt blocks in clean air; degrades back to ordinary Dirt under severe pollution
- **Poisoned Soil** — created by environmental damage; illustrates the consequences of pollution for students

## Building the Project

### Prerequisites

- [Java Development Kit (JDK) 21](https://adoptium.net/) or later
- Git

### Build

```bash
# 1. Clone the repository
git clone <repository-url>
cd SteamCraft/source

# 2. Build the mod JAR
# Linux / macOS
./gradlew build
# Windows
gradlew.bat build
```

The compiled JAR is placed in `source/build/libs/steamcraft-<version>.jar`.

### Running in Development

```bash
# Launch a test Minecraft client with the mod loaded
./gradlew runClient    # Linux / macOS
gradlew.bat runClient  # Windows
```

For IDE setup, import the `source/` directory as a Gradle project in [IntelliJ IDEA](https://www.jetbrains.com/idea/). The project uses [Fabric Loom](https://fabricmc.net/wiki/documentation:fabric_loom) and targets Java 21.

**Key dependency versions** (see [`source/gradle.properties`](source/gradle.properties)):

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.18.4 |
| Fabric API | 0.116.8+1.21.1 |
| Team Reborn Energy | 4.1.0 (bundled in JAR) |
| Patchouli | 1.21.1-93-FABRIC |

### Installing as a Player

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and [Patchouli](https://modrinth.com/mod/patchouli) and place their JARs in your `.minecraft/mods/` folder.
3. Place the SteamCraft JAR in the same `mods/` folder.
4. Team Reborn Energy is bundled inside the SteamCraft JAR — no separate download required.

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository and create a branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Make your changes**, following the existing code style and naming conventions.
3. **Test in-game** by running `./gradlew runClient` and verifying your changes work as expected.
4. **Commit** with a clear, descriptive message explaining what changed and why.
5. **Open a Pull Request** against `main`, describing what you changed and including in-game screenshots where relevant.

**Guidelines:**

- Keep pull requests focused — one feature or fix per PR.
- If you add a new gameplay mechanic, update or add entries in the *Book of SteamCraft* (Patchouli guide) so students can discover it in-game.
- For significant changes, open an issue first to discuss the approach before implementing.
- Bug reports and feature requests are welcome as [GitHub Issues](../../issues).

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for the full text.

In short: you are free to use, modify, and distribute this mod, provided that any modifications you distribute are also released under GPL-3.0 and include the source code.

---

![EU Erasmus+](assets/eu-loo.gif)

Financed by the European Union. Views and opinions expressed are however those of the author(s) and do not necessarily reflect those of the European Union or the Tempus Public Foundation. Neither the European Union nor the granting authority can be held responsible for them.
