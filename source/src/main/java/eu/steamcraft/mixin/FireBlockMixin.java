package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), require = 0)
    private void steamcraft$emitCO2(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        AirQualitySystem.addCO2(world, pos, 0.8f);
    }
}

