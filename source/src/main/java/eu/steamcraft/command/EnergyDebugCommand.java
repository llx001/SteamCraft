package eu.steamcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.steamcraft.SteamCraft;
import eu.steamcraft.block.entity.custom.BatteryBlockEntity;
import eu.steamcraft.block.entity.custom.ElectricTorchBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.base.SimpleEnergyItem;

public final class EnergyDebugCommand {
    private EnergyDebugCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(buildSubcommand());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildSubcommand() {
        return CommandManager.literal("energy")
                .requires(source -> source.hasPermissionLevel(2))

                .executes(ctx -> getEnergy(ctx.getSource()))

                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(ctx -> setHeldItemEnergy(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "amount")))))

                .then(CommandManager.literal("setblock")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(ctx -> setTargetBlockEnergy(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "amount")))));
    }

    private static int getEnergy(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            var held = player.getMainHandStack();
            if (!held.isEmpty() && held.getItem() instanceof SimpleEnergyItem energyItem) {
                long e = energyItem.getStoredEnergy(held);
                source.sendFeedback(() -> Text.literal("§e[Energy]§r Held item: " + e), false);
            }

            BlockEntity be = getTargetBlockEntity(player);
            if (be instanceof ElectricTorchBlockEntity torch) {
                source.sendFeedback(() -> Text.literal("§e[Energy]§r Torch block: "
                        + torch.getEnergy() + " / " + ElectricTorchBlockEntity.MAX_ENERGY), false);
            } else if (be instanceof BatteryBlockEntity battery) {
                source.sendFeedback(() -> Text.literal("§e[Energy]§r Battery block: "
                        + battery.getEnergy() + " / " + battery.getMaxEnergy()), false);
            }
            return 1;
        } catch (Exception e) {
            SteamCraft.LOGGER.error("Energy get failed", e);
            return 0;
        }
    }

    private static int setHeldItemEnergy(ServerCommandSource source, int amount) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            var held = player.getMainHandStack();
            if (held.isEmpty()) {
                source.sendError(Text.literal("Hold an item in your main hand."));
                return 0;
            }
            if (!(held.getItem() instanceof SimpleEnergyItem energyItem)) {
                source.sendError(Text.literal("Held item does not support energy."));
                return 0;
            }

            long clamped = Math.min(amount, energyItem.getEnergyCapacity(held));
            energyItem.setStoredEnergy(held, clamped);
            source.sendFeedback(() -> Text.literal("§e[Energy]§r Set item energy to " + clamped), true);
            return 1;
        } catch (Exception e) {
            SteamCraft.LOGGER.error("Energy set failed", e);
            return 0;
        }
    }

    private static int setTargetBlockEnergy(ServerCommandSource source, int amount) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            BlockEntity be = getTargetBlockEntity(player);
            if (be instanceof ElectricTorchBlockEntity torch) {
                torch.setEnergy(amount);
                source.sendFeedback(() -> Text.literal("§e[Energy]§r Set torch energy to " + amount), true);
                return 1;
            } else if (be instanceof BatteryBlockEntity battery) {
                battery.setEnergy(amount);
                source.sendFeedback(() -> Text.literal("§e[Energy]§r Set battery energy to " + amount), true);
                return 1;
            }
            source.sendError(Text.literal("Look at an electric torch or battery block."));
            return 0;
        } catch (Exception e) {
            SteamCraft.LOGGER.error("Energy setblock failed", e);
            return 0;
        }
    }

    private static BlockEntity getTargetBlockEntity(ServerPlayerEntity player) {
        var hitResult = player.raycast(10.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return null;
        BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
        return player.getServerWorld().getBlockEntity(pos);
    }
}

