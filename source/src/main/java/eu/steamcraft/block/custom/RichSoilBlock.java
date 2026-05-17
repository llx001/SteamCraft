package eu.steamcraft.block.custom;

import eu.steamcraft.air.PlantAirStress;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class RichSoilBlock extends PillarBlock {

    public RichSoilBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        PlantAirStress.Level level = PlantAirStress.getLevel(world, pos);

        // Severe pollution degrades rich soil back to ordinary dirt.
        if (level == PlantAirStress.Level.SEVERE && random.nextFloat() < 0.15f) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            return;
        }

        // In clean air, spread to a nearby dirt block.
        if (level == PlantAirStress.Level.HEALTHY && random.nextFloat() < 0.30f) {
            BlockPos target = pos.add(
                    random.nextInt(5) - 2,
                    random.nextInt(3) - 1,
                    random.nextInt(5) - 2);
            if (world.getBlockState(target).isOf(Blocks.DIRT)
                    && world.getBlockState(target.up()).isAir()) {
                world.setBlockState(target, state);
            }
        }

        // Boost growth of a fertilizable block (crop, sapling, etc.) directly above.
        BlockPos above = pos.up();
        BlockState aboveState = world.getBlockState(above);
        if (aboveState.getBlock() instanceof Fertilizable fertilizable
                && fertilizable.isFertilizable(world, above, aboveState)
                && fertilizable.canGrow(world, random, above, aboveState)
                && random.nextFloat() < 0.40f) {
            fertilizable.grow(world, random, above, aboveState);
        }
    }
}
