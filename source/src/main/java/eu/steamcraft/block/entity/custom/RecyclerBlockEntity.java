package eu.steamcraft.block.entity.custom;

import eu.steamcraft.block.entity.ImplementedInventory;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerBlockEntity extends BlockEntity implements ImplementedInventory {

    private static final int TICK_INTERVAL = 40;
    private static final int SLOT_INPUT  = 0;
    private static final int SLOT_OUTPUT = 1;

    /** Fixed yield lookup: item → output stack. Damage on the input is checked at insertion. */
    private static final Map<Item, ItemStack> RECIPE_MAP = new HashMap<>();

    static {
        // Rotten food → 1 Biomass
        RECIPE_MAP.put(ModItems.ROTTEN_FOOD, new ItemStack(ModItems.BIOMASS, 1));
        // Plastic tools → 1 Plastic Sheet
        RECIPE_MAP.put(ModItems.PLASTIC_AXE,      new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_PICKAXE,  new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_SHOVEL,   new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_HOE,      new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_SWORD,    new ItemStack(ModItems.PLASTIC_SHEET, 1));
        // Plastic armor → 1 Plastic Sheet
        RECIPE_MAP.put(ModItems.PLASTIC_ARMOR_HELMET,     new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_ARMOR_CHESTPLATE, new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_ARMOR_LEGGINGS,   new ItemStack(ModItems.PLASTIC_SHEET, 1));
        RECIPE_MAP.put(ModItems.PLASTIC_ARMOR_BOOTS,      new ItemStack(ModItems.PLASTIC_SHEET, 1));
        // Steel tools → 1 Steel Ingot
        RECIPE_MAP.put(ModItems.STEEL_AXE,      new ItemStack(ModItems.STEEL_INGOT, 1));
        RECIPE_MAP.put(ModItems.STEEL_PICKAXE,  new ItemStack(ModItems.STEEL_INGOT, 1));
        RECIPE_MAP.put(ModItems.STEEL_SHOVEL,   new ItemStack(ModItems.STEEL_INGOT, 1));
        RECIPE_MAP.put(ModItems.STEEL_HOE,      new ItemStack(ModItems.STEEL_INGOT, 1));
        RECIPE_MAP.put(ModItems.STEEL_SWORD,    new ItemStack(ModItems.STEEL_INGOT, 1));
        // Vanilla wooden tools → 1 Oak Planks
        RECIPE_MAP.put(Items.WOODEN_AXE,      new ItemStack(Items.OAK_PLANKS, 1));
        RECIPE_MAP.put(Items.WOODEN_PICKAXE,  new ItemStack(Items.OAK_PLANKS, 1));
        RECIPE_MAP.put(Items.WOODEN_SHOVEL,   new ItemStack(Items.OAK_PLANKS, 1));
        RECIPE_MAP.put(Items.WOODEN_HOE,      new ItemStack(Items.OAK_PLANKS, 1));
        RECIPE_MAP.put(Items.WOODEN_SWORD,    new ItemStack(Items.OAK_PLANKS, 1));
        // Vanilla stone tools → 1 Cobblestone
        RECIPE_MAP.put(Items.STONE_AXE,      new ItemStack(Items.COBBLESTONE, 1));
        RECIPE_MAP.put(Items.STONE_PICKAXE,  new ItemStack(Items.COBBLESTONE, 1));
        RECIPE_MAP.put(Items.STONE_SHOVEL,   new ItemStack(Items.COBBLESTONE, 1));
        RECIPE_MAP.put(Items.STONE_HOE,      new ItemStack(Items.COBBLESTONE, 1));
        RECIPE_MAP.put(Items.STONE_SWORD,    new ItemStack(Items.COBBLESTONE, 1));
        // Vanilla iron tools → 1 Iron Ingot
        RECIPE_MAP.put(Items.IRON_AXE,      new ItemStack(Items.IRON_INGOT, 1));
        RECIPE_MAP.put(Items.IRON_PICKAXE,  new ItemStack(Items.IRON_INGOT, 1));
        RECIPE_MAP.put(Items.IRON_SHOVEL,   new ItemStack(Items.IRON_INGOT, 1));
        RECIPE_MAP.put(Items.IRON_HOE,      new ItemStack(Items.IRON_INGOT, 1));
        RECIPE_MAP.put(Items.IRON_SWORD,    new ItemStack(Items.IRON_INGOT, 1));
        // Vanilla golden tools → 1 Gold Ingot
        RECIPE_MAP.put(Items.GOLDEN_AXE,      new ItemStack(Items.GOLD_INGOT, 1));
        RECIPE_MAP.put(Items.GOLDEN_PICKAXE,  new ItemStack(Items.GOLD_INGOT, 1));
        RECIPE_MAP.put(Items.GOLDEN_SHOVEL,   new ItemStack(Items.GOLD_INGOT, 1));
        RECIPE_MAP.put(Items.GOLDEN_HOE,      new ItemStack(Items.GOLD_INGOT, 1));
        RECIPE_MAP.put(Items.GOLDEN_SWORD,    new ItemStack(Items.GOLD_INGOT, 1));
        // Vanilla diamond tools → 1 Diamond
        RECIPE_MAP.put(Items.DIAMOND_AXE,      new ItemStack(Items.DIAMOND, 1));
        RECIPE_MAP.put(Items.DIAMOND_PICKAXE,  new ItemStack(Items.DIAMOND, 1));
        RECIPE_MAP.put(Items.DIAMOND_SHOVEL,   new ItemStack(Items.DIAMOND, 1));
        RECIPE_MAP.put(Items.DIAMOND_HOE,      new ItemStack(Items.DIAMOND, 1));
        RECIPE_MAP.put(Items.DIAMOND_SWORD,    new ItemStack(Items.DIAMOND, 1));
        // Vanilla netherite tools → 1 Netherite Ingot
        RECIPE_MAP.put(Items.NETHERITE_AXE,      new ItemStack(Items.NETHERITE_INGOT, 1));
        RECIPE_MAP.put(Items.NETHERITE_PICKAXE,  new ItemStack(Items.NETHERITE_INGOT, 1));
        RECIPE_MAP.put(Items.NETHERITE_SHOVEL,   new ItemStack(Items.NETHERITE_INGOT, 1));
        RECIPE_MAP.put(Items.NETHERITE_HOE,      new ItemStack(Items.NETHERITE_INGOT, 1));
        RECIPE_MAP.put(Items.NETHERITE_SWORD,    new ItemStack(Items.NETHERITE_INGOT, 1));
    }

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int tickCounter = 0;

    public RecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECYCLER_BE, pos, state);
    }

    // -------------------------------------------------------------------------
    // ImplementedInventory
    // -------------------------------------------------------------------------

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    /** Hoppers may only push into slot 0, and only if the item is accepted. */
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot != SLOT_INPUT) return false;
        if (!RECIPE_MAP.containsKey(stack.getItem())) return false;
        // Rotten food has no durability — always accept.
        // Tools/armor: only accept if not fully broken (remaining durability > 0).
        int maxDmg = stack.getMaxDamage();
        return maxDmg == 0 || stack.getDamage() < maxDmg;
    }

    /** Hoppers may only pull from slot 1 (output). */
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    private static final float FAIL_CHANCE = 0.30f;

    public void tick(World world, BlockPos pos, BlockState state) {
        if (++tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty()) return;

        // Skip fully-broken tools (shouldn't reach here via canInsert, but guard anyway)
        int maxDmg = input.getMaxDamage();
        if (maxDmg > 0 && input.getDamage() >= maxDmg) return;

        ItemStack result = RECIPE_MAP.get(input.getItem());
        if (result == null) return;

        // 30% chance of failed recycling — consume the input but produce nothing
        if (world.getRandom().nextFloat() < FAIL_CHANCE) {
            inventory.get(SLOT_INPUT).decrement(1);
            markDirty();
            return;
        }

        tryProduce(result.copy());
    }

    /**
     * Attempts to place {@code output} into slot 1.
     * Consumes one item from slot 0 only when the output actually fits.
     */
    private void tryProduce(ItemStack output) {
        ItemStack current = inventory.get(SLOT_OUTPUT);
        if (current.isEmpty()) {
            inventory.set(SLOT_OUTPUT, output);
            inventory.get(SLOT_INPUT).decrement(1);
            markDirty();
        } else if (ItemStack.areItemsEqual(current, output)
                && current.getCount() + output.getCount() <= current.getMaxCount()) {
            current.increment(output.getCount());
            inventory.get(SLOT_INPUT).decrement(1);
            markDirty();
        }
        // Output slot full → leave input in place, try again next interval
    }

    // -------------------------------------------------------------------------
    // Status readout (right-click)
    // -------------------------------------------------------------------------

    /** Builds human-readable status lines shown on right-click. */
    public List<Text> getStatusMessages(World world) {
        List<Text> lines = new ArrayList<>();

        lines.add(Text.literal("--- Recycler ---").formatted(Formatting.GOLD));

        // --- Input slot ---
        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty()) {
            lines.add(Text.literal("Input: ").formatted(Formatting.GRAY)
                    .append(Text.literal("empty").formatted(Formatting.DARK_GRAY)));
        } else {
            MutableText inputLine = Text.literal("Input: ").formatted(Formatting.GRAY)
                    .append(input.getName().copy().formatted(Formatting.WHITE))
                    .append(Text.literal(" x" + input.getCount()).formatted(Formatting.DARK_GRAY));
            lines.add(inputLine);

            ItemStack result = RECIPE_MAP.get(input.getItem());
            if (result != null) {
                int maxDmg = input.getMaxDamage();
                if (maxDmg > 0) {
                    int remaining = maxDmg - input.getDamage();
                    boolean broken = remaining <= 0;
                    lines.add(Text.literal("  Durability: " + remaining + " / " + maxDmg)
                            .formatted(broken ? Formatting.RED : Formatting.GRAY));
                    if (broken) {
                        lines.add(Text.literal("  → BROKEN — cannot be recycled").formatted(Formatting.DARK_RED));
                    } else {
                        lines.add(Text.literal("  → will produce: ").formatted(Formatting.GRAY)
                                .append(result.getName().copy().formatted(Formatting.AQUA))
                                .append(Text.literal(" (70% chance)").formatted(Formatting.DARK_GRAY)));
                    }
                } else {
                    lines.add(Text.literal("  → will produce: ").formatted(Formatting.GRAY)
                            .append(result.getName().copy().formatted(Formatting.AQUA))
                            .append(Text.literal(" (70% chance)").formatted(Formatting.DARK_GRAY)));
                }
            } else {
                lines.add(Text.literal("  → no recipe (won't process)").formatted(Formatting.DARK_RED));
            }
        }

        // --- Output slot ---
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            lines.add(Text.literal("Output: ").formatted(Formatting.GRAY)
                    .append(Text.literal("empty").formatted(Formatting.DARK_GRAY)));
        } else {
            boolean full = output.getCount() >= output.getMaxCount();
            MutableText outputLine = Text.literal("Output: ").formatted(Formatting.GRAY)
                    .append(output.getName().copy().formatted(full ? Formatting.RED : Formatting.WHITE))
                    .append(Text.literal(" x" + output.getCount()
                            + (full ? " (FULL — clear to continue)" : ""))
                            .formatted(full ? Formatting.RED : Formatting.DARK_GRAY));
            lines.add(outputLine);
        }

        return lines;
    }


    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("recycler_tick", tickCounter);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        tickCounter = nbt.getInt("recycler_tick");
    }
}

