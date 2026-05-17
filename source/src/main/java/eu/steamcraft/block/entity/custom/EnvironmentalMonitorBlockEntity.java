package eu.steamcraft.block.entity.custom;

import eu.steamcraft.air.AirQualityData;
import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

public class EnvironmentalMonitorBlockEntity extends PoweredMachineBlockEntity {

    /** Square grid edge length, in air cells. */
    public static final int GRID = 8;

    private static final int CELL_SIZE = 4;

    // Mirror the documented air-quality effect thresholds so 255 == "harmful".
    private static final float CO2_THRESHOLD   = 60f;
    private static final float TOXIN_THRESHOLD = 40f;
    private static final float MICRO_THRESHOLD = 50f;

    public EnvironmentalMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.ENVIRONMENTAL_MONITOR_BE,
                pos,
                state,
                EnergyHelper.MONITOR_MAX_ENERGY,
                EnergyHelper.MONITOR_MAX_ENERGY
        );
    }

    public boolean consumeScanEnergy() {
        return useEnergy(EnergyHelper.MONITOR_SCAN_COST);
    }

    @Override
    public void tickMachine(World world, BlockPos pos, BlockState state) {
        long budget = EnergyHelper.MONITOR_CHARGE_RATE;
        for (Direction dir : Direction.values()) {
            if (budget <= 0) {
                break;
            }

            EnergyStorage source = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());
            long moved = EnergyStorageUtil.move(source, getEnergyStorage(), budget, null);
            budget -= moved;
        }
    }

    /**
     * Builds a {@link #GRID}x{@link #GRID} row-major health grid centred on the
     * monitor. Each column is the worst normalised pollutant over a small
     * vertical band around the monitor's Y.
     */
    public static byte[] scan(ServerWorld world, BlockPos origin) {
        byte[] cells = new byte[GRID * GRID];
        int half = GRID / 2;
        BlockPos.Mutable probe = new BlockPos.Mutable();

        for (int gz = 0; gz < GRID; gz++) {
            for (int gx = 0; gx < GRID; gx++) {
                int wx = origin.getX() + (gx - half) * CELL_SIZE;
                int wz = origin.getZ() + (gz - half) * CELL_SIZE;

                float worst = 0f;
                for (int dy = -CELL_SIZE; dy <= CELL_SIZE; dy += CELL_SIZE) {
                    probe.set(wx, origin.getY() + dy, wz);
                    AirQualityData air = AirQualitySystem.get(world, probe);
                    if (air == null) continue;

                    float h = Math.max(
                            air.getCO2(probe) / CO2_THRESHOLD,
                            Math.max(
                                    air.getToxins(probe) / TOXIN_THRESHOLD,
                                    air.getMicroplastics(probe) / MICRO_THRESHOLD
                            )
                    );
                    if (h > worst) worst = h;
                }

                worst = MathHelper.clamp(worst, 0f, 1f);
                cells[gz * GRID + gx] = (byte) Math.round(worst * 255f);
            }
        }

        return cells;
    }
}
