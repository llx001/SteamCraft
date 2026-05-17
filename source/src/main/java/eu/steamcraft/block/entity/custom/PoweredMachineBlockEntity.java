package eu.steamcraft.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public abstract class PoweredMachineBlockEntity extends BlockEntity {
    private final SimpleEnergyStorage energyStorage;

    protected PoweredMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int maxInsert) {
        super(type, pos, state);
        this.energyStorage = new SimpleEnergyStorage(capacity, maxInsert, 0) {
            @Override
            protected void onFinalCommit() {
                PoweredMachineBlockEntity.this.markDirty();
            }
        };
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getEnergy() {
        return (int) energyStorage.amount;
    }

    public void setEnergy(int energy) {
        energyStorage.amount = Math.max(0, Math.min((int) energyStorage.capacity, energy));
        markDirty();
    }

    protected boolean useEnergy(int cost) {
        if (cost <= 0) {
            return true;
        }
        if (energyStorage.amount < cost) {
            return false;
        }
        energyStorage.amount -= cost;
        markDirty();
        return true;
    }

    public void tickMachine(World world, BlockPos pos, BlockState state) {
        // Default no-op for machine shells without runtime behavior.
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, PoweredMachineBlockEntity be) {
        be.tickMachine(world, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("energy", energyStorage.amount);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energyStorage.amount = Math.max(0, Math.min(energyStorage.capacity, nbt.getLong("energy")));
    }
}

