package eu.steamcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.steamcraft.SteamCraft;
import eu.steamcraft.air.AirQualityData;
import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.network.AirVisualizePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class AirDebugCommand {

    private static final int VISUAL_RADIUS_CHUNKS = 5;
    private static final int CELL_SIZE = 4;
    private static final int VISUAL_TTL_TICKS = 20 * 18;
    private static final int MAX_SAMPLES = 5000;

    private enum StatType { CO2, TOXINS, PLASTICS }
    private enum Operation { SET, ADD }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(buildSubcommand());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildSubcommand() {
        var root = CommandManager.literal("air")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(AirDebugCommand::execute)
                .then(CommandManager.literal("debug")
                        .executes(AirDebugCommand::debug))
                .then(CommandManager.literal("visualize")
                        .executes(AirDebugCommand::visualize)
                        .then(CommandManager.literal("clear")
                                .executes(AirDebugCommand::clearVisualize)));

        for (StatType type : StatType.values()) {

            String arg = type.name().toLowerCase();

            root.then(CommandManager.literal("set")
                    .then(CommandManager.literal(arg)
                            .then(CommandManager.argument("value", FloatArgumentType.floatArg(0, 100))
                                    .executes(ctx -> modify(ctx, type, Operation.SET)))));

            root.then(CommandManager.literal("add")
                    .then(CommandManager.literal(arg)
                            .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                                    .executes(ctx -> modify(ctx, type, Operation.ADD)))));
        }

        return root;
    }

    // --------------------------------------------------
    // Modify values
    // --------------------------------------------------

    private static int modify(CommandContext<ServerCommandSource> ctx, StatType type, Operation op) {

        ServerCommandSource source = ctx.getSource();

        try {

            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            ServerWorld world = source.getWorld();
            BlockPos pos = player.getBlockPos();

            float value = FloatArgumentType.getFloat(ctx, "value");

            AirQualityData air = AirQualitySystem.get(world, pos);

            if (air == null) {
                source.sendFeedback(() -> Text.literal("§eNo air data for this chunk."), false);
                return 0;
            }

            switch (type) {

                case CO2 -> {

                    if (op == Operation.SET)
                        air.setCO2(pos, value);
                    else
                        AirQualitySystem.addCO2(world, pos, value);
                }

                case TOXINS -> {

                    float delta = (op == Operation.SET)
                            ? value - air.getToxins(pos)
                            : value;

                    AirQualitySystem.addToxins(world, pos, delta);
                }

                case PLASTICS -> {

                    float delta = (op == Operation.SET)
                            ? value - air.getMicroplastics(pos)
                            : value;

                    AirQualitySystem.addMicroplastics(world, pos, delta);
                }
            }

            source.sendFeedback(() ->
                    Text.literal(String.format(
                            "§b[Air Edit]§r %s %s %.2f @ [%d %d %d]",
                            op, type, value,
                            pos.getX(), pos.getY(), pos.getZ()
                    )), true);

            return 1;

        } catch (Exception e) {

            SteamCraft.LOGGER.error("Air command failed", e);
            return 0;
        }
    }

    // --------------------------------------------------
    // Geometry debug
    // --------------------------------------------------

    private static int debug(CommandContext<ServerCommandSource> ctx) {

        ServerCommandSource source = ctx.getSource();

        try {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            ServerWorld world = source.getWorld();
            BlockPos pos = player.getBlockPos();
            BlockPos cell = new BlockPos(pos.getX() & ~(CELL_SIZE - 1), pos.getY() & ~(CELL_SIZE - 1), pos.getZ() & ~(CELL_SIZE - 1));
            BlockPos below = cell.down();

            AirQualityData air = AirQualitySystem.get(world, cell);
            AirQualityData belowAir = AirQualitySystem.get(world, below);

            float openness = air == null ? 1.0f : air.getOpenness(cell.getX(), cell.getY(), cell.getZ());
            float belowOpenness = belowAir == null ? 1.0f : belowAir.getOpenness(below.getX(), below.getY(), below.getZ());
            float sampledOpen = AirQualitySystem.sampleCellOpennessForDebug(world, cell);
            float floorOpen = AirQualitySystem.sampleFloorOpenFractionForDebug(world, cell);
            float sinkPermeability = AirQualitySystem.sinkPermeabilityForDebug(world, cell);
            float leafDensity = AirQualitySystem.sampleLeafDensityForDebug(world, cell);

            String report = String.format(
                    "§b[Air Debug]§r cell[%d %d %d]\n" +
                            "  openness(stored): %.2f\n" +
                            "  openness(sampled): %.2f\n" +
                            "  leaf density(sampled): %.2f\n" +
                            "  below openness(stored): %.2f\n" +
                            "  floor open: %.2f\n" +
                            "  sink permeability: %.2f",
                    cell.getX(), cell.getY(), cell.getZ(),
                    openness, sampledOpen, leafDensity, belowOpenness, floorOpen, sinkPermeability
            );

            source.sendFeedback(() -> Text.literal(report), false);
            return 1;

        } catch (Exception e) {
            SteamCraft.LOGGER.error("Air debug failed", e);
            return 0;
        }
    }

    // --------------------------------------------------
    // Visualization
    // --------------------------------------------------

    private static int visualize(CommandContext<ServerCommandSource> ctx) {

        ServerCommandSource source = ctx.getSource();

        try {

            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            ServerWorld world = source.getWorld();

            BlockPos center = player.getBlockPos();

            int subRadius = (VISUAL_RADIUS_CHUNKS * 16) / CELL_SIZE;

            List<AirVisualizePayload.CellSample> samples = new ArrayList<>();

            for (int x = -subRadius; x <= subRadius; x++) {
                for (int y = -subRadius; y <= subRadius; y++) {
                    for (int z = -subRadius; z <= subRadius; z++) {

                        BlockPos p = center.add(
                                x * CELL_SIZE,
                                y * CELL_SIZE,
                                z * CELL_SIZE
                        );

                        AirQualityData air = AirQualitySystem.get(world, p);
                        if (air == null) continue;

                        float co2 = air.getCO2(p);
                        float toxins = air.getToxins(p);
                        float plastics = air.getMicroplastics(p);

                        if (co2 > 3f || toxins > 1f || plastics > 1f) {
                            int cellX = p.getX() & ~(CELL_SIZE - 1);
                            int cellY = p.getY() & ~(CELL_SIZE - 1);
                            int cellZ = p.getZ() & ~(CELL_SIZE - 1);

                            samples.add(new AirVisualizePayload.CellSample(cellX, cellY, cellZ, co2, toxins, plastics));
                            if (samples.size() >= MAX_SAMPLES) {
                                break;
                            }
                        }
                    }
                    if (samples.size() >= MAX_SAMPLES) {
                        break;
                    }
                }
                if (samples.size() >= MAX_SAMPLES) {
                    break;
                }
            }

            sendVisualizePacket(player, samples, VISUAL_TTL_TICKS);

            source.sendFeedback(() ->
                            Text.literal("§b[Air Vis]§r Wireframe cells sent: " + samples.size()),
                    true
            );

            return 1;

        } catch (Exception e) {

            SteamCraft.LOGGER.error("Air visualize failed", e);
            return 0;
        }
    }

    private static int clearVisualize(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            sendVisualizePacket(player, List.of(), 0);
            source.sendFeedback(() -> Text.literal("§b[Air Vis]§r Cleared client visualization."), false);
            return 1;
        } catch (Exception e) {
            SteamCraft.LOGGER.error("Air visualize clear failed", e);
            return 0;
        }
    }

    private static void sendVisualizePacket(ServerPlayerEntity player, List<AirVisualizePayload.CellSample> samples, int ttlTicks) {
        ServerPlayNetworking.send(player, new AirVisualizePayload(ttlTicks, samples));
    }

    // --------------------------------------------------
    // Scan command
    // --------------------------------------------------

    private static int execute(CommandContext<ServerCommandSource> ctx) {

        ServerCommandSource source = ctx.getSource();

        try {

            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            ServerWorld world = source.getWorld();
            BlockPos pos = player.getBlockPos();

            AirQualityData air = AirQualitySystem.get(world, pos);

            if (air == null) {

                source.sendFeedback(() ->
                                Text.literal("§eNo air quality data attached to this chunk yet."),
                        false);

                return 0;
            }

            float o2 = air.getO2(pos);
            float co2 = air.getCO2(pos);
            float plastics = air.getMicroplastics(pos);
            float toxins = air.getToxins(pos);

            String report = String.format(
                    "§b[Air Scan]§r @ [%d %d %d]\n" +
                            "  §aO₂: %.2f%%\n" +
                            "  §cCO₂: %.2f%%\n" +
                            "  §6Plastics: %.2f\n" +
                            "  §5Toxins: %.2f",
                    pos.getX(), pos.getY(), pos.getZ(),
                    o2, co2, plastics, toxins
            );

            source.sendFeedback(() -> Text.literal(report), false);

            return 1;

        } catch (Exception e) {

            SteamCraft.LOGGER.error("Air scan failed", e);

            source.sendFeedback(() ->
                            Text.literal("§4Internal system error."),
                    false);

            return 0;
        }
    }
}