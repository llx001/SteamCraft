package eu.steamcraft.food;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.Map;

public final class FoodExpirySystem {
    public static final String CREATED_DAY_KEY = "created_day";
    public static final String PRESERVE_CARRY_KEY = "preserve_carry";
    public static final String LAST_PRESERVE_DAY_KEY = "last_preserve_day";
    public static final int DEFAULT_MAX_AGE_DAYS = 7;

    private static final int COLOR_GREEN = 0xFF80FF80;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_ROTTEN = 0xFF111111;

    private static final Map<Item, Integer> MAX_AGE_DAYS = new IdentityHashMap<>();

    static {
        // Fresh produce and cooked foods.
        register(Items.APPLE, 10);
        register(Items.BREAD, 7);
        register(Items.COOKED_BEEF, 4);
        register(Items.COOKED_CHICKEN, 4);
        register(Items.COOKED_PORKCHOP, 4);
        register(Items.COOKIE, 14);
        register(Items.CAKE, 3);
        register(Items.CARROT, 12);
        register(Items.POTATO, 12);
        register(Items.BAKED_POTATO, 5);
        register(Items.MELON_SLICE, 5);
        register(Items.DRIED_KELP, 30);
        register(Items.SWEET_BERRIES, 6);
        register(Items.HONEY_BOTTLE, 60);
        register(Items.PUMPKIN_PIE, 5);
        register(Items.MUSHROOM_STEW, 2);
        register(Items.RABBIT_STEW, 2);
        register(Items.BEETROOT_SOUP, 2);
        register(Items.SUSPICIOUS_STEW, 2);

        // Remaining vanilla foods.
        register(Items.BEEF, 3);
        register(Items.PORKCHOP, 3);
        register(Items.CHICKEN, 3);
        register(Items.COD, 2);
        register(Items.SALMON, 2);
        register(Items.TROPICAL_FISH, 2);
        register(Items.PUFFERFISH, 2);
        register(Items.COOKED_COD, 4);
        register(Items.COOKED_SALMON, 4);
        register(Items.ROTTEN_FLESH, 20);
        register(Items.SPIDER_EYE, 20);
        register(Items.GOLDEN_APPLE, 120);
        register(Items.ENCHANTED_GOLDEN_APPLE, 240);
        register(Items.POISONOUS_POTATO, 8);
        register(Items.GOLDEN_CARROT, 90);
        register(Items.RABBIT, 3);
        register(Items.COOKED_RABBIT, 4);
        register(Items.MUTTON, 3);
        register(Items.COOKED_MUTTON, 4);
        register(Items.CHORUS_FRUIT, 15);
        register(Items.BEETROOT, 12);
        register(Items.KELP, 4);
        register(Items.GLOW_BERRIES, 6);

        // Ensure every current vanilla food item has an entry.
        for (Item item : Registries.ITEM) {
            if (hasFoodComponent(item) && !MAX_AGE_DAYS.containsKey(item)) {
                MAX_AGE_DAYS.put(item, DEFAULT_MAX_AGE_DAYS);
            }
        }
    }

    private FoodExpirySystem() {
    }

    public static void stampInventory(Inventory inventory, long currentDay) {
        for (int i = 0; i < inventory.size(); i++) {
            stampIfMissing(inventory.getStack(i), currentDay);
        }
    }

    public static boolean stampIfMissing(ItemStack stack, long currentDay) {
        if (stack.isEmpty() || !isTrackedFood(stack.getItem())) {
            return false;
        }

        if (getCreatedDay(stack) != null) {
            return false;
        }

        setCreatedDay(stack, currentDay);
        return true;
    }

    public static void applyPreservation(ItemStack stack, double cancelFraction, long currentDay) {
        if (stack.isEmpty() || !isTrackedFood(stack.getItem())) {
            return;
        }

        Long createdDay = getCreatedDay(stack);
        if (createdDay == null) {
            return;
        }

        NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        if (!customData.contains(LAST_PRESERVE_DAY_KEY, NbtElement.LONG_TYPE)) {
            customData.putLong(LAST_PRESERVE_DAY_KEY, currentDay);
            customData.putDouble(PRESERVE_CARRY_KEY, 0.0);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
            return;
        }

        long elapsed = currentDay - customData.getLong(LAST_PRESERVE_DAY_KEY);
        if (elapsed <= 0L) {
            return;
        }

        if (elapsed >= 2L) {
            customData.putLong(LAST_PRESERVE_DAY_KEY, currentDay);
            customData.putDouble(PRESERVE_CARRY_KEY, 0.0);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
            return;
        }

        double carry = customData.getDouble(PRESERVE_CARRY_KEY) + cancelFraction;
        long whole = (long) Math.floor(carry);
        if (whole > 0L) {
            customData.putLong(CREATED_DAY_KEY, createdDay + whole);
            carry -= whole;
        }

        customData.putDouble(PRESERVE_CARRY_KEY, carry);
        customData.putLong(LAST_PRESERVE_DAY_KEY, currentDay);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
    }

