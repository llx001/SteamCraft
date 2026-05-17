package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.entity.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class ElectricTorchBlockEntity extends BlockEntity {
    public static final int MAX_ENERGY = 10_000;
    private static final int DRAIN_PER_TICK = 1;

    private final SimpleEnergyStorage energyStorage;
    private boolean enabled = true;

    public ElectricTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_TORCH_BE, pos, state);
        this.energyStorage = new SimpleEnergyStorage(MAX_ENERGY, MAX_ENERGY, 0) {
            @Override
            protected void onFinalCommit() {
                ElectricTorchBlockEntity.this.markDirty();
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
        energyStorage.amount = Math.max(0, Math.min(MAX_ENERGY, energy));
        markDirty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        markDirty();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ElectricTorchBlockEntity be) {
        if (be.enabled && be.energyStorage.amount > 0) {
            be.energyStorage.amount = Math.max(0, be.energyStorage.amount - DRAIN_PER_TICK);
            be.markDirty();
        }

        boolean shouldBePowered = be.enabled && be.energyStorage.amount > 0;
        if (state.contains(Properties.POWERED) && state.get(Properties.POWERED) != shouldBePowered) {
            world.setBlockState(pos, state.with(Properties.POWERED, shouldBePowered));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("energy", energyStorage.amount);
        nbt.putBoolean("enabled", enabled);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energyStorage.amount = Math.max(0, Math.min(MAX_ENERGY, nbt.getLong("energy")));
        // Default to enabled for torches placed before this flag existed.
        enabled = !nbt.contains("enabled") || nbt.getBoolean("enabled");
    }
}

