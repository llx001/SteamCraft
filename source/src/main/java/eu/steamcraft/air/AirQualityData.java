package eu.steamcraft.air;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public final class AirQualityData {

    // --------------------------------------------------
    // Grid configuration
    // --------------------------------------------------

    private static final int CELL_SIZE_BITS = 2; // 2^2 = 4
    private static final int CELL_SIZE = 1 << CELL_SIZE_BITS; // 4

    private static final int CHUNK_WIDTH = 16;
    private static final int CELLS_HORIZONTAL = CHUNK_WIDTH >> CELL_SIZE_BITS; // 4
    private static final int LAYER_SIZE = CELLS_HORIZONTAL * CELLS_HORIZONTAL; // 16

    // --------------------------------------------------
    // Channels
    // --------------------------------------------------

    public static final int CH_CO2 = 0;
    public static final int CH_TOXIN = 1;
    public static final int CH_MICRO = 2;
    private static final int CHANNELS = 3;

    // --------------------------------------------------
    // Caps
    // --------------------------------------------------

    private static final float CO2_CAP = 100f;
    private static final float TOXIN_CAP = 100f;
    private static final float MICRO_CAP = 100f;

    private static final float EPSILON = 1.0e-4f;

    // --------------------------------------------------
    // Default world range
    // --------------------------------------------------

    private static final int DEFAULT_MIN_Y = -64;
    private static final int DEFAULT_HEIGHT = 384;

    // --------------------------------------------------
    // Codec
    //
    // Each pollutant channel is a float grid. To keep the chunk attachment
    // compact (a packed IntArray NBT tag) and lossless, floats are persisted
    // via their raw bit pattern through INT_STREAM. Channels are optional so
    // chunks saved before this format (or with a missing channel) load as an
    // empty grid instead of failing to deserialize.
    // --------------------------------------------------

    public static final Codec<AirQualityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min_y").orElse(DEFAULT_MIN_Y).forGetter(d -> d.minY),
            Codec.INT.fieldOf("height").orElse(DEFAULT_HEIGHT).forGetter(d -> d.height),
            Codec.INT_STREAM.optionalFieldOf("co2").forGetter(d -> Optional.of(IntStream.of(toBits(d.cells[CH_CO2])))),
            Codec.INT_STREAM.optionalFieldOf("toxins").forGetter(d -> Optional.of(IntStream.of(toBits(d.cells[CH_TOXIN])))),
            Codec.INT_STREAM.optionalFieldOf("micro").forGetter(d -> Optional.of(IntStream.of(toBits(d.cells[CH_MICRO]))))
    ).apply(instance, AirQualityData::fromCodec));

    // --------------------------------------------------
    // State
    // --------------------------------------------------

    private final int minY;
    private final int height;
    private final int verticalCells;
    private final int cellCount;

    /** [channel][cellIndex] */
    private final float[][] cells;

    /**
     * Per-cell openness in [0, 1] = fraction of non-solid blocks. Derived from
     * world geometry, not persisted (recomputed during simulation). Defaults to
     * fully open so an un-sampled chunk behaves like the old air-only model.
     */
    private final float[] openness;

    // --------------------------------------------------
    // Transient geometry cache (not persisted; rebuilt from world state)
    // --------------------------------------------------

    /** Cached fraction of non-solid blocks in the floor directly below each cell (for CO2 sinking). */
    private float[] floorOpenness;

    /** Cached leaf density sampled at the top of each cell (for CO2 absorption). */
    private float[] leafDensity;

    /** Per-cell CO2 emission rate per simulation step from static sources (lava, nether blocks, biome, spawner). */
    private float[] staticCO2;

    /** Per-cell toxin emission rate per simulation step from static sources. */
    private float[] staticToxins;

    /**
     * World tick when geometry was last fully rebuilt. Negative = never built,
     * which always triggers a rebuild on first simulation.
     */
    private long geometryTick = -1L;

    /** How many ticks a geometry snapshot stays valid before a rebuild is triggered (~3 s). */
    public static final long GEOMETRY_TTL = 64L;

    private boolean dirty = false;

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------

    public AirQualityData() {
        this(DEFAULT_MIN_Y, DEFAULT_HEIGHT);
    }

    public AirQualityData(int minY, int height) {

        this.minY = minY;
        this.height = height;

        this.verticalCells = (height + CELL_SIZE - 1) / CELL_SIZE;
        this.cellCount = LAYER_SIZE * verticalCells;

        this.cells = new float[CHANNELS][cellCount];
        this.openness = new float[cellCount];
        Arrays.fill(this.openness, 1.0f);

        this.floorOpenness = new float[cellCount];
        Arrays.fill(this.floorOpenness, 1.0f);
        this.leafDensity  = new float[cellCount];
        this.staticCO2    = new float[cellCount];
        this.staticToxins = new float[cellCount];
    }

    private static AirQualityData fromCodec(int minY, int height,
                                            Optional<IntStream> co2Bits,
                                            Optional<IntStream> toxinBits,
                                            Optional<IntStream> microBits) {

        AirQualityData d = new AirQualityData(minY, height);

        d.loadChannel(CH_CO2, co2Bits, CO2_CAP);
        d.loadChannel(CH_TOXIN, toxinBits, TOXIN_CAP);
        d.loadChannel(CH_MICRO, microBits, MICRO_CAP);

        return d;
    }

    // --------------------------------------------------
    // Codec helpers
    // --------------------------------------------------

    private static int[] toBits(float[] src) {

        int[] out = new int[src.length];

        for (int i = 0; i < src.length; i++) {
            out[i] = Float.floatToRawIntBits(src[i]);
        }

        return out;
    }

    private void loadChannel(int ch, Optional<IntStream> src, float cap) {

        if (src.isEmpty()) return; // missing channel -> stays zero

        int[] bits = src.get().toArray();
        float[] dst = cells[ch];

        int n = Math.min(bits.length, dst.length);

        for (int i = 0; i < n; i++) {
            float v = Float.intBitsToFloat(bits[i]);
            dst[i] = Float.isNaN(v) ? 0f : MathHelper.clamp(v, 0f, cap);
        }

        // Grid resized since this was saved (e.g. world height change) -> the
        // copied/truncated grid no longer matches disk, so flag for re-save.
        if (bits.length != dst.length) {
            this.dirty = true;
        }
    }

    private static float cap(int ch) {
        return switch (ch) {
            case CH_CO2 -> CO2_CAP;
            case CH_TOXIN -> TOXIN_CAP;
            default -> MICRO_CAP;
        };
    }

    // --------------------------------------------------
    // Index helpers
    // --------------------------------------------------

    private int getIndex(int x, int y, int z) {

        int localX = x & 15;
        int localZ = z & 15;

        int cx = localX >> CELL_SIZE_BITS;
        int cz = localZ >> CELL_SIZE_BITS;

        int relY = y - minY;

        int cy = MathHelper.clamp(relY >> CELL_SIZE_BITS, 0, verticalCells - 1);

        return cy * LAYER_SIZE + cz * CELLS_HORIZONTAL + cx;
    }

    // --------------------------------------------------
    // Neighbor helpers (intra-chunk; -1 means chunk boundary)
    // --------------------------------------------------

    private int neighborX(int i) {
        if ((i & (CELLS_HORIZONTAL - 1)) == CELLS_HORIZONTAL - 1) return -1;
        return i + 1;
    }

    private int neighborNegX(int i) {
        if ((i & (CELLS_HORIZONTAL - 1)) == 0) return -1;
        return i - 1;
    }

    private int neighborZ(int i) {
        if ((i & (LAYER_SIZE - 1)) >= LAYER_SIZE - CELLS_HORIZONTAL) return -1;
        return i + CELLS_HORIZONTAL;
    }

    private int neighborNegZ(int i) {
        if ((i & (LAYER_SIZE - 1)) < CELLS_HORIZONTAL) return -1;
        return i - CELLS_HORIZONTAL;
    }

    private int neighborUp(int i) {
        if (i >= cellCount - LAYER_SIZE) return -1;
        return i + LAYER_SIZE;
    }

    private int neighborDown(int i) {
        if (i < LAYER_SIZE) return -1;
        return i - LAYER_SIZE;
    }

    // --------------------------------------------------
    // Getters
    // --------------------------------------------------

    public float getCO2(BlockPos pos) {
        return getCO2(pos.getX(), pos.getY(), pos.getZ());
    }

    public float getCO2(int x, int y, int z) {
        return cells[CH_CO2][getIndex(x, y, z)];
    }

    public float getCO2(int idx) {
        return cells[CH_CO2][idx];
    }

    public float getO2(BlockPos pos) {
        return getO2(pos.getX(), pos.getY(), pos.getZ());
    }

    public float getO2(int x, int y, int z) {
        return 100f - getCO2(x, y, z);
    }

    public float getToxins(BlockPos pos) {
        return getToxins(pos.getX(), pos.getY(), pos.getZ());
    }

    public float getToxins(int x, int y, int z) {
        return cells[CH_TOXIN][getIndex(x, y, z)];
    }

    public float getMicroplastics(BlockPos pos) {
        return getMicroplastics(pos.getX(), pos.getY(), pos.getZ());
    }

    public float getMicroplastics(int x, int y, int z) {
        return cells[CH_MICRO][getIndex(x, y, z)];
    }

    public float getOpenness(int x, int y, int z) {
        return openness[getIndex(x, y, z)];
    }

    public void setOpenness(int x, int y, int z, float value) {
        openness[getIndex(x, y, z)] = MathHelper.clamp(value, 0f, 1f);
    }

    // --------------------------------------------------
    // Setters
    // --------------------------------------------------

    private void setIndex(int ch, int idx, float val) {

        float clamped = MathHelper.clamp(val, 0f, cap(ch));

        if (cells[ch][idx] != clamped) {
            cells[ch][idx] = clamped;
            dirty = true;
        }
    }

    public void setCO2(int x, int y, int z, float val) {
        setIndex(CH_CO2, getIndex(x, y, z), val);
    }

    public void setCO2(BlockPos pos, float val) {
        setCO2(pos.getX(), pos.getY(), pos.getZ(), val);
    }

    // --------------------------------------------------
    // Emission + conservative overflow cascade
    // --------------------------------------------------

    public void addCO2(BlockPos pos, float delta) {
        addCO2(pos.getX(), pos.getY(), pos.getZ(), delta);
    }

    public void addCO2(int x, int y, int z, float delta) {
        add(CH_CO2, getIndex(x, y, z), delta);
    }

    public void addToxins(int x, int y, int z, float delta) {
        add(CH_TOXIN, getIndex(x, y, z), delta);
    }

    public void addMicroplastics(int x, int y, int z, float delta) {
        add(CH_MICRO, getIndex(x, y, z), delta);
    }

    private void add(int ch, int idx, float delta) {

        float[] arr = cells[ch];
        float cap = cap(ch);

        float next = arr[idx] + delta;

        // Negative deltas are intentional sinks (decay/environmental removal);
        // mass past 0 is meant to leave the simulation, so just floor at 0.
        if (next <= 0f) {
            if (arr[idx] != 0f) {
                arr[idx] = 0f;
                dirty = true;
            }
            return;
        }

        if (next <= cap) {
            arr[idx] = next;
            dirty = true;
            return;
        }

        arr[idx] = cap;
        dirty = true;
        cascadeOverflow(ch, idx, next - cap);
    }

    /**
     * Pushes {@code overflow} outward from {@code startIdx} ring by ring,
     * filling each cell up to its cap proportionally to free capacity. Mass is
     * conserved within the chunk; chunk-boundary edges are reflective (the
     * continuous diffusion pass carries border excess into neighbour chunks).
     * Only a fully saturated reachable region clips the remainder.
     */
    private void cascadeOverflow(int ch, int startIdx, float overflow) {

        float[] arr = cells[ch];
        float cap = cap(ch);

        boolean[] visited = new boolean[cellCount];
        visited[startIdx] = true;

        int[] frontier = new int[cellCount];
        int[] nextFrontier = new int[cellCount];

        int frontierSize = collectNeighbors(startIdx, frontier, visited, openness);

        while (overflow > EPSILON && frontierSize > 0) {

            float totalFree = 0f;
            float totalWeight = 0f;
            for (int k = 0; k < frontierSize; k++) {
                int cell = frontier[k];
                float free = cap - arr[cell];
                if (free <= 0f) continue;
                totalFree += free;
                float weight = free * openness[cell];
                if (weight > 0f) totalWeight += weight;
            }

            if (totalFree > 0f && totalWeight > 0f) {

                float give = Math.min(overflow, totalFree);
                float givenActual = 0f;

                for (int k = 0; k < frontierSize; k++) {
                    int c = frontier[k];
                    float free = cap - arr[c];
                    if (free <= 0f) continue;
                    float weight = free * openness[c];
                    if (weight <= 0f) continue;
                    float add = Math.min(give * (weight / totalWeight), free);
                    arr[c] += add;
                    givenActual += add;
                }

                overflow -= givenActual;
            }

            // Expand to the next ring.
            int nextSize = 0;
            for (int k = 0; k < frontierSize; k++) {
                nextSize = collectNeighborsInto(frontier[k], nextFrontier, nextSize, visited, openness);
            }

            int[] swap = frontier;
            frontier = nextFrontier;
            nextFrontier = swap;
            frontierSize = nextSize;
        }

        // overflow remaining here means the whole reachable region is saturated
        // -> accepted clip (per design choice).
        dirty = true;
    }

    private int collectNeighbors(int i, int[] out, boolean[] visited, float[] openness) {
        return collectNeighborsInto(i, out, 0, visited, openness);
    }

    private int collectNeighborsInto(int i, int[] out, int size, boolean[] visited, float[] openness) {
        size = pushNeighbor(i, neighborX(i), out, size, visited, openness);
        size = pushNeighbor(i, neighborNegX(i), out, size, visited, openness);
        size = pushNeighbor(i, neighborZ(i), out, size, visited, openness);
        size = pushNeighbor(i, neighborNegZ(i), out, size, visited, openness);
        size = pushNeighbor(i, neighborUp(i), out, size, visited, openness);
        size = pushNeighbor(i, neighborDown(i), out, size, visited, openness);
        return size;
    }

    private int pushNeighbor(int from, int n, int[] out, int size, boolean[] visited, float[] openness) {
        if (n == -1 || visited[n]) return size;
        if (Math.min(openness[from], openness[n]) <= EPSILON) return size;
        visited[n] = true;
        out[size] = n;
        return size + 1;
    }

    // --------------------------------------------------
    // Passive diffusion (conservative; cross-chunk capable)
    // --------------------------------------------------

    /** Supplies the {@link AirQualityData} of a chunk offset by (dx, dz) chunks, or null if not loaded. */
    public interface NeighborChunks {
        AirQualityData get(int dx, int dz);
    }

    /**
     * Equalises concentrations between adjacent cells using a conservative
     * pairwise flux. Each undirected edge is visited exactly once (only the
     * +X/+Z/+Y directions), so for every transfer {@code a + b} is invariant.
     * Horizontal edges on a chunk face exchange with the adjacent loaded chunk;
     * if it is absent the edge is skipped (zero-flux = reflective = no loss).
     *
     * @param rate fraction of the concentration gap moved per step; keep &lt; 0.5 for stability.
     */
    public void diffuse(float rate, NeighborChunks neighbors) {

        AirQualityData east = null;
        AirQualityData south = null;
        boolean eastResolved = false;
        boolean southResolved = false;

        for (int i = 0; i < cellCount; i++) {

            int layerIdx = i & (LAYER_SIZE - 1);
            int cx = layerIdx & (CELLS_HORIZONTAL - 1);
            int cz = layerIdx >> CELL_SIZE_BITS;

            // +X
            if (cx < CELLS_HORIZONTAL - 1) {
                transfer(this, i, this, i + 1, rate);
            } else {
                if (!eastResolved) { east = neighbors.get(1, 0); eastResolved = true; }
                if (compatible(east)) {
                    transfer(this, i, east, i - (CELLS_HORIZONTAL - 1), rate);
                }
            }

            // +Z
            if (cz < CELLS_HORIZONTAL - 1) {
                transfer(this, i, this, i + CELLS_HORIZONTAL, rate);
            } else {
                if (!southResolved) { south = neighbors.get(0, 1); southResolved = true; }
                if (compatible(south)) {
                    transfer(this, i, south, i - (CELLS_HORIZONTAL - 1) * CELLS_HORIZONTAL, rate);
                }
            }

            // +Y (always intra-chunk; top face is reflective)
            if (i < cellCount - LAYER_SIZE) {
                transfer(this, i, this, i + LAYER_SIZE, rate);
            }
        }
    }

    private boolean compatible(AirQualityData other) {
        return other != null && other != this && other.cellCount == this.cellCount;
    }

    private static void transfer(AirQualityData src, int i, AirQualityData tgt, int j, float rate) {

        float permeability = Math.min(src.openness[i], tgt.openness[j]);
        if (permeability <= EPSILON) {
            return;
        }

        // Skip cell pairs where both cells are near-zero across all channels.
        // This avoids floating-point work on the vast majority of empty cells
        // in a clean chunk (sparse optimisation for Phase 2).
        boolean hasContent = false;
        for (int ch = 0; ch < CHANNELS; ch++) {
            if (src.cells[ch][i] > EPSILON || tgt.cells[ch][j] > EPSILON) {
                hasContent = true;
                break;
            }
        }
        if (!hasContent) return;

        boolean moved = false;

        for (int ch = 0; ch < CHANNELS; ch++) {
            float a = src.cells[ch][i];
            float b = tgt.cells[ch][j];
            float flux = rate * permeability * (a - b);
            if (flux == 0f) continue;
            src.cells[ch][i] = a - flux;
            tgt.cells[ch][j] = b + flux;
            moved = true;
        }

        if (moved) {
            src.dirty = true;
            tgt.dirty = true;
        }
    }

    public void applyDecay(float fractionPerCycle) {
        applyDecay(fractionPerCycle, fractionPerCycle, fractionPerCycle);
    }

    public void applyDecay(float co2FractionPerCycle, float toxinFractionPerCycle, float microFractionPerCycle) {

        float[] rates = {
                MathHelper.clamp(co2FractionPerCycle, 0f, 1f),
                MathHelper.clamp(toxinFractionPerCycle, 0f, 1f),
                MathHelper.clamp(microFractionPerCycle, 0f, 1f)
        };

        boolean changed = false;

        for (int ch = 0; ch < CHANNELS; ch++) {
            float keep = 1f - rates[ch];
            if (keep >= 1f) continue;

            float[] channel = cells[ch];
            for (int i = 0; i < cellCount; i++) {
                float before = channel[i];
                if (before <= 0f) continue;
                float after = before * keep;
                if (after < EPSILON) after = 0f;
                if (after != before) {
                    channel[i] = after;
                    changed = true;
                }
            }
        }

        if (changed) {
            dirty = true;
        }
    }

    // --------------------------------------------------
    // Geometry cache API
    // --------------------------------------------------

    public int getMinY()    { return minY; }
    public int getCellSize(){ return CELL_SIZE; }

    /** Returns true if the cached geometry is absent or older than {@link #GEOMETRY_TTL} ticks. */
    public boolean isGeometryStale(long worldTick) {
        return geometryTick < 0L || worldTick - geometryTick >= GEOMETRY_TTL;
    }

    public void markGeometryBuilt(long worldTick) {
        this.geometryTick = worldTick;
    }

    // floor openness
    public void setFloorOpenness(int x, int y, int z, float v) {
        floorOpenness[getIndex(x, y, z)] = MathHelper.clamp(v, 0f, 1f);
    }
    public float getFloorOpenness(int x, int y, int z) {
        return floorOpenness[getIndex(x, y, z)];
    }

    // leaf density
    public void setLeafDensity(int x, int y, int z, float v) {
        leafDensity[getIndex(x, y, z)] = MathHelper.clamp(v, 0f, 1f);
    }
    public float getLeafDensity(int x, int y, int z) {
        return leafDensity[getIndex(x, y, z)];
    }

    // static pollution sources
    public void clearStaticSources() {
        Arrays.fill(staticCO2,    0f);
        Arrays.fill(staticToxins, 0f);
    }

    public void addStaticCO2(int x, int y, int z, float rate) {
        int idx = getIndex(x, y, z);
        staticCO2[idx] = Math.min(staticCO2[idx] + rate, CO2_CAP);
    }

    public void addStaticToxins(int x, int y, int z, float rate) {
        int idx = getIndex(x, y, z);
        staticToxins[idx] = Math.min(staticToxins[idx] + rate, TOXIN_CAP);
    }

    public float getStaticCO2(int x, int y, int z) {
        return staticCO2[getIndex(x, y, z)];
    }

    public float getStaticToxins(int x, int y, int z) {
        return staticToxins[getIndex(x, y, z)];
    }

    // --------------------------------------------------
    // Pollution range queries
    // --------------------------------------------------

    /** Returns {@code true} if any cell contains non-negligible pollution. Short-circuits. */
    public boolean hasAnyPollution() {
        for (int ch = 0; ch < CHANNELS; ch++) {
            for (float v : cells[ch]) {
                if (v > EPSILON) return true;
            }
        }
        return false;
    }

    /**
     * Lowest world-Y of any polluted cell, or {@link Integer#MAX_VALUE} if the chunk is clean.
     * Used for Y-range pruning in the simulation loop.
     */
    public int bottomPollutedY() {
        for (int cy = 0; cy < verticalCells; cy++) {
            int base = cy * LAYER_SIZE;
            for (int ch = 0; ch < CHANNELS; ch++) {
                for (int j = 0; j < LAYER_SIZE; j++) {
                    if (cells[ch][base + j] > EPSILON) return minY + cy * CELL_SIZE;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Highest world-Y of any polluted cell, or {@link Integer#MIN_VALUE} if the chunk is clean.
     */
    public int topPollutedY() {
        for (int cy = verticalCells - 1; cy >= 0; cy--) {
            int base = cy * LAYER_SIZE;
            for (int ch = 0; ch < CHANNELS; ch++) {
                for (int j = 0; j < LAYER_SIZE; j++) {
                    if (cells[ch][base + j] > EPSILON) return minY + cy * CELL_SIZE;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    // --------------------------------------------------
    // State
    // --------------------------------------------------

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
