package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockMixin {

    @Inject(method = "litServerTick", at = @At("HEAD"), require = 0)
    private static void steamcraft$emitCO2(World world, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!state.contains(Properties.LIT) || !state.get(Properties.LIT)) return;
        AirQualitySystem.addCO2(serverWorld, pos, 0.06f);
    }
}

