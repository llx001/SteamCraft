package eu.steamcraft.screen;

import eu.steamcraft.block.entity.custom.FridgeBlockEntity;
import eu.steamcraft.food.FoodExpirySystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FridgeScreenHandler extends ScreenHandler {
    // Fridge: 54 slots (6 rows × 9 cols). Player: 27 inv + 9 hotbar = 90 total.
    private static final int FRIDGE_ROWS = 6;
    private static final int FRIDGE_COLS = 9;
    private static final int PLAYER_INV_START = FridgeBlockEntity.SLOT_COUNT;       // 54
    private static final int HOTBAR_START     = PLAYER_INV_START + 27;              // 81
    private static final int TOTAL_SLOTS      = HOTBAR_START + 9;                  // 90

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public FridgeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(FridgeBlockEntity.SLOT_COUNT), new ArrayPropertyDelegate(3));
    }

    public FridgeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.FRIDGE, syncId);
        checkSize(inventory, FridgeBlockEntity.SLOT_COUNT);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // Fridge slots: 6 rows × 9 cols, starting at GUI (8, 18)
        for (int row = 0; row < FRIDGE_ROWS; row++) {
            for (int col = 0; col < FRIDGE_COLS; col++) {
                int slot = col + row * FRIDGE_COLS;
                this.addSlot(new FridgeSlot(inventory, slot, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player main inventory (3 rows × 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        this.addProperties(propertyDelegate);
    }

    public int getEnergy()    { return propertyDelegate.get(0) * FridgeBlockEntity.PROPERTY_SCALE; }
    public int getMaxEnergy() { return propertyDelegate.get(1) * FridgeBlockEntity.PROPERTY_SCALE; }
    public boolean isOn()     { return propertyDelegate.get(2) != 0; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < FridgeBlockEntity.SLOT_COUNT) {
                // Fridge → player area
                if (!this.insertItem(originalStack, PLAYER_INV_START, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (FoodExpirySystem.isTrackedFood(originalStack.getItem())) {
                    // Food → try fridge first
                    if (!this.insertItem(originalStack, 0, FridgeBlockEntity.SLOT_COUNT, false)) {
                        // Then shift within player area
                        if (invSlot < HOTBAR_START) {
                            if (!this.insertItem(originalStack, HOTBAR_START, TOTAL_SLOTS, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (!this.insertItem(originalStack, PLAYER_INV_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // Non-food → shift within player area only
                    if (invSlot < HOTBAR_START) {
                        if (!this.insertItem(originalStack, HOTBAR_START, TOTAL_SLOTS, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(originalStack, PLAYER_INV_START, HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
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
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private static final class FridgeSlot extends Slot {
        private FridgeSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return FoodExpirySystem.isTrackedFood(stack.getItem());
        }
    }
}
