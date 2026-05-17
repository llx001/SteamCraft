package eu.steamcraft.air;

import eu.steamcraft.item.ModItems;
import eu.steamcraft.network.AirHudPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AirQualitySystem {

    private static final float NETHER_PINNED_CO2 = 100f;
    private static final float NETHER_PINNED_TOXINS = 100f;
    private static final float NETHER_PINNED_MICROPLASTICS = 0f;

    private AirQualitySystem() {}

    private static final Map<RegistryKey<World>, Set<Chunk>> ACTIVE_CHUNKS = new ConcurrentHashMap<>();

    /** Fraction of the concentration gap moved across each cell edge per simulation step. */
    private static final float DIFFUSION_RATE = 0.003f;
    /** Tiny global pollutant removal per simulation cycle; tuned lower so pollutants linger longer. */
    private static final float CO2_DECAY_RATE = 0.0003f;
    private static final float TOXIN_DECAY_RATE = 0.00025f;
    private static final float MICRO_DECAY_RATE = 0.00025f;
    /** Heavy-gas pooling transfer per cycle before geometric gating. */
    private static final float CO2_SINK_RATE = 0.02f;
    /** Slow plant absorption per simulation cycle in leaf-rich cells. */
    private static final float LEAF_CO2_ABSORB_RATE = 0.01f;

    /**
     * Chunk-coordinate radius around each player within which chunks are kept active.
     * A radius of 4 covers a 9x9 chunk area (81 chunks max) per player.
     */
    private static final int SIM_RADIUS = 4;

    /**
     * How many simulation cycles (of 8 ticks each) between full entity-breathing scans.
     * 2 = scan every 16 ticks instead of every 8, halving entity query cost.
     */
    private static final int ENTITY_SCAN_INTERVAL = 2;

    // --------------------------------------------------
    // Chunk registration
    // --------------------------------------------------

    public static void registerChunk(ServerWorld world, Chunk chunk) {
        ACTIVE_CHUNKS.computeIfAbsent(world.getRegistryKey(), k -> ConcurrentHashMap.newKeySet()).add(chunk);
    }

    public static void unregisterChunk(ServerWorld world, Chunk chunk) {
        Set<Chunk> chunks = ACTIVE_CHUNKS.get(world.getRegistryKey());
        if (chunks == null) return;
        chunks.remove(chunk);
        if (chunks.isEmpty()) {
            ACTIVE_CHUNKS.remove(world.getRegistryKey());
        }
    }

    public static Iterable<Chunk> getActiveChunks(ServerWorld world) {
        Set<Chunk> chunks = ACTIVE_CHUNKS.get(world.getRegistryKey());
        if (chunks == null || chunks.isEmpty()) {
            return Set.of();
        }
        // Snapshot to avoid iteration issues if chunk load/unload events fire mid-tick.
        return new HashSet<>(chunks);
    }

    // --------------------------------------------------
    // Access helpers
    // --------------------------------------------------

    public static AirQualityData get(ServerWorld world, BlockPos pos) {
        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) return null;
        return chunk.getAttached(AirAttachments.AIR_QUALITY);
    }

    public static AirQualityData get(ServerWorld world, Chunk chunk) {
        return chunk.getAttached(AirAttachments.AIR_QUALITY);
    }

    public static float sampleCellOpennessForDebug(ServerWorld world, BlockPos pos) {
        int cellX = pos.getX() & ~3;
        int cellY = pos.getY() & ~3;
        int cellZ = pos.getZ() & ~3;
        return sampleCellOpenness(world, cellX, cellY, cellZ, new BlockPos.Mutable());
    }

    public static float sampleFloorOpenFractionForDebug(ServerWorld world, BlockPos pos) {
        int cellX = pos.getX() & ~3;
        int cellY = pos.getY() & ~3;
        int cellZ = pos.getZ() & ~3;
        return sampleFloorOpenFraction(world, cellX, cellY, cellZ, new BlockPos.Mutable());
    }

    public static float sinkPermeabilityForDebug(ServerWorld world, BlockPos pos) {
        AirQualityData air = get(world, pos);
        if (air == null) return 0f;
        BlockPos below = pos.down();
        AirQualityData belowAir = get(world, below);
        if (belowAir == null) return 0f;
        int cellX = pos.getX() & ~3;
        int cellY = pos.getY() & ~3;
        int cellZ = pos.getZ() & ~3;
        float hereOpen  = air.getOpenness(cellX, cellY, cellZ);
        float belowOpen = belowAir.getOpenness(below.getX() & ~3, below.getY() & ~3, below.getZ() & ~3);
        float floorOpen = sampleFloorOpenFraction(world, cellX, cellY, cellZ, new BlockPos.Mutable());
        return Math.min(floorOpen, Math.min(hereOpen, belowOpen));
    }

    public static float sampleLeafDensityForDebug(ServerWorld world, BlockPos pos) {
        int cellX = pos.getX() & ~3;
        int cellY = pos.getY() & ~3;
        int cellZ = pos.getZ() & ~3;
        return sampleLeafDensity(world, cellX, cellY, cellZ, new BlockPos.Mutable());
    }

    // --------------------------------------------------
    // High-level operations
    // --------------------------------------------------

    private static boolean isOverworld(World world) {
        return world.getRegistryKey().equals(World.OVERWORLD);
    }

    private static boolean isNether(World world) {
        return world.getRegistryKey().equals(World.NETHER);
    }

    public static void addCO2(ServerWorld world, BlockPos pos, float amount) {
        if (!isOverworld(world)) return;
        AirQualityData air = get(world, pos);
        if (air == null) return;
        air.addCO2(pos.getX(), pos.getY(), pos.getZ(), amount);
    }

    public static void addToxins(ServerWorld world, BlockPos pos, float amount) {
        if (!isOverworld(world)) return;
        AirQualityData air = get(world, pos);
        if (air == null) return;
        air.addToxins(pos.getX(), pos.getY(), pos.getZ(), amount);
    }

    public static void addMicroplastics(ServerWorld world, BlockPos pos, float amount) {
        if (!isOverworld(world)) return;
        AirQualityData air = get(world, pos);
        if (air == null) return;
        air.addMicroplastics(pos.getX(), pos.getY(), pos.getZ(), amount);
    }

    // --------------------------------------------------
    // Main tick
    // --------------------------------------------------

    public static void tick(ServerWorld world) {

        boolean overworld = isOverworld(world);
        boolean nether    = isNether(world);

        long time = world.getTime();

        // ---- Player loop (HUD sync + emissions + effects) ---------------
        for (ServerPlayerEntity player : world.getPlayers()) {

            BlockPos pos = player.getBlockPos();
            AirQualityData air = AirQualitySystem.get(world, pos);

            float co2     = 0f;
            float toxins  = 0f;
            float plastics = 0f;

            if (overworld && air != null) {
                co2     = air.getCO2(pos);
                toxins  = air.getToxins(pos);
                plastics = air.getMicroplastics(pos);
            } else if (nether) {
                co2     = NETHER_PINNED_CO2;
                toxins  = NETHER_PINNED_TOXINS;
                plastics = NETHER_PINNED_MICROPLASTICS;
            }

            // Sync HUD every 5 ticks, staggered by player id.
            if ((time % 5) == (Math.abs(player.getId()) % 5)) {
                float oxygen = Math.max(0f, 100f - co2);
                ServerPlayNetworking.send(player, new AirHudPayload(co2, oxygen, toxins));
            }

            if (player.isCreative() || player.isSpectator()) continue;
            if (!overworld && !nether) continue;
            if (overworld && air == null) continue;

            if (!overworld) {
                applyPlayerEffects(player, co2, toxins, plastics);
                continue;
            }

            // Player breathing emission
            float emission = 0.02f;
            if (!player.isSneaking()) {
                if      (player.isSprinting())                     emission += 0.10f;
                else if (player.isSwimming() || player.isClimbing()) emission += 0.08f;
                else if (player.isUsingItem())                     emission += 0.04f;
            }
            AirQualitySystem.addCO2(world, pos, emission);

            applyPlayerEffects(player, co2, toxins, plastics);
        }

        if (!overworld) return;

        // ---- Chunk simulation (Overworld only) --------------------------

        // Pre-compute near-player chunk set and Y band from player positions.
        Set<Long> nearPlayerChunks = new HashSet<>();
        int simMinY = world.getTopY();
        int simMaxY = world.getBottomY();

        for (ServerPlayerEntity sp : world.getPlayers()) {
            int pcx = sp.getChunkPos().x;
            int pcz = sp.getChunkPos().z;
            for (int dx = -SIM_RADIUS; dx <= SIM_RADIUS; dx++) {
                for (int dz = -SIM_RADIUS; dz <= SIM_RADIUS; dz++) {
                    nearPlayerChunks.add(ChunkPos.toLong(pcx + dx, pcz + dz));
                }
            }
            simMinY = Math.min(simMinY, sp.getBlockY() - 32);
            simMaxY = Math.max(simMaxY, sp.getBlockY() + 64);
        }

        // Clamp to world bounds; fall back to full range if no players online.
        if (simMinY > simMaxY || nearPlayerChunks.isEmpty()) {
            simMinY = world.getBottomY();
            simMaxY = world.getTopY() - 4;
        } else {
            simMinY = Math.max(simMinY, world.getBottomY());
            simMaxY = Math.min(simMaxY, world.getTopY() - 4);
        }

        // Entity-breathing scans every ENTITY_SCAN_INTERVAL simulation cycles.
        // (time >> 3) gives simulation-cycle index; mask by interval.
        boolean doEntityScan = ((time >> 3) % ENTITY_SCAN_INTERVAL) == 0;

        int phase = (int) (time & 7L);
        for (Chunk chunk : AirQualitySystem.getActiveChunks(world)) {

            // Stagger workload: each chunk simulates once every 8 ticks.
            if (((chunk.getPos().x + chunk.getPos().z) & 7) != phase) continue;

            AirQualityData air = AirQualitySystem.get(world, chunk);
            if (air == null) continue;

            // Skip chunks that are neither near a player nor carrying pollution.
            // Polluted chunks must continue to decay/diffuse even when far away.
            long key = chunk.getPos().toLong();
            if (!nearPlayerChunks.contains(key) && !air.hasAnyPollution()) continue;

            simulateChunk(world, chunk, air, time, simMinY, simMaxY, doEntityScan);

            if (!air.isDirty()) continue;

            // Re-attach to mark the chunk dirty for saving.
            chunk.setAttached(AirAttachments.AIR_QUALITY, air);
        }
    }

    private static void applyPlayerEffects(ServerPlayerEntity player, float co2, float toxins, float plastics) {

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (helmet.isOf(ModItems.HAZMAT_ARMOR_HELMET)) return;

        if (player.age % 40 == 0 && co2 > 60f) {
            player.damage(player.getWorld().getDamageSources().magic(), 1f);
        }
        if (toxins > 40f) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,   60, 0, true, false));
        }
        if (plastics > 50f) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 0, true, false));
        }
    }

    // --------------------------------------------------
    // Geometry cache builder (merged one-pass scan)
    // --------------------------------------------------

    /**
     * Single-pass scan of chunk blocks/biomes that builds the static geometry
     * cache inside {@code air}:
     * <ul>
     *   <li>8 corner reads per cell → cell openness + lava/nether-block sources</li>
     *   <li>4 floor reads → floor openness for CO2 sinking</li>
     *   <li>4 leaf reads at cell top → leaf density for CO2 absorption</li>
     *   <li>1 centre read + 1 biome read → spawner toxins + swamp/mangrove baseline</li>
     * </ul>
     * Total: ~18 world reads per cell (vs ~26 in the old separate passes).
     * Called once per chunk every {@link AirQualityData#GEOMETRY_TTL} ticks.
     */
    private static void buildChunkGeometry(ServerWorld world, Chunk chunk, AirQualityData air, long worldTick) {

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        boolean isOW = world.getRegistryKey().equals(World.OVERWORLD);
        BlockPos.Mutable probe = new BlockPos.Mutable();

        air.clearStaticSources();

        for (int y = world.getBottomY(); y < world.getTopY(); y += 4) {
            for (int x = 0; x < 16; x += 4) {
                for (int z = 0; z < 16; z += 4) {

                    int cellX = startX + x;
                    int cellZ = startZ + z;

                    int openCount  = 0;
                    int floorOpen  = 0;
                    float co2Add   = 0f;
                    float toxinAdd = 0f;

                    // --- 8 corners: openness + lava check + nether-block check ---
                    for (int ox = 0; ox <= 2; ox += 2) {
                        for (int oy = 0; oy <= 2; oy += 2) {
                            for (int oz = 0; oz <= 2; oz += 2) {
                                probe.set(cellX + ox, y + oy, cellZ + oz);
                                BlockState bs = world.getBlockState(probe);

                                if (!bs.isSolidBlock(world, probe)) openCount++;

                                if (bs.getFluidState().isIn(FluidTags.LAVA)) {
                                    toxinAdd += 0.08f;
                                }

                                if (isOW) {
                                    Block block = bs.getBlock();
                                    if (block == Blocks.NETHERRACK || block == Blocks.SOUL_SAND
                                            || block == Blocks.MAGMA_BLOCK || block == Blocks.SOUL_SOIL) {
                                        co2Add   += 0.015f;
                                        toxinAdd += 0.030f;
                                    }
                                }
                            }
                        }
                    }
                    air.setOpenness(cellX, y, cellZ, openCount / 8.0f);

                    // --- 4 floor corners (y-1): floor openness for CO2 sinking ---
                    int fy = y - 1;
                    for (int ox = 0; ox <= 2; ox += 2) {
                        for (int oz = 0; oz <= 2; oz += 2) {
                            probe.set(cellX + ox, fy, cellZ + oz);
                            if (!world.getBlockState(probe).isSolidBlock(world, probe)) floorOpen++;
                        }
                    }
                    air.setFloorOpenness(cellX, y, cellZ, floorOpen / 4.0f);

                    // --- 4 leaf corners at cell top (y+3): CO2 absorption ---
                    int leafCount = 0;
                    int ly = y + 3;
                    for (int ox = 1; ox <= 3; ox += 2) {
                        for (int oz = 1; oz <= 3; oz += 2) {
                            probe.set(cellX + ox, ly, cellZ + oz);
                            if (world.getBlockState(probe).isIn(BlockTags.LEAVES)) leafCount++;
                        }
                    }
                    air.setLeafDensity(cellX, y, cellZ, leafCount / 4.0f);

                    // --- 1 centre block: spawner + biome ---
                    probe.set(cellX + 2, y + 2, cellZ + 2);
                    BlockState centre = world.getBlockState(probe);
                    if (centre.isOf(Blocks.SPAWNER)) toxinAdd += 0.6f;

                    var biome = world.getBiome(probe);
                    if (biome.matchesKey(BiomeKeys.SWAMP) || biome.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
                        co2Add   += 0.003f;
                        toxinAdd += 0.0015f;
                    }

                    if (co2Add   > 0f) air.addStaticCO2(cellX, y, cellZ, co2Add);
                    if (toxinAdd > 0f) air.addStaticToxins(cellX, y, cellZ, toxinAdd);
                }
            }
        }

        air.markGeometryBuilt(worldTick);
    }

    // --------------------------------------------------
    // Chunk simulation (optimised)
    // --------------------------------------------------

    /**
     * Simulates one chunk:
     * <ol>
     *   <li>Rebuilds geometry cache if stale (TTL-based, ~every 64 ticks).</li>
     *   <li>Optional entity-breathing scan (throttled by {@link #ENTITY_SCAN_INTERVAL}).</li>
     *   <li>Merged per-cell loop restricted to the player Y-band:
     *       static source emission → leaf absorption → CO2 heavy-gas sink.</li>
     *   <li>Full-grid decay (cheap array multiply).</li>
     *   <li>Full-grid diffusion (sparse: zero-zero cell pairs are skipped).</li>
     * </ol>
     */
    private static void simulateChunk(ServerWorld world, Chunk chunk, AirQualityData air,
                                      long worldTick, int simMinY, int simMaxY, boolean doEntityScan) {

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        // 1. Rebuild static geometry if the TTL has expired.
        if (air.isGeometryStale(worldTick)) {
            buildChunkGeometry(world, chunk, air, worldTick);
        }

        // 2. Entity breathing (dynamic; throttled).
        if (doEntityScan) {
            Box chunkBox = new Box(startX, world.getBottomY(), startZ,
                    startX + 16, world.getTopY(), startZ + 16);
            List<LivingEntity> mobs = world.getEntitiesByClass(LivingEntity.class, chunkBox,
                    e -> !(e instanceof PlayerEntity));
            for (LivingEntity mob : mobs) {
                air.addCO2(mob.getBlockX(), mob.getBlockY(), mob.getBlockZ(), 0.12f);
            }
        }

        // 3. Determine Y loop range: player band ∪ polluted range, clamped to world.
        int loopMinY = simMinY & ~3;
        int loopMaxY = simMaxY & ~3;

        if (air.hasAnyPollution()) {
            int pollBot = air.bottomPollutedY();
            int pollTop = air.topPollutedY();
            if (pollBot != Integer.MAX_VALUE) {
                loopMinY = Math.max(world.getBottomY(),    Math.min(loopMinY, (pollBot - 4) & ~3));
                loopMaxY = Math.min(world.getTopY() - 4,   Math.max(loopMaxY, (pollTop + 4) & ~3));
            }
        }
        loopMinY = Math.max(loopMinY, world.getBottomY());
        loopMaxY = Math.min(loopMaxY, (world.getTopY() - 4) & ~3);

        // 4. Merged per-cell loop: static sources + leaf absorption + CO2 sink.
        for (int y = loopMinY; y <= loopMaxY; y += 4) {
            for (int x = 0; x < 16; x += 4) {
                for (int z = 0; z < 16; z += 4) {

                    int cellX = startX + x;
                    int cellZ = startZ + z;

                    // 4a. Static pollution sources (from cached geometry).
                    float sco2 = air.getStaticCO2(cellX, y, cellZ);
                    float stox = air.getStaticToxins(cellX, y, cellZ);
                    if (sco2 > 0f) air.addCO2(cellX, y, cellZ, sco2);
                    if (stox > 0f) air.addToxins(cellX, y, cellZ, stox);

                    // 4b. Leaf CO2 absorption (cached leaf density).
                    float leafDensity = air.getLeafDensity(cellX, y, cellZ);
                    if (leafDensity > 0f) {
                        float openness = air.getOpenness(cellX, y, cellZ);
                        air.addCO2(cellX, y, cellZ, -LEAF_CO2_ABSORB_RATE * leafDensity * openness);
                    }

                    // 4c. CO2 heavy-gas sink (downward, intra-chunk).
                    if (y > world.getBottomY()) {
                        float co2 = air.getCO2(cellX, y, cellZ);
                        if (co2 > 20f) {
                            float floorOpen = air.getFloorOpenness(cellX, y, cellZ);
                            if (floorOpen > 0f) {
                                int belowY = y - 4;
                                float permeability = Math.min(floorOpen,
                                        Math.min(air.getOpenness(cellX, y, cellZ),
                                                 air.getOpenness(cellX, belowY, cellZ)));
                                if (permeability > 0f) {
                                    float sinkAmt = CO2_SINK_RATE * permeability;
                                    air.addCO2(cellX, y,     cellZ, -sinkAmt);
                                    air.addCO2(cellX, belowY, cellZ,  sinkAmt);
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Decay (full grid; fast array multiply, skips zeroes).
        air.applyDecay(CO2_DECAY_RATE, TOXIN_DECAY_RATE, MICRO_DECAY_RATE);

        // 6. Passive diffusion into adjacent loaded chunks.
        final int cx = chunk.getPos().x;
        final int cz = chunk.getPos().z;
        air.diffuse(DIFFUSION_RATE, (dx, dz) -> {
            Chunk c = world.getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
            return c == null ? null : c.getAttached(AirAttachments.AIR_QUALITY);
        });
    }

    // --------------------------------------------------
    // Private geometry helpers (kept for debug commands)
    // --------------------------------------------------

    private static float sampleCellOpenness(ServerWorld world, int cellStartX, int cellStartY, int cellStartZ, BlockPos.Mutable probe) {
        int open = 0;
        for (int ox = 0; ox <= 2; ox += 2) {
            for (int oy = 0; oy <= 2; oy += 2) {
                for (int oz = 0; oz <= 2; oz += 2) {
                    probe.set(cellStartX + ox, cellStartY + oy, cellStartZ + oz);
                    if (!world.getBlockState(probe).isSolidBlock(world, probe)) open++;
                }
            }
        }
        return open / 8.0f;
    }

    private static float sampleFloorOpenFraction(ServerWorld world, int cellStartX, int cellStartY, int cellStartZ, BlockPos.Mutable probe) {
        int open = 0;
        int y = cellStartY - 1;
        for (int ox = 0; ox <= 2; ox += 2) {
            for (int oz = 0; oz <= 2; oz += 2) {
                probe.set(cellStartX + ox, y, cellStartZ + oz);
                if (!world.getBlockState(probe).isSolidBlock(world, probe)) open++;
            }
        }
        return open / 4.0f;
    }

    private static float sampleLeafDensity(ServerWorld world, int cellStartX, int cellStartY, int cellStartZ, BlockPos.Mutable probe) {
        int leaves = 0;
        int y = cellStartY + 3;
        for (int ox = 1; ox <= 3; ox += 2) {
            for (int oz = 1; oz <= 3; oz += 2) {
                probe.set(cellStartX + ox, y, cellStartZ + oz);
                if (world.getBlockState(probe).isIn(BlockTags.LEAVES)) leaves++;
            }
        }
        return leaves / 4.0f;
    }
}

