package eu.steamcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.steamcraft.food.FoodExpirySystem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class FoodDebugCommand {
    private FoodDebugCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(buildSubcommand());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildSubcommand() {
        return CommandManager.literal("food")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("days", IntegerArgumentType.integer(-36500, 36500))
                                .executes(ctx -> setHeldFoodExpiry(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "days")))))
                .then(CommandManager.literal("expire")
                        .executes(ctx -> expireHeldFood(ctx.getSource())));
    }

    private static int setHeldFoodExpiry(ServerCommandSource source, int days) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            source.sendError(Text.literal("Hold a food item in your main hand."));
            return 0;
        }

        if (!FoodExpirySystem.isTrackedFood(stack.getItem())) {
            source.sendError(Text.literal("Held item is not tracked food."));
            return 0;
        }

        long currentDay = FoodExpirySystem.getCurrentDay(player.getWorld());
        FoodExpirySystem.setExpiresInDays(stack, currentDay, days);

        source.sendFeedback(() -> Text.literal("Set held food expiry to " + days + " day" + (days == 1 ? "" : "s") + "."), true);
        return 1;
    }

    private static int expireHeldFood(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            source.sendError(Text.literal("Hold a food item in your main hand."));
            return 0;
        }

        if (!FoodExpirySystem.isTrackedFood(stack.getItem())) {
            source.sendError(Text.literal("Held item is not tracked food."));
            return 0;
        }

        long currentDay = FoodExpirySystem.getCurrentDay(player.getWorld());
        FoodExpirySystem.setExpiresInDays(stack, currentDay, -1);

        source.sendFeedback(() -> Text.literal("Held food is now expired."), true);
        return 1;
    }
}

