package eu.steamcraft.air;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public final class PlantAirGameRules {

    private PlantAirGameRules() {
    }

    private static final GameRules.Category CATEGORY = GameRules.Category.UPDATES;

    public static final GameRules.Key<GameRules.BooleanRule> ENABLED = GameRuleRegistry.register(
            "steamcraftPlantAirEnabled",
            CATEGORY,
            GameRuleFactory.createBooleanRule(true)
    );

    public static final GameRules.Key<GameRules.IntRule> CO2_SLOWED = GameRuleRegistry.register(
            "steamcraftPlantAirCo2Slowed",
            CATEGORY,
            GameRuleFactory.createIntRule(45, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> CO2_BLOCKED = GameRuleRegistry.register(
            "steamcraftPlantAirCo2Blocked",
            CATEGORY,
            GameRuleFactory.createIntRule(65, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> CO2_SEVERE = GameRuleRegistry.register(
            "steamcraftPlantAirCo2Severe",
            CATEGORY,
            GameRuleFactory.createIntRule(80, 0, 100)
    );

    public static final GameRules.Key<GameRules.IntRule> TOXIN_SLOWED = GameRuleRegistry.register(
            "steamcraftPlantAirToxinSlowed",
            CATEGORY,
            GameRuleFactory.createIntRule(20, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> TOXIN_BLOCKED = GameRuleRegistry.register(
            "steamcraftPlantAirToxinBlocked",
            CATEGORY,
            GameRuleFactory.createIntRule(35, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> TOXIN_SEVERE = GameRuleRegistry.register(
            "steamcraftPlantAirToxinSevere",
            CATEGORY,
            GameRuleFactory.createIntRule(55, 0, 100)
    );

    public static final GameRules.Key<GameRules.IntRule> SLOWED_CANCEL_CHANCE = GameRuleRegistry.register(
            "steamcraftPlantAirSlowedCancelChance",
            CATEGORY,
            GameRuleFactory.createIntRule(60, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> SEVERE_CROP_DEATH_CHANCE = GameRuleRegistry.register(
            "steamcraftPlantAirSevereCropDeathChance",
            CATEGORY,
            GameRuleFactory.createIntRule(35, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> SEVERE_GRASS_DECAY_CHANCE = GameRuleRegistry.register(
            "steamcraftPlantAirSevereGrassDecayChance",
            CATEGORY,
            GameRuleFactory.createIntRule(35, 0, 100)
    );
    public static final GameRules.Key<GameRules.IntRule> SEVERE_LEAF_DECAY_CHANCE = GameRuleRegistry.register(
            "steamcraftPlantAirSevereLeafDecayChance",
            CATEGORY,
            GameRuleFactory.createIntRule(20, 0, 100)
    );

    public static void register() {
        // Intentionally no-op: touching this class ensures gamerules register.
    }
}

