package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.air.PlantAirStress;
import eu.steamcraft.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void steamcraft$reactToAir(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.contains(LeavesBlock.PERSISTENT) && state.get(LeavesBlock.PERSISTENT)) {
            return;
        }

        PlantAirStress.Level level = PlantAirStress.getLevel(world, pos);
        if (level != PlantAirStress.Level.SEVERE) {
            return;
        }

        if (random.nextFloat() < PlantAirStress.getSevereLeafDecayChance(world)) {
            Block.dropStack(world, pos, new ItemStack(ModItems.BIOMASS));
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            AirQualitySystem.addToxins(world, pos, 1.0f);
        }
    }
}

