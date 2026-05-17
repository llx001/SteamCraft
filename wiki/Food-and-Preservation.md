# Food & Preservation

In SteamCraft, food doesn't last forever. Every food item has a shelf life measured in in-game days. Managing food freshness is an important survival skill and a core educational mechanic of the mod.

---

## How Food Freshness Works

When you first open any container (chest, furnace, etc.) with food inside, or when you pick up food, it is automatically **timestamped** with the current in-game day. From that point the item begins to age.

### Visual Indicator

The colour tint of a food item shows its freshness at a glance:

| Colour | Meaning |
|---|---|
| White | Fresh — full nutrition |
| Green (shifting) | Getting old — nutrition begins to decrease near expiry |
| Dark / Near-black | Expired — causes negative effects |

---

## Effects of Eating Old or Expired Food

| Food State | Effect |
|---|---|
| Near expiry | Nutrition scales down to ~75% of base value |
| Expired | Reduced hunger and saturation; **Nausea** and **Slowness** status effects |
| Very expired | Stronger Nausea and Slowness (severity scales with age past expiry) |

---

## Food Shelf Life Reference

| Food Item | Shelf Life (in-game days) |
|---|---|
| Honey Bottle | 60 |
| Golden Carrot | 90 |
| Golden Apple | 120 |
| Enchanted Golden Apple | 240 |
| Dried Kelp | 30 |
| Chorus Fruit | 15 |
| Cookie | 14 |
| Carrot / Potato / Beetroot | 12 |
| Apple | 10 |
| Poisonous Potato | 8 |
| Bread | 7 |
| Sweet Berries / Glow Berries | 6 |
| Melon Slice / Baked Potato / Pumpkin Pie | 5 |
| Cooked Beef / Chicken / Porkchop / Fish | 4 |
| Kelp | 4 |
| Beef / Pork / Chicken (raw) | 3 |
| Rabbit (raw) | 3 |
| Cake | 3 |
| Cod / Salmon (raw) | 2 |
| Mushroom Stew / Rabbit Stew / Beetroot Soup / Suspicious Stew | 2 |
| **Beef Jerky** *(mod-added)* | Never expires |
| **MRE** *(mod-added)* | Never expires |

---

## What Happens When Food Expires

When a food item passes its expiry date inside your inventory, it **automatically converts to Rotten Food**. Rotten Food cannot be safely eaten, but it is not wasted:

> Take Rotten Food to the **Recycler** → get **Biomass** → use Biomass like Bonemeal on crops.

This creates a closed loop: grow food → eat food → rotted scraps become fertilizer → grow more food.

---

## Preservation Tools

### Fridge

The Fridge completely **stops food ageing** as long as it has power. It has 54 storage slots and costs **16 FE per food item per day**.

| Property | Value |
|---|---|
| Energy capacity | 20,000 FE |
| Energy cost | 16 FE/t |
| Storage slots | 54 |
| Accepted items | Food items only |

**Tips:**
- Connect a Battery (ideally on a rooftop) so the Fridge runs off free solar energy.
- The Fridge retains its energy when picked up.
- If it loses power, food resumes ageing normally.
- You'll hear a quiet hum when the Fridge is running.

**Crafting:** See the *Book of SteamCraft* in-game.

### Food Pouch

The Food Pouch is a portable bag that slows food spoilage while you carry it.

| Property | Value |
|---|---|
| Slowdown | Food ages at half the normal speed |
| Slots | 9 food items |
| Power required | None |

**Example:** Cooked Beef normally lasts 4 days. Inside the Food Pouch it lasts 8 days.

The pouch is ideal for long adventures or expeditions where bringing a Fridge is impractical. It checks once per in-game day.

**Crafting:** See the *Book of SteamCraft* in-game.

---

## Preserved (Shelf-Stable) Foods

SteamCraft adds two foods that **never expire** and are ideal for dangerous expeditions.

### Beef Jerky

| Property | Value |
|---|---|
| Hunger restored | 4 |
| Expiry | Never |
| How to make | Smelt Cooked Beef a second time in a furnace |

The extra heat removes moisture, creating a shelf-stable preserved meat.

### MRE (Meal Ready to Eat)

| Property | Value |
|---|---|
| Hunger restored | 10 |
| Saturation multiplier | 2.0× |
| Expiry | Never |
| How to make | Crafting recipe — see the *Book of SteamCraft* |

The MRE is a high-calorie emergency ration, perfect for the Nether or deep cave expeditions.

---

## Biomass and the Food Loop

Farm animals — cows, pigs, sheep, chickens, and rabbits — have a **50% chance** to drop one **Biomass** when killed. Combined with Rotten Food recycling, this means your farm generates its own fertilizer.

Biomass works exactly like Bonemeal: right-click on a crop or grass to apply an instant growth boost.

See [Machines](Machines) for more on the Recycler.
