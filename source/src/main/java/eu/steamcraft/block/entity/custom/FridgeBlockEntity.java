package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.custom.FridgeBlock;
import eu.steamcraft.block.entity.ImplementedInventory;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.energy.EnergyHelper;
import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.screen.FridgeScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FridgeBlockEntity extends PoweredMachineBlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
    public static final int SLOT_COUNT = 54;
    public static final int PROPERTY_SCALE = 8;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate;
    private long lastPreserveDay = Long.MIN_VALUE;

    public FridgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRIDGE_BE, pos, state, EnergyHelper.FRIDGE_MAX_ENERGY, EnergyHelper.FRIDGE_MAX_ENERGY);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) (FridgeBlockEntity.this.getEnergyStorage().amount / PROPERTY_SCALE);
                    case 1 -> (int) (FridgeBlockEntity.this.getEnergyStorage().capacity / PROPERTY_SCALE);
                    case 2 -> FridgeBlockEntity.this.getEnergyStorage().amount > 0 ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    FridgeBlockEntity.this.getEnergyStorage().amount = (long) value * PROPERTY_SCALE;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, FridgeBlockEntity be) {
        if (world.isClient()) {
            return;
        }

        // Sync powered blockstate
        boolean shouldBeOn = be.getEnergyStorage().amount > 0;
        if (state.get(FridgeBlock.POWERED) != shouldBeOn) {
            world.setBlockState(pos, state.with(FridgeBlock.POWERED, shouldBeOn));
        }

        long currentDay = FoodExpirySystem.getCurrentDay(world);
        if (be.lastPreserveDay == currentDay) {
            return;
        }

        be.lastPreserveDay = currentDay;

        for (int i = 0; i < be.size(); i++) {
            ItemStack stack = be.getStack(i);
            if (stack.isEmpty() || !FoodExpirySystem.isTrackedFood(stack.getItem())) {
                continue;
            }
            if (FoodExpirySystem.getCreatedDay(stack) == null) {
                continue;
            }
            if (!be.useEnergy(EnergyHelper.FRIDGE_PRESERVE_COST)) {
                break;
            }

            FoodExpirySystem.applyPreservation(stack, 1.0, currentDay);
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return FoodExpirySystem.isTrackedFood(stack.getItem());
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world == null || this.world.getBlockEntity(this.pos) != this) {
            return false;
        }
        return player.squaredDistanceTo(
                this.pos.getX() + 0.5,
                this.pos.getY() + 0.5,
                this.pos.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.steamcraft.fridge");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FridgeScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putLong("last_preserve_day", this.lastPreserveDay);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        this.lastPreserveDay = nbt.getLong("last_preserve_day");
    }
}
