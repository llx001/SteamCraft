package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class AdvancedBatteryBlockEntity extends BatteryBlockEntity {

    public AdvancedBatteryBlockEntity(BlockPos pos, BlockState state, int maxEnergy, int solarChargeRate) {
        super(ModBlockEntities.ADVANCED_BATTERY_BE, pos, state, maxEnergy, solarChargeRate);
    }

    public AdvancedBatteryBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY, EnergyHelper.BATTERY_ADVANCED_SOLAR_RATE);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.steamcraft.battery_advanced");
    }
}
