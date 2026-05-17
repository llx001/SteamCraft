package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandMixin {

    /**
     * Emit toxins when a brew completes (injected just before the private craft() call).
     * require = 0 so a mapping mismatch won't crash the game.
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"
            ),
            require = 0
    )
    private static void steamcraft$onBrewComplete(
            World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity,
            CallbackInfo ci) {
        if (!(world instanceof ServerWorld sw)) return;
        AirQualitySystem.addToxins(sw, pos, 4.0f);
    }
}

