package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.AdvancedBatteryBlockEntity;
import eu.steamcraft.block.entity.custom.BatteryBlockEntity;
import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdvancedBatteryBlock extends BatteryBlock {

    public static final MapCodec<AdvancedBatteryBlock> CODEC = createCodec(AdvancedBatteryBlock::new);

    public AdvancedBatteryBlock(Settings settings) {
        super(EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY, EnergyHelper.BATTERY_ADVANCED_SOLAR_RATE, settings);
    }

    public AdvancedBatteryBlock(int maxEnergy, int solarChargeRate, Settings settings) {
        super(maxEnergy, solarChargeRate, settings);
    }

    @Override
    public MapCodec<PillarBlock> getCodec() {
        return PillarBlock.CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedBatteryBlockEntity(pos, state, getMaxEnergy(), getSolarChargeRate());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type == ModBlockEntities.ADVANCED_BATTERY_BE) {
            return (w, p, s, be) -> BatteryBlockEntity.serverTick(w, p, s, (BatteryBlockEntity) be);
        }
        return null;
    }
}
