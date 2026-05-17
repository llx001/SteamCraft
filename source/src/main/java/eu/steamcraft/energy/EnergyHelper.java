package eu.steamcraft.energy;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class EnergyHelper {
    public static final String ENABLED_KEY = "enabled";

    public static final int TORCH_MAX_ENERGY            = 10_000;
    public static final int SCANNER_MAX_ENERGY          = 5_000;
    public static final int BATTERY_MAX_ENERGY          = 50_000;
    public static final int BATTERY_ADVANCED_MAX_ENERGY = 200_000;
    public static final int FRIDGE_MAX_ENERGY           = 20_000;

    // Battery insert (charging) and extract (output) limits — per transaction tick ceiling
    public static final int BATTERY_MAX_INPUT            = 512;
    public static final int BATTERY_MAX_EXTRACT          = 256;
    public static final int BATTERY_ADVANCED_MAX_INPUT   = 2048;
    public static final int BATTERY_ADVANCED_MAX_EXTRACT = 1024;

    public static final int BATTERY_SOLAR_RATE          = 5;
    public static final int BATTERY_ADVANCED_SOLAR_RATE = 20;
    public static final int FRIDGE_PRESERVE_COST        = 16;

    public static final int SCRUBBER_MAX_ENERGY = 20_000;
    public static final int SCRUBBER_INTERVAL_TICKS = 20;
    public static final int SCRUBBER_ENERGY_PER_CYCLE = 40;
    public static final float SCRUBBER_SCRUB_RATE = 2.0f;
    public static final int SCRUBBER_RADIUS = 4;

    public static final int MONITOR_MAX_ENERGY = 5_000;
    public static final int MONITOR_CHARGE_RATE = 100;
    public static final int MONITOR_SCAN_COST = 200;

    private EnergyHelper() {}


    public static boolean isEnabled(ItemStack stack) {
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return false;
        return custom.copyNbt().getBoolean(ENABLED_KEY);
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        NbtCompound custom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        custom.putBoolean(ENABLED_KEY, enabled);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(custom));
    }
}

