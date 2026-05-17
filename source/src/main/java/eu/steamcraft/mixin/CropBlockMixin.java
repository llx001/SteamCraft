package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.air.PlantAirStress;
import eu.steamcraft.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void steamcraft$reactToAir(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        PlantAirStress.Level level = PlantAirStress.getLevel(world, pos);

        if (level == PlantAirStress.Level.HEALTHY) {
            return;
        }

        if (level == PlantAirStress.Level.SEVERE) {
            int age = state.get(CropBlock.AGE);
            if (age > 0) {
                world.setBlockState(pos, state.with(CropBlock.AGE, age - 1), Block.NOTIFY_LISTENERS);
            } else if (random.nextFloat() < PlantAirStress.getSevereCropDeathChance(world)) {
                Block.dropStack(world, pos, new ItemStack(ModItems.BIOMASS));
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                AirQualitySystem.addCO2(world, pos, 1.5f);
                AirQualitySystem.addToxins(world, pos, 0.8f);
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

