package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualityData;
import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.block.ModBlocks;
import net.minecraft.block.AbstractTorchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTorchBlock.class)
public class TorchBlockMixin {
    private static final float CO2_EXTINGUISH = 60f;

    @Inject(
            method = "getStateForNeighborUpdate",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void steamcraft$extinguishVanillaTorchInBadAir(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            WorldAccess world,
            BlockPos pos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        // Only affect classic vanilla torches (floor + wall).
        if (!state.isOf(Blocks.TORCH) && !state.isOf(Blocks.WALL_TORCH)) {
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        AirQualityData air = AirQualitySystem.get(serverWorld, pos);
        if (air == null) {
            return;
        }

        if (air.getCO2(pos) <= CO2_EXTINGUISH) {
            return;
        }

        serverWorld.playSound(
                null,
                pos,
                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.6f,
                1.7f + (serverWorld.random.nextFloat() * 0.2f)
        );
        if (state.isOf(Blocks.WALL_TORCH) && state.contains(Properties.HORIZONTAL_FACING)) {
            cir.setReturnValue(ModBlocks.UNLIT_WALL_TORCH.getDefaultState()
                    .with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING)));
            return;
        }

        cir.setReturnValue(ModBlocks.UNLIT_TORCH.getDefaultState());
    }
}
