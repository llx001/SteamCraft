package eu.steamcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.steamcraft.air.PlantAirGameRules;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SteamCraftCommand {
    private SteamCraftCommand() {
    }

    private enum EcosystemPreset {
        EASY,
        NORMAL,
        EXTINCTION
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var root = CommandManager.literal("steamcraft")
                .then(AirDebugCommand.buildSubcommand()
                        .requires(source -> source.hasPermissionLevel(2)))
                .then(FoodDebugCommand.buildSubcommand()
                        .requires(source -> source.hasPermissionLevel(2)))
                .then(EnergyDebugCommand.buildSubcommand()
                        .requires(source -> source.hasPermissionLevel(2)))
                .then(CommandManager.literal("ecosystem")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("easy")
                                .executes(ctx -> applyPreset(ctx.getSource(), EcosystemPreset.EASY)))
                        .then(CommandManager.literal("normal")
                                .executes(ctx -> applyPreset(ctx.getSource(), EcosystemPreset.NORMAL)))
                        .then(CommandManager.literal("extinction")
                                .executes(ctx -> applyPreset(ctx.getSource(), EcosystemPreset.EXTINCTION))))
                .then(CommandManager.literal("guide")
                        .executes(ctx -> giveGuide(ctx.getSource())));

        dispatcher.register(root);
    }

    private static int giveGuide(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Item guideItem = Registries.ITEM.get(Identifier.of("patchouli", "guide_book"));
        if (guideItem == Items.AIR) {
            source.sendFeedback(() -> Text.literal("[SteamCraft] Patchouli is not installed."), false);
            return 0;
        }

        ItemStack stack = new ItemStack(guideItem);
        ComponentType<?> bookComponent = Registries.DATA_COMPONENT_TYPE
                .get(Identifier.of("patchouli", "book"));
        if (bookComponent != null) {
            setComponent(stack, bookComponent, Identifier.of("steamcraft", "book_of_steamcraft"));
        }

        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
        source.sendFeedback(() -> Text.literal("[SteamCraft] Here is your Book of SteamCraft!"), false);
        return 1;
    }

    @SuppressWarnings("unchecked")
    private static <T> void setComponent(ItemStack stack, ComponentType<?> type, T value) {
        stack.set((ComponentType<T>) type, value);
    }

    private static int applyPreset(ServerCommandSource source, EcosystemPreset preset) {
        ServerWorld world = source.getWorld();

        switch (preset) {
            case EASY -> {
                setRules(world, 60, 75, 90, 45, 60, 80, 25, 5, 5, 5);
                source.sendFeedback(() -> Text.literal("[SteamCraft] Ecosystem preset set to EASY."), true);
            }
            case NORMAL -> {
                setRules(world, 45, 65, 80, 20, 35, 55, 60, 35, 35, 20);
                source.sendFeedback(() -> Text.literal("[SteamCraft] Ecosystem preset set to NORMAL."), true);
            }
            case EXTINCTION -> {
                setRules(world, 0, 0, 0, 0, 0, 0, 100, 100, 100, 100);
                source.sendFeedback(() -> Text.literal("[SteamCraft] Ecosystem preset set to EXTINCTION."), true);
            }
        }

        return 1;
    }

    private static void setRules(ServerWorld world,
                                 int co2Slowed,
                                 int co2Blocked,
                                 int co2Severe,
                                 int toxinSlowed,
                                 int toxinBlocked,
                                 int toxinSevere,
                                 int slowedCancelChance,
                                 int severeCropDeathChance,
                                 int severeGrassDecayChance,
                                 int severeLeafDecayChance) {
        var gameRules = world.getGameRules();

        gameRules.get(PlantAirGameRules.ENABLED).set(true, world.getServer());
        gameRules.get(PlantAirGameRules.CO2_SLOWED).set(co2Slowed, world.getServer());
        gameRules.get(PlantAirGameRules.CO2_BLOCKED).set(co2Blocked, world.getServer());
        gameRules.get(PlantAirGameRules.CO2_SEVERE).set(co2Severe, world.getServer());
        gameRules.get(PlantAirGameRules.TOXIN_SLOWED).set(toxinSlowed, world.getServer());
        gameRules.get(PlantAirGameRules.TOXIN_BLOCKED).set(toxinBlocked, world.getServer());
        gameRules.get(PlantAirGameRules.TOXIN_SEVERE).set(toxinSevere, world.getServer());

        gameRules.get(PlantAirGameRules.SLOWED_CANCEL_CHANCE).set(slowedCancelChance, world.getServer());
        gameRules.get(PlantAirGameRules.SEVERE_CROP_DEATH_CHANCE).set(severeCropDeathChance, world.getServer());
        gameRules.get(PlantAirGameRules.SEVERE_GRASS_DECAY_CHANCE).set(severeGrassDecayChance, world.getServer());
        gameRules.get(PlantAirGameRules.SEVERE_LEAF_DECAY_CHANCE).set(severeLeafDecayChance, world.getServer());
    }
}

