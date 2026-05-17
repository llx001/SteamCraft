package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.MachineBlock;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.FridgeBlockEntity;
import eu.steamcraft.item.FridgeBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FridgeBlock extends MachineBlock implements BlockEntityProvider {
    public static final MapCodec<FridgeBlock> CODEC = createCodec(FridgeBlock::new);
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public FridgeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(POWERED, false));
    }

    @Override
    protected MapCodec<? extends MachineBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FridgeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        if (type == ModBlockEntities.FRIDGE_BE) {
            return (w, p, s, be) -> FridgeBlockEntity.serverTick(w, p, s, (FridgeBlockEntity) be);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && world.getBlockEntity(pos) instanceof FridgeBlockEntity fridge
                && itemStack.getItem() instanceof FridgeBlockItem fridgeItem) {
            fridge.setEnergy((int) fridgeItem.getStoredEnergy(itemStack));
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FridgeBlockEntity fridge) {
                ItemScatterer.spawn(world, pos, fridge);
                if (!world.isClient && fridge.getEnergy() > 0) {
                    ItemStack stack = new ItemStack(this.asItem());
                    if (stack.getItem() instanceof FridgeBlockItem fridgeItem) {
                        fridgeItem.setStoredEnergy(stack, fridge.getEnergy());
                    }
                    ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
                world.updateComparators(pos, this);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        LootContextParameterSet params = builder
                .add(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);
        BlockEntity be = params.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (be instanceof FridgeBlockEntity fridge && fridge.getEnergy() > 0) {
            return List.of();
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof NamedScreenHandlerFactory factory)) {
            return ActionResult.PASS;
        }

        player.openHandledScreen(factory);
        return ActionResult.CONSUME;
    }
}
