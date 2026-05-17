package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.MachineBlock;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.EnvironmentalMonitorBlockEntity;
import eu.steamcraft.block.entity.custom.PoweredMachineBlockEntity;
import eu.steamcraft.network.AirHealthMapPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnvironmentalMonitorBlock extends MachineBlock implements BlockEntityProvider {

    public static final MapCodec<EnvironmentalMonitorBlock> CODEC =
            createCodec(EnvironmentalMonitorBlock::new);

    private static final int TTL_TICKS = 100; // ~5s HUD flash

    public EnvironmentalMonitorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnvironmentalMonitorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        if (type == ModBlockEntities.ENVIRONMENTAL_MONITOR_BE) {
            return (w, p, s, be) -> PoweredMachineBlockEntity.serverTick(w, p, s, (PoweredMachineBlockEntity) be);
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        if (!(world.getBlockEntity(pos) instanceof EnvironmentalMonitorBlockEntity monitor)
                || !(player instanceof ServerPlayerEntity serverPlayer)
                || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        if (!monitor.consumeScanEnergy()) {
            player.sendMessage(Text.literal("§c[Monitor]§r No power"), true);
            return ActionResult.CONSUME;
        }

        byte[] cells = EnvironmentalMonitorBlockEntity.scan(serverWorld, pos);
        ServerPlayNetworking.send(serverPlayer,
                new AirHealthMapPayload(EnvironmentalMonitorBlockEntity.GRID, TTL_TICKS, cells));
        return ActionResult.CONSUME;
    }
}
