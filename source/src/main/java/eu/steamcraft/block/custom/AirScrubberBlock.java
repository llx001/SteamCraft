package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.MachineBlock;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.AirScrubberBlockEntity;
import eu.steamcraft.block.entity.custom.PoweredMachineBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AirScrubberBlock extends MachineBlock implements BlockEntityProvider {
    public static final MapCodec<AirScrubberBlock> CODEC = createCodec(AirScrubberBlock::new);

    public AirScrubberBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends MachineBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AirScrubberBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        if (type == ModBlockEntities.AIR_SCRUBBER_BE) {
            return (w, p, s, be) -> PoweredMachineBlockEntity.serverTick(w, p, s, (PoweredMachineBlockEntity) be);
        }
        return null;
    }
}


