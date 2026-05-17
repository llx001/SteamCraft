package eu.steamcraft.block.entity;

import eu.steamcraft.SteamCraft;
import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.entity.custom.AdvancedBatteryBlockEntity;
import eu.steamcraft.block.entity.custom.AirScrubberBlockEntity;
import eu.steamcraft.block.entity.custom.BatteryBlockEntity;
import eu.steamcraft.block.entity.custom.ElectricTorchBlockEntity;
import eu.steamcraft.block.entity.custom.EnvironmentalMonitorBlockEntity;
import eu.steamcraft.block.entity.custom.FridgeBlockEntity;
import eu.steamcraft.block.entity.custom.RecyclerBlockEntity;
import eu.steamcraft.block.entity.custom.TreeTapBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<TreeTapBlockEntity> TREE_TAP_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "tree_tap_be"),
                    BlockEntityType.Builder.create(TreeTapBlockEntity::new, ModBlocks.TREE_TAP).build(null));

    public static final BlockEntityType<ElectricTorchBlockEntity> ELECTRIC_TORCH_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "electric_torch_be"),
                    BlockEntityType.Builder.create(ElectricTorchBlockEntity::new,
                            ModBlocks.ELECTRIC_TORCH, ModBlocks.ELECTRIC_TORCH_WALl).build(null));

    public static final BlockEntityType<BatteryBlockEntity> BATTERY_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "battery_be"),
                    BlockEntityType.Builder.create(BatteryBlockEntity::new, ModBlocks.BATTERY).build(null));

    public static final BlockEntityType<AdvancedBatteryBlockEntity> ADVANCED_BATTERY_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "advanced_battery_be"),
                    BlockEntityType.Builder.create(AdvancedBatteryBlockEntity::new, ModBlocks.BATTERY_ADVANCED).build(null));

    public static final BlockEntityType<AirScrubberBlockEntity> AIR_SCRUBBER_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "air_scrubber_be"),
                    BlockEntityType.Builder.create(AirScrubberBlockEntity::new, ModBlocks.AIR_SCRUBBER).build(null));

    public static final BlockEntityType<FridgeBlockEntity> FRIDGE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "fridge_be"),
                    BlockEntityType.Builder.create(FridgeBlockEntity::new, ModBlocks.FRIDGE).build(null));

    public static final BlockEntityType<RecyclerBlockEntity> RECYCLER_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "recycler_be"),
                    BlockEntityType.Builder.create(RecyclerBlockEntity::new, ModBlocks.RECYCLER).build(null));

    public static final BlockEntityType<EnvironmentalMonitorBlockEntity> ENVIRONMENTAL_MONITOR_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(SteamCraft.MOD_ID, "environmental_monitor_be"),
                    BlockEntityType.Builder.create(EnvironmentalMonitorBlockEntity::new, ModBlocks.ENVIRONMENTAL_MONITOR).build(null));

    public static void registerAllBlockEntities() {
        SteamCraft.LOGGER.info("Registering Block Entities for " + SteamCraft.MOD_ID);
    }
}
