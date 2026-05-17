package eu.steamcraft.air;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Centralized plant stress tiers derived from local air quality.
 */
public final class PlantAirStress {

    private PlantAirStress() {
    }

    public enum Level {
        HEALTHY,
        SLOWED,
        BLOCKED,
        SEVERE
    }

    public static boolean isEnabled(ServerWorld world) {
        return world.getGameRules().getBoolean(PlantAirGameRules.ENABLED);
    }

    public static Level getLevel(ServerWorld world, BlockPos pos) {
        if (!isEnabled(world)) {
            return Level.HEALTHY;
        }

        AirQualityData air = AirQualitySystem.get(world, pos);
        if (air == null) {
            return Level.HEALTHY;
        }

        float co2 = air.getCO2(pos);
        float toxins = air.getToxins(pos);

        int co2Slowed = getInt(world, PlantAirGameRules.CO2_SLOWED);
        int co2Blocked = Math.max(co2Slowed, getInt(world, PlantAirGameRules.CO2_BLOCKED));
        int co2Severe = Math.max(co2Blocked, getInt(world, PlantAirGameRules.CO2_SEVERE));

        int toxinSlowed = getInt(world, PlantAirGameRules.TOXIN_SLOWED);
        int toxinBlocked = Math.max(toxinSlowed, getInt(world, PlantAirGameRules.TOXIN_BLOCKED));
        int toxinSevere = Math.max(toxinBlocked, getInt(world, PlantAirGameRules.TOXIN_SEVERE));

        if (co2 >= co2Severe || toxins >= toxinSevere) {
            return Level.SEVERE;
        }
        if (co2 >= co2Blocked || toxins >= toxinBlocked) {
            return Level.BLOCKED;
        }
        if (co2 >= co2Slowed || toxins >= toxinSlowed) {
            return Level.SLOWED;
        }

        return Level.HEALTHY;
    }

    public static float getSlowCancelChance(ServerWorld world) {
        return toChance(world, PlantAirGameRules.SLOWED_CANCEL_CHANCE);
    }

    public static float getSevereCropDeathChance(ServerWorld world) {
        return toChance(world, PlantAirGameRules.SEVERE_CROP_DEATH_CHANCE);
    }

    public static float getSevereGrassDecayChance(ServerWorld world) {
        return toChance(world, PlantAirGameRules.SEVERE_GRASS_DECAY_CHANCE);
    }

    public static float getSevereLeafDecayChance(ServerWorld world) {
        return toChance(world, PlantAirGameRules.SEVERE_LEAF_DECAY_CHANCE);
    }

    private static int getInt(ServerWorld world, net.minecraft.world.GameRules.Key<net.minecraft.world.GameRules.IntRule> key) {
        return MathHelper.clamp(world.getGameRules().getInt(key), 0, 100);
    }

    private static float toChance(ServerWorld world, net.minecraft.world.GameRules.Key<net.minecraft.world.GameRules.IntRule> key) {
        return getInt(world, key) / 100.0f;
    }
}

