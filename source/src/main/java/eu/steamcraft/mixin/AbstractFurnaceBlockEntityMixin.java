package eu.steamcraft.mixin;

import eu.steamcraft.air.AirQualityData;
import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.item.ModItems;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    private static final int CHIMNEY_HEIGHT = 8;
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;setLastRecipe(Lnet/minecraft/recipe/RecipeEntry;)V"
            )
    )
    private static void onSmeltFinish(
            World world,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo ci
    ) {

        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;

        ItemStack input = furnace.getStack(0);
        ItemStack fuel  = furnace.getStack(1);

        float co2 = 2f;
        float toxins = 0f;
        float plastics = 0f;

        Item item = input.getItem();

        // --- recipe pollution ---
        if (item instanceof BlockItem blockItem) {

            Block block = blockItem.getBlock();


            if (block == Blocks.SAND || block == Blocks.COAL_ORE) {
                co2 = 6f;
                toxins = 1f;
            }
        }

        if (item == Items.KELP || item == Items.BEEF || item == Items.PORKCHOP) {
            co2 = 3f;
        }

        if (item == ModItems.RUBBER) {
            co2 = 6f;
            plastics = 2f;
            toxins = 2f;
        }

        // --- fuel multiplier ---
        float fuelMultiplier = 1f;

        if (fuel.isOf(Items.STICK)) fuelMultiplier = 0.3f;
        else if (fuel.isOf(Items.CHARCOAL)) fuelMultiplier = 1f;
        else if (fuel.isOf(Items.COAL)) fuelMultiplier = 1.2f;
        else if (fuel.isOf(Items.LAVA_BUCKET)) fuelMultiplier = 2f;

        co2 *= fuelMultiplier;
        toxins *= fuelMultiplier;
        plastics *= fuelMultiplier;

        AirQualitySystem.addCO2(serverWorld, pos, co2);
        AirQualitySystem.addToxins(serverWorld, pos, toxins);
        AirQualitySystem.addMicroplastics(serverWorld, pos, plastics);

        BlockPos chimneyTop = findChimneyTop(serverWorld, pos);

        BlockPos emissionPos = chimneyTop != null ? chimneyTop : pos;
        // spawn smoke at chimney exit
        if (chimneyTop != null) {

            serverWorld.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    emissionPos.getX() + 0.5,
                    emissionPos.getY() + 0.7,
                    emissionPos.getZ() + 0.5,
                    15,
                    0.1,
                    1.5,
                    0.1,
                    0.02
            );
        }
    }


    // --- stop furnace in bad air ---

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void checkAirQuality(
            World world,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo ci
    ) {

        if (world.isClient) return;

        AirQualityData air = AirQualitySystem.get((ServerWorld) world, pos);
        if (air == null) return;

        float co2 = air.getCO2(pos);
        float toxins = air.getToxins(pos);

        // extreme conditions stop furnace
        if (co2 > 95f || toxins > 40f) {
            ci.cancel();

            if (state.contains(AbstractFurnaceBlock.LIT) && state.get(AbstractFurnaceBlock.LIT)) {
                world.setBlockState(
                        pos,
                        state.with(AbstractFurnaceBlock.LIT, false),
                        3
                );
            }

        }
    }

    private static BlockPos findChimneyTop(ServerWorld world, BlockPos pos) {

        BlockPos.Mutable check = pos.mutableCopy();

        for (int i = 1; i <= CHIMNEY_HEIGHT; i++) {

            check.move(Direction.UP);

            BlockState state = world.getBlockState(check);

            if (state.isAir())
                continue;

            if (state.isOf(Blocks.COPPER_GRATE) ||
                    state.isOf(Blocks.IRON_BARS) ||
                    state.isOf(Blocks.CHAIN))
                continue;

            return check.up();
        }

        return check;
    }
}