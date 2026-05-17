package eu.steamcraft.mixin;

import eu.steamcraft.air.PlantAirStress;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void steamcraft$reactToAir(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!state.isOf(Blocks.GRASS_BLOCK)) {
            return;
        }

        PlantAirStress.Level level = PlantAirStress.getLevel(world, pos);
        if (level == PlantAirStress.Level.HEALTHY) {
            return;
        }

        if (level == PlantAirStress.Level.SEVERE) {
            if (random.nextFloat() < PlantAirStress.getSevereGrassDecayChance(world)) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            ci.cancel();
            return;
        }

        if (level == PlantAirStress.Level.BLOCKED) {
            ci.cancel();
            return;
        }

        if (random.nextFloat() < PlantAirStress.getSlowCancelChance(world)) {
            ci.cancel();
        }
    }
}

