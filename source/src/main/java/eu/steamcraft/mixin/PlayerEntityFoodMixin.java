package eu.steamcraft.mixin;

import eu.steamcraft.food.FoodExpirySystem;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityFoodMixin {
    @Unique private boolean steamcraft$applyScaling;
    @Unique private int steamcraft$foodBefore;
    @Unique private float steamcraft$saturationBefore;
    @Unique private float steamcraft$nutritionMultiplier;
    @Unique private long steamcraft$expiresInDays;

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void steamcraft$captureBeforeEat(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        this.steamcraft$applyScaling = false;
        this.steamcraft$nutritionMultiplier = 1.0f;
        this.steamcraft$expiresInDays = Long.MAX_VALUE;

        PlayerEntity player = (PlayerEntity) (Object) this;
        if (world.isClient() || !FoodExpirySystem.isTrackedFood(stack.getItem())) {
            return;
        }

        long currentDay = FoodExpirySystem.getCurrentDay(world);
        FoodExpirySystem.stampIfMissing(stack, currentDay);

        this.steamcraft$expiresInDays = FoodExpirySystem.getExpiresInDays(stack, currentDay);
        this.steamcraft$nutritionMultiplier = FoodExpirySystem.getNutritionMultiplier(
                this.steamcraft$expiresInDays,
                FoodExpirySystem.getMaxAgeDays(stack.getItem())
        );

        HungerManager hungerManager = player.getHungerManager();
        this.steamcraft$foodBefore = hungerManager.getFoodLevel();
        this.steamcraft$saturationBefore = hungerManager.getSaturationLevel();
        this.steamcraft$applyScaling = true;
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    private void steamcraft$scaleFoodAndApplyDebuffs(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (!this.steamcraft$applyScaling || world.isClient()) {
            return;
        }

        PlayerEntity player = (PlayerEntity) (Object) this;
        HungerManager hungerManager = player.getHungerManager();

        int currentFood = hungerManager.getFoodLevel();
        float currentSaturation = hungerManager.getSaturationLevel();

        int baseFoodGain = Math.max(0, currentFood - this.steamcraft$foodBefore);
        float baseSaturationGain = Math.max(0.0f, currentSaturation - this.steamcraft$saturationBefore);

        int scaledFoodGain = Math.max(0, Math.round(baseFoodGain * this.steamcraft$nutritionMultiplier));
        float scaledSaturationGain = Math.max(0.0f, baseSaturationGain * this.steamcraft$nutritionMultiplier);

        int adjustedFood = MathHelper.clamp(this.steamcraft$foodBefore + scaledFoodGain, 0, 20);
        float adjustedSaturation = MathHelper.clamp(
                this.steamcraft$saturationBefore + scaledSaturationGain,
                0.0f,
                adjustedFood
        );

        hungerManager.setFoodLevel(adjustedFood);
        hungerManager.setSaturationLevel(adjustedSaturation);

        FoodExpirySystem.tryApplyExpiredFoodDebuffs(player, this.steamcraft$expiresInDays);
    }
}

