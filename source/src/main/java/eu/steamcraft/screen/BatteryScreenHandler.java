package eu.steamcraft.screen;

import eu.steamcraft.block.entity.custom.BatteryBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BatteryScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    // Client-side constructor
    public BatteryScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(2));
    }

    // Server-side constructor
    public BatteryScreenHandler(int syncId, PlayerInventory playerInventory,
                                Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.BATTERY, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // Charging slot — same position as tree tap output slot
        this.addSlot(new Slot(inventory, 0, 73, 32));

        // Player inventory (3 rows × 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addProperties(propertyDelegate);
    }

    public int getEnergy() { return propertyDelegate.get(0) * BatteryBlockEntity.PROPERTY_SCALE; }
    public int getMaxEnergy() { return propertyDelegate.get(1) * BatteryBlockEntity.PROPERTY_SCALE; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot == 0) {
                // Charging slot → player inventory
                if (!this.insertItem(originalStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory → try charging slot first, then shift between player rows
                if (!this.insertItem(originalStack, 0, 1, false)) {
                    if (invSlot < 28) {
                        if (!this.insertItem(originalStack, 28, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.insertItem(originalStack, 1, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
