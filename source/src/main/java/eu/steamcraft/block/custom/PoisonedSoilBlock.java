package eu.steamcraft.block.custom;

import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.air.PlantAirStress;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class PoisonedSoilBlock extends PillarBlock {

    public PoisonedSoilBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient
                && entity instanceof LivingEntity living
                && !(entity instanceof PlayerEntity player && player.isCreative())) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, true, false));
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Always leach a pulse of toxins into local air.
        AirQualitySystem.addToxins(world, pos, 1.5f);

        PlantAirStress.Level level = PlantAirStress.getLevel(world, pos);

        // In clean air, poisoned soil slowly heals back to ordinary dirt.
        if (level == PlantAirStress.Level.HEALTHY && random.nextFloat() < 0.10f) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            return;
        }

        // In polluted air, spread to adjacent dirt or grass.
        if (level != PlantAirStress.Level.HEALTHY && random.nextFloat() < 0.20f) {
            BlockPos target = pos.add(
                    random.nextInt(5) - 2,
                    random.nextInt(3) - 1,
                    random.nextInt(5) - 2);
            BlockState targetState = world.getBlockState(target);
            if ((targetState.isOf(Blocks.DIRT) || targetState.isOf(Blocks.GRASS_BLOCK))
                    && world.getBlockState(target.up()).isAir()) {
                world.setBlockState(target, state);
            }
        }

        // Kill any plant growing directly above.
        BlockPos above = pos.up();
        if (world.getBlockState(above).getBlock() instanceof PlantBlock
                && random.nextFloat() < 0.25f) {
            world.breakBlock(above, false);
        }

        // In polluted air, spawn a zombie above the block in darkness.
        if (level != PlantAirStress.Level.HEALTHY
                && random.nextFloat() < 0.08f
                && world.getBlockState(above).isAir()
                && world.getLightLevel(LightType.BLOCK, above) < 8) {
            ZombieEntity zombie = new ZombieEntity(world);
            zombie.refreshPositionAndAngles(
                    above.getX() + 0.5, above.getY(), above.getZ() + 0.5,
                    random.nextFloat() * 360f, 0f);
            zombie.initialize(world, world.getLocalDifficulty(above), SpawnReason.NATURAL, null);
            world.spawnEntity(zombie);
        }
    }
}
