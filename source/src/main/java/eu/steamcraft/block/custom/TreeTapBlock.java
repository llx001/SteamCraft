package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.MachineBlock;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.TreeTapBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TreeTapBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec <TreeTapBlock> CODEC = createCodec(
            TreeTapBlock::new
    );
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    // Add processing block state
    public static final BooleanProperty PROCESSING = BooleanProperty.of("processing");
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public TreeTapBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getDefaultState().with(PROCESSING, false)); // Default to not processing
        setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROCESSING);
        builder.add(FACING);
        super.appendProperties(builder);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TreeTapBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {

        if(state.getBlock() != newState.getBlock()) {
            // Drop inventory contents if needed
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TreeTapBlockEntity) {
                ItemScatterer.spawn(world, pos, (TreeTapBlockEntity)blockEntity);
                world.updateComparators(pos,this);
            }
        super.onStateReplaced(state, world, pos, newState, moved);
        }

    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient())
            return null;

        return validateTicker(type, ModBlockEntities.TREE_TAP_BE, (world1, pos, state1, be) -> {
            be.tick(world1, pos, state1);
        });
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(!(blockEntity instanceof TreeTapBlockEntity tap)) {
            return ActionResult.PASS;
        }

        if(player.isSneaking()) {
            // Drop inventory contents forward player is facing
                Direction facing = state.get(FACING);
                BlockPos dropPos = pos.offset(facing);
                ItemScatterer.spawn(world, dropPos, tap);

            tap.clear();
            tap.markDirty();
            return ActionResult.CONSUME;
        }

        player.openHandledScreen(tap);
        return ActionResult.CONSUME;



    }
}
