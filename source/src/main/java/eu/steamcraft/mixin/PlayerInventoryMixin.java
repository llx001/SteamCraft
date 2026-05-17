package eu.steamcraft.mixin;

import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;
    @Shadow public PlayerEntity player;

    @Inject(method = "updateItems", at = @At("HEAD"))
    private void steamcraft$stampTrackedFoods(CallbackInfo ci) {
        if (this.player.getWorld().isClient()) {
            return;
        }

        long currentDay = FoodExpirySystem.getCurrentDay(this.player.getWorld());
        for (DefaultedList<ItemStack> inventoryPart : this.combinedInventory) {
            for (int i = 0; i < inventoryPart.size(); i++) {
                ItemStack stack = inventoryPart.get(i);
                FoodExpirySystem.stampIfMissing(stack, currentDay);
                if (!stack.isEmpty() && FoodExpirySystem.isTrackedFood(stack.getItem())
                        && FoodExpirySystem.getExpiresInDays(stack, currentDay) < 0) {
                    inventoryPart.set(i, new ItemStack(ModItems.BIOMASS, stack.getCount()));
                }
            }
        }
    }
}

