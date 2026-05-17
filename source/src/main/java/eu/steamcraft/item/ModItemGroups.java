package eu.steamcraft.item;

import eu.steamcraft.SteamCraft;
import eu.steamcraft.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup STEAMCRAFT_GROUP_ITEMS = Registry.register(Registries.ITEM_GROUP, Identifier.of(SteamCraft.MOD_ID, "steamcraft_items_group_items"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.CIRCUIT_BOARD))
                    .displayName(Text.translatable("itemgroup.steamcraft.steamcraft_items_group_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.CIRCUIT_BOARD);
                        entries.add(ModItems.WIRE);
                        entries.add(ModItems.PLASTIC_SHEET);
                        entries.add(ModItems.RUBBER_SAP);
                        entries.add(ModItems.RUBBER);
                        entries.add(ModItems.BIOMASS);
                        entries.add(ModItems.ROTTEN_FOOD);
                        entries.add(ModItems.FOOD_POUCH);
                        //entries.add(ModItems.ROTTEN_FOOD);
                        entries.add(ModItems.IRON_PLATE);
                        entries.add(ModItems.IRON_ROD);
                        entries.add(ModItems.COPPER_COIL);
                        entries.add(ModItems.COPPER_PLATE);
                        entries.add(ModItems.COPPER_ROD);
                        entries.add(ModItems.STEEL_INGOT);
                        entries.add(ModItems.STEEL_PLATE);
                        entries.add(ModItems.STEEL_ROD);

                        entries.add(ModItems.PLASTIC_AXE);
                        entries.add(ModItems.PLASTIC_PICKAXE);
                        entries.add(ModItems.PLASTIC_SHOVEL);
                        entries.add(ModItems.PLASTIC_HOE);
                        entries.add(ModItems.PLASTIC_SWORD);

                        entries.add(ModItems.STEEL_AXE);
                        entries.add(ModItems.STEEL_PICKAXE);
                        entries.add(ModItems.STEEL_SHOVEL);
                        entries.add(ModItems.STEEL_HOE);
                        entries.add(ModItems.STEEL_SWORD);

                        entries.add(ModItems.PLASTIC_ARMOR_HELMET);
                        entries.add(ModItems.PLASTIC_ARMOR_CHESTPLATE);
                        entries.add(ModItems.PLASTIC_ARMOR_LEGGINGS);
                        entries.add(ModItems.PLASTIC_ARMOR_BOOTS);

                        entries.add(ModItems.HAZMAT_ARMOR_HELMET);
                        entries.add(ModItems.HAZMAT_ARMOR_CHESTPLATE);
                        entries.add(ModItems.HAZMAT_ARMOR_LEGGINGS);
                        entries.add(ModItems.HAZMAT_ARMOR_BOOTS);

                        entries.add(ModItems.PORTABLE_SCANNER);
                        entries.add(ModBlocks.ELECTRIC_TORCH);
                    })
                    .build());

    public static final ItemGroup STEAMCRAFT_GROUP_BLOCKS = Registry.register(Registries.ITEM_GROUP, Identifier.of(SteamCraft.MOD_ID, "steamcraft_items_group_blocks"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.BATTERY))
                    .displayName(Text.translatable("itemgroup.steamcraft.steamcraft_items_group_blocks"))
                    .entries((displayContext, entries) -> {

                        entries.add(ModBlocks.BATTERY);
                        entries.add(ModBlocks.BATTERY_ADVANCED);
                        entries.add(ModBlocks.ENVIRONMENTAL_MONITOR);
                        entries.add(ModBlocks.RECYCLER);
entries.add(ModBlocks.AIR_SCRUBBER);
                        entries.add(ModBlocks.FRIDGE);
                        entries.add(ModBlocks.RICH_SOIL);
                        entries.add(ModBlocks.POISONED_SOIL);
                        entries.add(ModBlocks.TREE_TAP);
                    })
                    .build());


    public static void registerItemGroups() {
        // Item group registration logic can be added here in the future
        SteamCraft.LOGGER.info("Registering Mod Item Groups for " + SteamCraft.MOD_ID);
    }
}
