package eu.steamcraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class TreeTapScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    // Client-side constructor (called by ScreenHandlerType factory)
    public TreeTapScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(2));
    }

    // Server-side constructor
    public TreeTapScreenHandler(int syncId, PlayerInventory playerInventory,
                                Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.TREE_TAP, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // Output slot — pixel position (72, 31)
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

    public int getProgress() {
        return propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return propertyDelegate.get(1);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot == 0) {
                // Machine output → player inventory
                if (!this.insertItem(originalStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory → shift between player rows only (output slot is machine-only)
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

