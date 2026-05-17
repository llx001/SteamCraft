package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.RecyclerBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RecyclerBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final MapCodec<RecyclerBlock> CODEC = createCodec(RecyclerBlock::new);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    public RecyclerBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING,
                ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RecyclerBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RecyclerBlockEntity recycler) {
                ItemScatterer.spawn(world, pos, (RecyclerBlockEntity) recycler);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return validateTicker(type, ModBlockEntities.RECYCLER_BE,
                (w, p, s, be) -> be.tick(w, p, s));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                  BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof RecyclerBlockEntity recycler)) return ActionResult.PASS;

        for (Text line : recycler.getStatusMessages(world)) {
            player.sendMessage(line, false);
        }
        return ActionResult.CONSUME;
    }
}


