package eu.steamcraft.screen;

import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.item.FoodPouchItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class FoodPouchScreenHandler extends ScreenHandler {
    private static final int POUCH_SLOT_COUNT = 9;
    private static final int PLAYER_INV_START = 9;
    private static final int HOTBAR_START = 36;
    private static final int PLAYER_SLOT_END = 45;

    private final Inventory pouchInventory;
    @Nullable
    private final Hand hand;
    private final int lockedIndex;

    public FoodPouchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null, new SimpleInventory(POUCH_SLOT_COUNT));
    }

    public FoodPouchScreenHandler(int syncId, PlayerInventory playerInventory, Hand hand) {
        this(syncId, playerInventory, hand, createPouchInventory(playerInventory.player, hand));
    }

    private FoodPouchScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable Hand hand, Inventory inventory) {
        super(ModScreenHandlers.FOOD_POUCH, syncId);
        checkSize(inventory, POUCH_SLOT_COUNT);
        this.pouchInventory = inventory;
        this.hand = hand;
        this.lockedIndex = hand == Hand.MAIN_HAND ? HOTBAR_START + playerInventory.selectedSlot : -1;
        inventory.onOpen(playerInventory.player);

        for (int col = 0; col < POUCH_SLOT_COUNT; col++) {
            this.addSlot(new PouchSlot(inventory, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            int invSlot = col;
            Slot slot = invSlot == playerInventory.selectedSlot && hand == Hand.MAIN_HAND
                    ? new LockedSlot(playerInventory, invSlot, 8 + col * 18, 108)
                    : new Slot(playerInventory, invSlot, 8 + col * 18, 108);
            this.addSlot(slot);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (slotIndex < POUCH_SLOT_COUNT) {
                if (!insertIntoPlayerArea(originalStack)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex != lockedIndex) {
                if (!this.insertItem(originalStack, 0, POUCH_SLOT_COUNT, false)) {
                    if (slotIndex < HOTBAR_START) {
                        if (!insertIntoHotbar(originalStack)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!insertIntoMainInventory(originalStack)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                return ItemStack.EMPTY;
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
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == lockedIndex || (actionType == SlotActionType.SWAP && button == player.getInventory().selectedSlot)) {
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (this.hand == null) {
            return true;
        }
        return player.getStackInHand(this.hand).getItem() instanceof FoodPouchItem;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.pouchInventory.onClose(player);
    }

    private boolean insertIntoPlayerArea(ItemStack stack) {
        return insertIntoMainInventory(stack) || insertIntoHotbar(stack);
    }

    private boolean insertIntoMainInventory(ItemStack stack) {
        if (lockedIndex < 0) {
            return this.insertItem(stack, PLAYER_INV_START, HOTBAR_START, false);
        }
        if (this.insertItem(stack, PLAYER_INV_START, lockedIndex, false)) {
            return true;
        }
        return this.insertItem(stack, lockedIndex + 1, HOTBAR_START, false);
    }

    private boolean insertIntoHotbar(ItemStack stack) {
        if (lockedIndex < 0) {
            return this.insertItem(stack, HOTBAR_START, PLAYER_SLOT_END, false);
        }
        if (this.insertItem(stack, HOTBAR_START, lockedIndex, false)) {
            return true;
        }
        return this.insertItem(stack, lockedIndex + 1, PLAYER_SLOT_END, false);
    }

    private static Inventory createPouchInventory(PlayerEntity player, @Nullable Hand hand) {
        final boolean[] initializing = {true};
        SimpleInventory inventory = new SimpleInventory(POUCH_SLOT_COUNT) {
            @Override
            public void markDirty() {
                if (initializing[0]) {
                    return;
                }
                if (hand == null) {
                    return;
                }

                ItemStack live = player.getStackInHand(hand);
                if (!(live.getItem() instanceof FoodPouchItem)) {
                    return;
                }

                DefaultedList<ItemStack> stacks = DefaultedList.ofSize(POUCH_SLOT_COUNT, ItemStack.EMPTY);
                for (int i = 0; i < POUCH_SLOT_COUNT; i++) {
                    stacks.set(i, this.getStack(i).copy());
                }
                live.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                player.playerScreenHandler.sendContentUpdates();
            }
        };

        if (hand != null) {
            ItemStack live = player.getStackInHand(hand);
            if (live.getItem() instanceof FoodPouchItem) {
                DefaultedList<ItemStack> stacks = DefaultedList.ofSize(POUCH_SLOT_COUNT, ItemStack.EMPTY);
                live.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(stacks);
                for (int i = 0; i < POUCH_SLOT_COUNT; i++) {
                    inventory.setStack(i, stacks.get(i));
                }
            }
        }

        initializing[0] = false;

        return inventory;
    }

    private static final class PouchSlot extends Slot {
        private PouchSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return !(stack.getItem() instanceof FoodPouchItem)
                    && FoodExpirySystem.isTrackedFood(stack.getItem());
        }
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}

