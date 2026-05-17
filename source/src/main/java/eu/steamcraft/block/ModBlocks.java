package eu.steamcraft.block;

import eu.steamcraft.SteamCraft;
import eu.steamcraft.block.custom.AdvancedBatteryBlock;
import eu.steamcraft.block.custom.AirScrubberBlock;
import eu.steamcraft.block.custom.BatteryBlock;
import eu.steamcraft.block.custom.ElectricTorchBlock;
import eu.steamcraft.block.custom.ElectricWallTorchBlock;
import eu.steamcraft.block.custom.EnvironmentalMonitorBlock;
import eu.steamcraft.block.custom.FridgeBlock;
import eu.steamcraft.block.custom.PoisonedSoilBlock;
import eu.steamcraft.block.custom.RecyclerBlock;
import eu.steamcraft.block.custom.RichSoilBlock;
import eu.steamcraft.block.custom.TreeTapBlock;
import eu.steamcraft.energy.EnergyHelper;
import eu.steamcraft.item.BatteryBlockItem;
import eu.steamcraft.item.ElectricTorchItem;
import eu.steamcraft.item.FridgeBlockItem;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class ModBlocks {

    // Electric Torch Block and Wall Torch Block
    public static final Block ELECTRIC_TORCH = registerBlock("electric_torch",
            new ElectricTorchBlock(
                    ParticleTypes.ELECTRIC_SPARK,
                    Block.Settings.copy(Blocks.TORCH)
                            .luminance(state -> state.get(net.minecraft.state.property.Properties.POWERED) ? 14 : 0)
            ),
            false);

    public static final Block ELECTRIC_TORCH_WALl = registerBlock("electric_torch_wall",
            new ElectricWallTorchBlock(
                    ParticleTypes.ELECTRIC_SPARK,
                    Block.Settings.copy(Blocks.WALL_TORCH)
                            .luminance(state -> state.get(net.minecraft.state.property.Properties.POWERED) ? 14 : 0)
            ),
            false);

    public static final Item ELECTRIC_TORCH_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(SteamCraft.MOD_ID, "electric_torch"),
            new ElectricTorchItem(
                    ModBlocks.ELECTRIC_TORCH,
                    ModBlocks.ELECTRIC_TORCH_WALl,
                    new Item.Settings(),
                    Direction.UP,
                    EnergyHelper.TORCH_MAX_ENERGY
            ));

    // Internal unlit states used when vanilla torches extinguish in bad air.
    public static final Block UNLIT_TORCH = registerBlock("unlit_torch",
            new TorchBlock(ParticleTypes.SMOKE,
                    Block.Settings.copy(Blocks.TORCH).luminance(state -> 0)) {
                @Override
                public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
                    // Intentionally no flame/smoke particles for extinguished torches.
                }
            },
            false);

    public static final Block UNLIT_WALL_TORCH = registerBlock("unlit_wall_torch",
            new WallTorchBlock(ParticleTypes.SMOKE,
                    Block.Settings.copy(Blocks.WALL_TORCH).luminance(state -> 0)) {
                @Override
                public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
                    // Intentionally no flame/smoke particles for extinguished torches.
                }
            },
            false);

    // Electrical — batteries now store energy
    public static final Block BATTERY = registerBlock("battery",
            new BatteryBlock(EnergyHelper.BATTERY_MAX_ENERGY, EnergyHelper.BATTERY_SOLAR_RATE,
                    Block.Settings.copy(Blocks.STONE)),
            true);

    public static final Block BATTERY_ADVANCED = registerBlock("battery_advanced",
            new AdvancedBatteryBlock(EnergyHelper.BATTERY_ADVANCED_MAX_ENERGY, EnergyHelper.BATTERY_ADVANCED_SOLAR_RATE,
                    Block.Settings.copy(Blocks.STONE)),
            true);

    // Machines
    public static final Block ENVIRONMENTAL_MONITOR = registerBlock("environmental_monitor",
            new EnvironmentalMonitorBlock(Block.Settings.copy(Blocks.STONE).requiresTool()),
            true);
    public static final Block RECYCLER = registerBlock("recycler",
            new RecyclerBlock(Block.Settings.copy(Blocks.STONE).requiresTool()),
            true);
    public static final Block AIR_SCRUBBER = registerBlock("air_scrubber",
            new AirScrubberBlock(Block.Settings.copy(Blocks.STONE).requiresTool()),
            true);
    public static final Block FRIDGE = registerBlock("fridge",
            new FridgeBlock(Block.Settings.copy(Blocks.IRON_BLOCK).requiresTool()),
            true);
    public static final Block TREE_TAP = registerBlock("tree_tap",
            new TreeTapBlock(Block.Settings.copy(Blocks.STONE).requiresTool()),
            true);

    // Dirts
    public static final Block RICH_SOIL = registerBlock("rich_soil",
            new RichSoilBlock(Block.Settings.copy(Blocks.DIRT).ticksRandomly()
                    .allowsSpawning((state, world, pos, type) -> type.getSpawnGroup() != net.minecraft.entity.SpawnGroup.MONSTER)),
            true);
    public static final Block POISONED_SOIL = registerBlock("poisoned_soil",
            new PoisonedSoilBlock(Block.Settings.copy(Blocks.DIRT).ticksRandomly()),
            true);

    private static Block registerBlock(String name, Block block, boolean registerItem) {
        if (registerItem)
            registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(SteamCraft.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Item item;
        if (block instanceof BatteryBlock battery) {
            item = new BatteryBlockItem(block, new Item.Settings(), battery.getMaxEnergy(), battery.getSolarChargeRate());
        } else if (block instanceof FridgeBlock) {
            item = new FridgeBlockItem(block, new Item.Settings());
        } else {
            item = new BlockItem(block, new Item.Settings());
        }
        Registry.register(Registries.ITEM, Identifier.of(SteamCraft.MOD_ID, name), item);
    }

    public static void registerBlocks() {
        SteamCraft.LOGGER.info("Registering Mod Blocks for " + SteamCraft.MOD_ID);
    }
}
