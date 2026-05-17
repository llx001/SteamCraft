package eu.steamcraft.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class ElectricTorchItem extends VerticallyAttachableBlockItem implements SimpleEnergyItem {
    private final int maxEnergy;

    public ElectricTorchItem(Block floorBlock, Block wallBlock, Item.Settings settings,
                             Direction attachmentDirection, int maxEnergy) {
        super(floorBlock, wallBlock, settings, attachmentDirection);
        this.maxEnergy = maxEnergy;
    }

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
        return 0x4488FF; // Blue
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return maxEnergy;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return maxEnergy;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }
}

