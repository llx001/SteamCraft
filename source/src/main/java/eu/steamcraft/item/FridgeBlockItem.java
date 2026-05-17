package eu.steamcraft.item;

import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class FridgeBlockItem extends BlockItem implements SimpleEnergyItem {
    public FridgeBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getStoredEnergy(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13f * getStoredEnergy(stack) / EnergyHelper.FRIDGE_MAX_ENERGY);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x4488FF;
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return EnergyHelper.FRIDGE_MAX_ENERGY;
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
