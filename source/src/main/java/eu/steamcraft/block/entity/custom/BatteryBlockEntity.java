package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.entity.ImplementedInventory;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.energy.EnergyHelper;
import eu.steamcraft.screen.BatteryScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyItem;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class BatteryBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
    private static final int ITEM_CHARGE_RATE = 100;
    // Per-tick output budget used when pushing to adjacent blocks.
    // Aligned with the TR API maxExtract limits defined in EnergyHelper.
    private static final int BATTERY_OUTPUT_RATE          = EnergyHelper.BATTERY_MAX_EXTRACT;
    private static final int BATTERY_ADVANCED_OUTPUT_RATE = EnergyHelper.BATTERY_ADVANCED_MAX_EXTRACT;
    // Property sync uses short-backed screen values; scale avoids overflow at 200k capacity with cosmetic rounding.
    public static final int PROPERTY_SCALE = 8;

    private final SimpleEnergyStorage energyStorage;
    private final int solarChargeRate;
    private final int outputRate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    protected final PropertyDelegate propertyDelegate;

    // Protected so subclasses can supply their own BlockEntityType
    protected BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  int maxEnergy, int solarChargeRate) {
        this(type, pos, state, maxEnergy, solarChargeRate,
                maxEnergy >= EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY
                        ? BATTERY_ADVANCED_OUTPUT_RATE
                        : BATTERY_OUTPUT_RATE);
    }

    // Output rate is a per-tick total budget across all 6 faces.
    protected BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  int maxEnergy, int solarChargeRate, int outputRate) {
        super(type, pos, state);
        this.solarChargeRate = solarChargeRate;
        this.outputRate = outputRate;
        // maxInsert / maxExtract determined by which tier this battery is
        int maxInsert  = maxEnergy >= EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY
                ? EnergyHelper.BATTERY_ADVANCED_MAX_INPUT   : EnergyHelper.BATTERY_MAX_INPUT;
        int maxExtract = maxEnergy >= EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY
                ? EnergyHelper.BATTERY_ADVANCED_MAX_EXTRACT : EnergyHelper.BATTERY_MAX_EXTRACT;
        this.energyStorage = new SimpleEnergyStorage(maxEnergy, maxInsert, maxExtract) {
            @Override
            protected void onFinalCommit() {
                BatteryBlockEntity.this.markDirty();
            }
        };
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int)(energyStorage.amount / PROPERTY_SCALE);
                    case 1 -> (int)(energyStorage.capacity / PROPERTY_SCALE);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) energyStorage.amount = (long) value * PROPERTY_SCALE;
            }

            @Override
            public int size() { return 2; }
        };
    }

    public BatteryBlockEntity(BlockPos pos, BlockState state, int maxEnergy, int solarChargeRate) {
        this(ModBlockEntities.BATTERY_BE, pos, state, maxEnergy, solarChargeRate);
    }

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, EnergyHelper.BATTERY_MAX_ENERGY, EnergyHelper.BATTERY_SOLAR_RATE);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        // Solar charging — requires full sky light above (day + no rain + no obstruction)
        if (be.solarChargeRate > 0
                && world.getLightLevel(LightType.SKY, pos.up()) == 15
                && be.energyStorage.amount < be.energyStorage.capacity) {
            try (Transaction tx = Transaction.openOuter()) {
                be.energyStorage.insert(be.solarChargeRate, tx);
                tx.commit();
            }
        }

        // Push energy to adjacent consumers before slot charging so empty slot does not skip distribution.
        if (be.energyStorage.amount > 0) {
            long budget = be.outputRate;
            for (Direction dir : Direction.values()) {
                if (budget <= 0) {
                    break;
                }

                EnergyStorage target = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());
                if (target == null) {
                    continue;
                }

                long moved = EnergyStorageUtil.move(be.energyStorage, target, budget, null);
                budget -= moved;
            }
        }

        // Item charging — drain battery into chargeable item in slot 0
        ItemStack stack = be.getStack(0);
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof SimpleEnergyItem energyItem)) return;
        if (energyItem.getEnergyMaxInput(stack) <= 0) return;

        long maxItemEnergy = energyItem.getEnergyCapacity(stack);
        long itemEnergy = energyItem.getStoredEnergy(stack);
        long need = maxItemEnergy - itemEnergy;
        if (need <= 0 || be.energyStorage.amount <= 0) return;

        int transfer = (int) Math.min(ITEM_CHARGE_RATE, Math.min(need, be.energyStorage.amount));
        int removed = be.removeEnergy(transfer);
        if (removed <= 0) return;
        energyItem.setStoredEnergy(stack, itemEnergy + removed);
    }


    public int getSolarChargeRate() { return solarChargeRate; }

    @Override
    public DefaultedList<ItemStack> getItems() { return inventory; }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.steamcraft.battery");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BatteryScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    public SimpleEnergyStorage getEnergyStorage() { return energyStorage; }

    public int getEnergy() { return (int) energyStorage.amount; }

    public int getMaxEnergy() { return (int) energyStorage.capacity; }

    public void setEnergy(int energy) {
        energyStorage.amount = Math.max(0, Math.min((int) energyStorage.capacity, energy));
        markDirty();
    }

    public int removeEnergy(int amount) {
        try (Transaction tx = Transaction.openOuter()) {
            long removed = energyStorage.extract(amount, tx);
            tx.commit();
            return (int) removed;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("energy", energyStorage.amount);
        Inventories.writeNbt(nbt, inventory, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energyStorage.amount = Math.max(0, Math.min(energyStorage.capacity, nbt.getLong("energy")));
        Inventories.readNbt(nbt, inventory, registryLookup);
    }
}
