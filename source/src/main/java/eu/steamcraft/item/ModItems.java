package eu.steamcraft.item;

import eu.steamcraft.SteamCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item BEEF_JERKY = registerItem("beef_jerky",
            new Item(new Item.Settings()
                    .food(new FoodComponent.Builder()
                            .nutrition(4)
                            .saturationModifier(0.7f)
                            .build())));

    public static final Item MRE = registerItem("mre",
            new Item(new Item.Settings()
                    .food(new FoodComponent.Builder()
                            .nutrition(10)
                            .saturationModifier(2f)
                            .build())));
    public static final Item ROTTEN_FOOD = registerItem("rotten_food",
            new Item(new Item.Settings()));

    public static final Item CIRCUIT_BOARD = registerItem("circuit_board",
            new Item(new Item.Settings()));
    public static final Item WIRE = registerItem("wire",
            new Item(new Item.Settings()));

    public static final Item PLASTIC_SHEET = registerItem("plastic_sheet",
            new Item(new Item.Settings()));

    public static final Item RUBBER_SAP = registerItem("rubber_sap",
            new Item(new Item.Settings()));
    public static final Item RUBBER = registerItem("rubber",
            new Item(new Item.Settings()));
    public static final Item BIOMASS = registerItem("biomass",
            new BiomassItem(new Item.Settings()));

    public static final Item FOOD_POUCH = registerItem("food_pouch",
            new FoodPouchItem(new Item.Settings().maxCount(1)));

    public static final Item IRON_PLATE = registerItem("iron_plate",
            new Item(new Item.Settings()));
    public static final Item IRON_ROD = registerItem("iron_rod",
            new Item(new Item.Settings()));

    public static final Item COPPER_COIL = registerItem("copper_coil",
            new Item(new Item.Settings()));
    public static final Item COPPER_PLATE = registerItem("copper_plate",
            new Item(new Item.Settings()));
    public static final Item COPPER_ROD = registerItem("copper_rod",
            new Item(new Item.Settings()));

    public static final Item STEEL_INGOT = registerItem("steel_ingot",
            new Item(new Item.Settings()));
    public static final Item STEEL_PLATE = registerItem("steel_plate",
            new Item(new Item.Settings()));
    public static final Item STEEL_ROD = registerItem("steel_rod",
            new Item(new Item.Settings()));

    // Tool Items
    public static final Item PLASTIC_AXE = registerItem("plastic_axe",
            new AxeItem(ModToolMaterials.PLASTIC, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.PLASTIC, 4, -3.0f
                    ))));
    public static final Item PLASTIC_PICKAXE = registerItem("plastic_pickaxe",
            new PickaxeItem(ModToolMaterials.PLASTIC, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.PLASTIC, 0, -2.6f
                    ))));
    public static final Item PLASTIC_SHOVEL = registerItem("plastic_shovel",
            new ShovelItem(ModToolMaterials.PLASTIC, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.PLASTIC, 0, -2.8f
                    ))));
    public static final Item PLASTIC_HOE = registerItem("plastic_hoe",
            new HoeItem(ModToolMaterials.PLASTIC, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.PLASTIC, 0, -2.8f
                    ))));
    public static final Item PLASTIC_SWORD = registerItem("plastic_sword",
            new SwordItem(ModToolMaterials.PLASTIC, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.PLASTIC, 2, -2.0f
                    ))));

    public static final Item STEEL_AXE = registerItem("steel_axe",
            new AxeItem(ModToolMaterials.STEEL, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.STEEL, 6, -3.2f
                    ))));
    public static final Item STEEL_PICKAXE = registerItem("steel_pickaxe",
            new PickaxeItem(ModToolMaterials.STEEL, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.STEEL, 1, -2.8f
                    ))));
    public static final Item STEEL_SHOVEL = registerItem("steel_shovel",
            new ShovelItem(ModToolMaterials.STEEL, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.STEEL, 1, -3.0f
                    ))));
    public static final Item STEEL_HOE = registerItem("steel_hoe",
            new HoeItem(ModToolMaterials.STEEL, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.STEEL, 0, -3.0f
                    ))));
    public static final Item STEEL_SWORD = registerItem("steel_sword",
            new SwordItem(ModToolMaterials.STEEL, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.STEEL, 3, -2.4f
                    ))));

    public static final Item PORTABLE_SCANNER = registerItem("portable_scanner",
            new PortableScannerItem(new Item.Settings()));

    // Armor Items
    public static final Item PLASTIC_ARMOR_HELMET = registerItem("plastic_helmet",
            new ArmorItem(ModArmorMaterials.PLASTIC_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(10))));
    public static final Item PLASTIC_ARMOR_CHESTPLATE = registerItem("plastic_chestplate",
            new ArmorItem(ModArmorMaterials.PLASTIC_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(10))));
    public static final Item PLASTIC_ARMOR_LEGGINGS = registerItem("plastic_leggings",
            new ArmorItem(ModArmorMaterials.PLASTIC_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(10))));
    public static final Item PLASTIC_ARMOR_BOOTS = registerItem("plastic_boots",
            new ArmorItem(ModArmorMaterials.PLASTIC_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(10))));

    public static final Item HAZMAT_ARMOR_HELMET = registerItem("hazmat_helmet",
            new ArmorItem(ModArmorMaterials.HAZMAT_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(5))));
    public static final Item HAZMAT_ARMOR_CHESTPLATE = registerItem("hazmat_chestplate",
            new ArmorItem(ModArmorMaterials.HAZMAT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(5))));
    public static final Item HAZMAT_ARMOR_LEGGINGS = registerItem("hazmat_leggings",
            new ArmorItem(ModArmorMaterials.HAZMAT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(5))));
    public static final Item HAZMAT_ARMOR_BOOTS = registerItem("hazmat_boots",
            new ArmorItem(ModArmorMaterials.HAZMAT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(5))));





    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(SteamCraft.MOD_ID, name), item);
    }

    public static void registerItems() {
        // Registration logic for items goes here

        SteamCraft.LOGGER.info("Registering Mod Items for " + SteamCraft.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(BEEF_JERKY);
            entries.add(MRE);
        });


    }

}
