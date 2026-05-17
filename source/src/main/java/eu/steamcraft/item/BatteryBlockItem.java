package eu.steamcraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class BatteryBlockItem extends BlockItem implements SimpleEnergyItem {
    private final int maxEnergy;
    private final int solarChargeRate;

    public BatteryBlockItem(Block block, Item.Settings settings, int maxEnergy, int solarChargeRate) {
        super(block, settings);
        this.maxEnergy = maxEnergy;
        this.solarChargeRate = solarChargeRate;
    }

    public int getMaxEnergy() { return maxEnergy; }
    public int getSolarChargeRate() { return solarChargeRate; }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        long energy = getStoredEnergy(stack);
        return Math.round(13f * energy / maxEnergy);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x4488FF;
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return maxEnergy;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return 0;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }
}
