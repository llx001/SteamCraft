package eu.steamcraft.block.entity.custom;

import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AirScrubberBlockEntity extends PoweredMachineBlockEntity {
    public AirScrubberBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.AIR_SCRUBBER_BE,
                pos,
                state,
                EnergyHelper.SCRUBBER_MAX_ENERGY,
                EnergyHelper.SCRUBBER_MAX_ENERGY
        );
    }

    @Override
    public void tickMachine(World world, BlockPos pos, BlockState state) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        if (EnergyHelper.SCRUBBER_INTERVAL_TICKS <= 0 || world.getTime() % EnergyHelper.SCRUBBER_INTERVAL_TICKS != 0) {
            return;
        }
        if (!useEnergy(EnergyHelper.SCRUBBER_ENERGY_PER_CYCLE)) {
            return;
        }

        BlockPos.Mutable probe = new BlockPos.Mutable();
        int step = 4;
        for (int dx = -EnergyHelper.SCRUBBER_RADIUS; dx <= EnergyHelper.SCRUBBER_RADIUS; dx += step) {
            for (int dy = -EnergyHelper.SCRUBBER_RADIUS; dy <= EnergyHelper.SCRUBBER_RADIUS; dy += step) {
                for (int dz = -EnergyHelper.SCRUBBER_RADIUS; dz <= EnergyHelper.SCRUBBER_RADIUS; dz += step) {
                    probe.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    AirQualitySystem.addCO2(serverWorld, probe, -EnergyHelper.SCRUBBER_SCRUB_RATE);
                    AirQualitySystem.addToxins(serverWorld, probe, -EnergyHelper.SCRUBBER_SCRUB_RATE);
                    AirQualitySystem.addMicroplastics(serverWorld, probe, -EnergyHelper.SCRUBBER_SCRUB_RATE);
                }
            }
        }
    }
}