    public static void setCreatedDay(ItemStack stack, long createdDay) {
        NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        customData.putLong(CREATED_DAY_KEY, createdDay);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
    }

    public static long setExpiresInDays(ItemStack stack, long currentDay, long expiresInDays) {
        int maxAgeDays = getMaxAgeDays(stack.getItem());
        long createdDay = currentDay + expiresInDays - maxAgeDays;
        setCreatedDay(stack, createdDay);
        return createdDay;
    }

    public static boolean isTrackedFood(Item item) {
        return MAX_AGE_DAYS.containsKey(item) || hasFoodComponent(item);
    }

    public static int getMaxAgeDays(Item item) {
        return MAX_AGE_DAYS.getOrDefault(item, DEFAULT_MAX_AGE_DAYS);
    }

    public static long getCurrentDay(World world) {
        return world.getTimeOfDay() / 24000L;
    }

    public static Long getCreatedDay(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }

        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains(CREATED_DAY_KEY, NbtElement.LONG_TYPE)) {
            return null;
        }

        return nbt.getLong(CREATED_DAY_KEY);
    }

    public static long getExpiresInDays(ItemStack stack, long currentDay) {
        Long createdDay = getCreatedDay(stack);
        if (createdDay == null) {
            return Long.MAX_VALUE;
        }

        return (createdDay + getMaxAgeDays(stack.getItem())) - currentDay;
    }

    public static float getFreshness(long createdDay, int maxAgeDays, long currentDay) {
        if (maxAgeDays <= 0) {
            return -1.0f;
        }

        return (createdDay + maxAgeDays - currentDay) / (float) maxAgeDays;
    }

    public static float getNutritionMultiplier(long expiresInDays, int maxAgeDays) {
        if (expiresInDays < 0) {
            return 0.25f;
        }

        if (maxAgeDays <= 0) {
            return 1.0f;
        }

        float lifeRatio = MathHelper.clamp(expiresInDays / (float) maxAgeDays, 0.0f, 1.0f);
        return 0.5f + lifeRatio;
    }

    public static void tryApplyExpiredFoodDebuffs(PlayerEntity player, long expiresInDays) {
        if (expiresInDays >= 0) {
            return;
        }

        long daysExpired = -expiresInDays;
        float nauseaChance = MathHelper.clamp(0.2f + daysExpired * 0.03f, 0.2f, 0.85f);
        float slownessChance = MathHelper.clamp(0.15f + daysExpired * 0.025f, 0.15f, 0.75f);
        int amplifier = (int) Math.min(2, daysExpired / 8L);

        if (player.getRandom().nextFloat() < nauseaChance) {
            int duration = 80 + (int) Math.min(320L, daysExpired * 20L);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, amplifier));
        }

        if (player.getRandom().nextFloat() < slownessChance) {
            int duration = 60 + (int) Math.min(240L, daysExpired * 16L);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, amplifier));
        }
    }

    public static int computeTint(float freshness) {
        float clamped = MathHelper.clamp(freshness, -1.0f, 1.0f);

        if (clamped >= 0.0f) {
            // Inverted from the old behavior: freshness dropping toward 0 shifts from white to green.
            float t = 1.0f - clamped;
            return lerpColor(COLOR_WHITE, COLOR_GREEN, t);
        }

        // After expiry, continue from green toward near-black as rot worsens.
        float t = -clamped;
        return lerpColor(COLOR_GREEN, COLOR_ROTTEN, t);
    }

    private static boolean hasFoodComponent(Item item) {
        return item.getComponents().contains(DataComponentTypes.FOOD);
    }

    private static void register(Item item, int days) {
        MAX_AGE_DAYS.put(item, days);
    }

    private static int lerpColor(int from, int to, float t) {
        int a = lerpChannel((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, t);
        int r = lerpChannel((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, t);
        int g = lerpChannel((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * MathHelper.clamp(t, 0.0f, 1.0f));
    }
}

