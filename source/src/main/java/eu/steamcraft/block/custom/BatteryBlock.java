package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.BatteryBlockEntity;
import eu.steamcraft.energy.EnergyHelper;
import eu.steamcraft.item.BatteryBlockItem;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BatteryBlock extends PillarBlock implements BlockEntityProvider {

    public static final MapCodec<BatteryBlock> CODEC = createCodec(BatteryBlock::new);

    private final int maxEnergy;
    private final int solarChargeRate;

    public BatteryBlock(Settings settings) {
        this(EnergyHelper.BATTERY_MAX_ENERGY, EnergyHelper.BATTERY_SOLAR_RATE, settings);
    }

    public BatteryBlock(int maxEnergy, Settings settings) {
        this(maxEnergy, EnergyHelper.BATTERY_SOLAR_RATE, settings);
    }

    public BatteryBlock(int maxEnergy, int solarChargeRate, Settings settings) {
        super(settings);
        this.maxEnergy = maxEnergy;
        this.solarChargeRate = solarChargeRate;
    }

    public int getMaxEnergy() { return maxEnergy; }
    public int getSolarChargeRate() { return solarChargeRate; }

    @Override
    public MapCodec<PillarBlock> getCodec() {
        return PillarBlock.CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryBlockEntity(pos, state, maxEnergy, solarChargeRate);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient
                && world.getBlockEntity(pos) instanceof BatteryBlockEntity battery
                && itemStack.getItem() instanceof BatteryBlockItem batteryItem) {
            int stored = (int) batteryItem.getStoredEnergy(itemStack);
            if (stored > 0) battery.setEnergy(stored);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type == ModBlockEntities.BATTERY_BE) {
            return (w, p, s, be) -> BatteryBlockEntity.serverTick(w, p, s, (BatteryBlockEntity) be);
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof BatteryBlockEntity battery)) return ActionResult.PASS;
        player.openHandledScreen(battery);
        return ActionResult.CONSUME;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock() && !world.isClient) {
            if (world.getBlockEntity(pos) instanceof BatteryBlockEntity battery) {
                // Drop any item in the charging slot
                ItemScatterer.spawn(world, pos, battery);
                // Drop the battery block itself with its stored energy
                if (battery.getEnergy() > 0) {
                    ItemStack stack = new ItemStack(this.asItem());
                    if (stack.getItem() instanceof BatteryBlockItem batteryItem) {
                        batteryItem.setStoredEnergy(stack, battery.getEnergy());
                    }
                    ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    // Prevent default loot table from dropping a separate (empty) item when we already dropped one above.
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        LootContextParameterSet params = builder
                .add(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);
        BlockEntity be = params.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (be instanceof BatteryBlockEntity battery && battery.getEnergy() > 0) {
            // We already manually scattered the item with energy in onStateReplaced — drop nothing here.
            return List.of();
        }
        return super.getDroppedStacks(state, builder);
    }
}

