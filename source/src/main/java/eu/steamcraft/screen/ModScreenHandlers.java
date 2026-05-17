package eu.steamcraft.screen;

import eu.steamcraft.SteamCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<TreeTapScreenHandler> TREE_TAP =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(SteamCraft.MOD_ID, "tree_tap"),
                    new ScreenHandlerType<>(TreeTapScreenHandler::new, FeatureSet.empty())
            );

    public static final ScreenHandlerType<BatteryScreenHandler> BATTERY =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(SteamCraft.MOD_ID, "battery"),
                    new ScreenHandlerType<>(BatteryScreenHandler::new, FeatureSet.empty())
            );

    public static final ScreenHandlerType<FridgeScreenHandler> FRIDGE =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(SteamCraft.MOD_ID, "fridge"),
                    new ScreenHandlerType<>(FridgeScreenHandler::new, FeatureSet.empty())
            );

    public static final ScreenHandlerType<FoodPouchScreenHandler> FOOD_POUCH =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(SteamCraft.MOD_ID, "food_pouch"),
                    new ScreenHandlerType<>(FoodPouchScreenHandler::new, FeatureSet.empty())
            );

    public static void registerScreenHandlers() {
        SteamCraft.LOGGER.info("Registering Screen Handlers for " + SteamCraft.MOD_ID);
    }
}

