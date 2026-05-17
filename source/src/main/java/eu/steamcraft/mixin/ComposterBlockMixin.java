package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    // 1.20.x path: onUse(BlockState, World, BlockPos, PlayerEntity, BlockHitResult) -> ActionResult
    @Inject(method = "onUse", at = @At("RETURN"), require = 0)
    private void steamcraft$onCompostOld(BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, BlockHitResult hit,
                                         CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue() != ActionResult.SUCCESS) return;
        if (!(world instanceof ServerWorld sw)) return;
        AirQualitySystem.addCO2(sw, pos, 1.2f);
    }

    // 1.21+ path: onUseWithItem(ItemStack, BlockState, World, BlockPos, PlayerEntity, Hand, BlockHitResult)
    @Inject(method = "onUseWithItem", at = @At("RETURN"), require = 0)
    private void steamcraft$onCompostNew(ItemStack stack, BlockState state, World world,
                                         BlockPos pos, PlayerEntity player, Hand hand,
                                         BlockHitResult hit,
                                         CallbackInfoReturnable<?> cir) {
        if (!(world instanceof ServerWorld sw)) return;
        // ItemActionResult.SUCCESS ordinal 0 — compare by name to stay compatible across MC versions
        Object result = cir.getReturnValue();
        if (result == null) return;
        String name = result.toString();
        if (!name.equals("SUCCESS") && !name.contains("SUCCESS")) return;
        AirQualitySystem.addCO2(sw, pos, 1.2f);
    }
}

