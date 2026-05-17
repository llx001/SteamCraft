package eu.steamcraft.mixin;

import eu.steamcraft.item.PortableScannerItem;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The portable scanner drains energy into its data component every tick while enabled.
 * {@code HeldItemRenderer#updateHeldItems} decides whether to replay the first-person
 * re-equip ("bob") animation by comparing the old/new held stacks with
 * {@link ItemStack#areEqual}, which includes component equality — so the per-tick energy
 * change made the scanner re-equip every tick.
 *
 * 1.21.1 removed {@code Item#allowNbtUpdateAnimation}, so this redirect reinstates that
 * behaviour, scoped to the scanner: if it's the same item and only components differ,
 * treat the stacks as equal so the animation does not replay. Every other item keeps
 * vanilla behaviour.
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Redirect(
            method = "updateHeldItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean steamcraft$ignoreScannerEnergyChange(ItemStack oldStack, ItemStack newStack) {
        if (oldStack.getItem() == newStack.getItem() && oldStack.getItem() instanceof PortableScannerItem) {
            return true;
        }
        return ItemStack.areEqual(oldStack, newStack);
    }
}
