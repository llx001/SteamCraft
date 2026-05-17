package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.custom.TreeTapBlock;
import eu.steamcraft.block.entity.ImplementedInventory;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.item.ModItems;
import eu.steamcraft.screen.TreeTapScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public class TreeTapBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory =
            DefaultedList.ofSize(1, ItemStack.EMPTY);

    protected final PropertyDelegate propertyDelegate;
    private int progress = 0;
    private int maxProgress = 100;

    private int logProgress = 0;
    private int maxLogProgress = 4;


    public TreeTapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TREE_TAP_BE, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> TreeTapBlockEntity.this.progress;
                    case 1 -> TreeTapBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> TreeTapBlockEntity.this.progress = value;
                    case 1 -> TreeTapBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) return;

        Direction facing = state.get(TreeTapBlock.FACING);
        BlockPos behind = pos.offset(facing.getOpposite());

        if (!world.getBlockState(behind).isIn(BlockTags.LOGS)) {
            // Reset progress if no log is behind
            this.progress = 0;
            this.logProgress = 0;
            world.setBlockBreakingInfo(0, behind, -1); // Clear breaking animation
            return;
        }

        if (this.progress < this.maxProgress) {
            this.progress++;

            // Calculate break progress across all extraction cycles (0-9)
            // Each cycle covers a fraction: logProgress gives the base, progress within cycle gives the sub-progress
            float overallProgress = (logProgress + (float) this.progress / this.maxProgress) / this.maxLogProgress;
            int breakProgress = (int) (overallProgress * 10);
            breakProgress = Math.min(breakProgress, 9); // Cap at 9 (10 means fully broken visually)

            world.setBlockBreakingInfo(0, behind, breakProgress);
            return;
        }

        // Extraction complete for this cycle
        ItemStack current = this.getStack(0);

        if (current.isEmpty()) {
            this.setStack(0, new ItemStack(ModItems.RUBBER_SAP));
        } else if (current.isOf(ModItems.RUBBER_SAP) && current.getCount() < current.getMaxCount()) {
            current.increment(1);
        } else {
            return; // don't reset progress if inventory is full
        }

        logProgress++;
        this.progress = 0;

        // Break the log after extracting maxLogProgress sap (4 sap per log)
        if (logProgress >= maxLogProgress) {
            world.breakBlock(behind, false);
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_DRIP, SoundCategory.BLOCKS, 1f, 1.0f);
            world.setBlockBreakingInfo(0, behind, -1); // Clear breaking animation
            logProgress = 0;
        }
    }




    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.steamcraft.tree_tap");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TreeTapScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    // NBT read/write

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("tree_tap.progress", this.progress);
        nbt.putInt("tree_tap.maxProgress", this.maxProgress);
        nbt.putInt("tree_tap.logProgress", this.logProgress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.readNbt(nbt, inventory, registryLookup);
        this.progress = nbt.getInt("tree_tap.progress");
        this.maxProgress = nbt.getInt("tree_tap.maxProgress");
        this.logProgress = nbt.getInt("tree_tap.logProgress");
        super.readNbt(nbt, registryLookup);
    }

    // Syncing block entity data with the client ALWAYS COPY

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public String getInventoryContents() {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            contents.append("Slot ").append(i).append(": ")
                    .append(stack.isEmpty() ? "Empty" : stack.getCount() + "x " + stack.getItem().getName().getString())
                    .append("\n");
        }
        return contents.toString();
    }
}
