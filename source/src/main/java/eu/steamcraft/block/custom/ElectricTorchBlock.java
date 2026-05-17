package eu.steamcraft.block.custom;

import com.mojang.serialization.MapCodec;
import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.ElectricTorchBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.List;

public class ElectricTorchBlock extends TorchBlock implements BlockEntityProvider {

    public static final MapCodec<ElectricTorchBlock> CODEC = createCodec(
            settings -> new ElectricTorchBlock(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, settings)
    );

    public ElectricTorchBlock(SimpleParticleType particle, Settings settings) {
        super(particle, settings);
        setDefaultState(getDefaultState().with(Properties.POWERED, false));
    }

    @Override
    public MapCodec<TorchBlock> getCodec() {
        return TorchBlock.CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.POWERED);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(Properties.POWERED)) {
            super.randomDisplayTick(state, world, pos, random);
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricTorchBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type == ModBlockEntities.ELECTRIC_TORCH_BE) {
            return (w, p, s, be) -> ElectricTorchBlockEntity.serverTick(w, p, s, (ElectricTorchBlockEntity) be);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient
                && world.getBlockEntity(pos) instanceof ElectricTorchBlockEntity be
                && itemStack.getItem() instanceof SimpleEnergyItem energyItem) {
            int energy = (int) energyItem.getStoredEnergy(itemStack);
            be.setEnergy(energy);
            if (energy > 0 && !state.get(Properties.POWERED)) {
                world.setBlockState(pos, state.with(Properties.POWERED, true));
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        if (!(world.getBlockEntity(pos) instanceof ElectricTorchBlockEntity torch)) return ActionResult.PASS;

        if (player.isSneaking()) {
            if (torch.getEnergy() <= 0) {
                player.sendMessage(Text.literal("§c[Torch]§r No energy"), true);
                return ActionResult.CONSUME;
            }
            boolean nowEnabled = !torch.isEnabled();
            torch.setEnabled(nowEnabled);
            boolean powered = nowEnabled && torch.getEnergy() > 0;
            if (state.contains(Properties.POWERED) && state.get(Properties.POWERED) != powered) {
                world.setBlockState(pos, state.with(Properties.POWERED, powered));
            }
            player.sendMessage(
                    Text.literal(nowEnabled ? "§e[Torch]§r Enabled" : "§e[Torch]§r Disabled"),
                    true
            );
            return ActionResult.CONSUME;
        }


        player.sendMessage(
                Text.literal("§e[Torch]§r Energy: " + torch.getEnergy() + " / " + ElectricTorchBlockEntity.MAX_ENERGY),
                true
        );
        return ActionResult.CONSUME;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock() && !world.isClient) {
            if (world.getBlockEntity(pos) instanceof ElectricTorchBlockEntity torch && torch.getEnergy() > 0) {
                ItemStack stack = new ItemStack(ModBlocks.ELECTRIC_TORCH_ITEM);
                if (stack.getItem() instanceof SimpleEnergyItem energyItem) {
                    energyItem.setStoredEnergy(stack, torch.getEnergy());
                }
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        LootContextParameterSet params = builder
                .add(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);
        if (params.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof ElectricTorchBlockEntity torch
                && torch.getEnergy() > 0) {
            return List.of();
        }
        return super.getDroppedStacks(state, builder);
    }
}

