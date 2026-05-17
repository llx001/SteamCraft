package eu.steamcraft.datagen;

import eu.steamcraft.SteamCraft;
import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.custom.FridgeBlock;
import eu.steamcraft.block.custom.TreeTapBlock;
import eu.steamcraft.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {

    // cube_column variant whose top and side faces carry tintindex 0 for biome grass colouring.
    private static final Model CUBE_COLUMN_TINTED = new Model(
            Optional.of(Identifier.of(SteamCraft.MOD_ID, "block/cube_column_tinted")),
            Optional.empty(),
            TextureKey.END, TextureKey.SIDE);

    private static final TexturedModel.Factory CUBE_COLUMN_TINTED_FACTORY =
            TexturedModel.makeFactory(
                    block -> new TextureMap()
                            .put(TextureKey.SIDE, TextureMap.getSubId(block, "_side"))
                            .put(TextureKey.END,  TextureMap.getSubId(block, "_top")),
                    CUBE_COLUMN_TINTED);

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }
    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {

        gen.registerTorch(ModBlocks.ELECTRIC_TORCH, ModBlocks.ELECTRIC_TORCH_WALl);
        gen.registerAxisRotated(ModBlocks.BATTERY, TexturedModel.CUBE_COLUMN);
        gen.registerAxisRotated(ModBlocks.BATTERY_ADVANCED, TexturedModel.CUBE_COLUMN);
        gen.registerAxisRotated(ModBlocks.RICH_SOIL, CUBE_COLUMN_TINTED_FACTORY);
        gen.registerAxisRotated(ModBlocks.POISONED_SOIL, CUBE_COLUMN_TINTED_FACTORY);

        gen.registerNorthDefaultHorizontalRotation(ModBlocks.RECYCLER);
        gen.registerNorthDefaultHorizontalRotation(ModBlocks.AIR_SCRUBBER);
        // Fridge: two models (powered on/off) differing only in the front (north) texture
        Identifier fridgeBase = Identifier.of(SteamCraft.MOD_ID, "block/fridge");

        Identifier fridgeModelOff = gen.createSubModel(
                ModBlocks.FRIDGE, "_off", Models.CUBE,
                unused -> new TextureMap()
                        .put(TextureKey.PARTICLE, fridgeBase.withSuffixedPath("_side"))
                        .put(TextureKey.UP,    fridgeBase.withSuffixedPath("_top"))
                        .put(TextureKey.DOWN,  fridgeBase.withSuffixedPath("_bottom"))
                        .put(TextureKey.NORTH, fridgeBase.withSuffixedPath("_front_off"))
                        .put(TextureKey.SOUTH, fridgeBase.withSuffixedPath("_back"))
                        .put(TextureKey.EAST,  fridgeBase.withSuffixedPath("_side"))
                        .put(TextureKey.WEST,  fridgeBase.withSuffixedPath("_side"))
        );

        Identifier fridgeModelOn = gen.createSubModel(
                ModBlocks.FRIDGE, "_on", Models.CUBE,
                unused -> new TextureMap()
                        .put(TextureKey.PARTICLE, fridgeBase.withSuffixedPath("_side"))
                        .put(TextureKey.UP,    fridgeBase.withSuffixedPath("_top"))
                        .put(TextureKey.DOWN,  fridgeBase.withSuffixedPath("_bottom"))
                        .put(TextureKey.NORTH, fridgeBase.withSuffixedPath("_front_on"))
                        .put(TextureKey.SOUTH, fridgeBase.withSuffixedPath("_back"))
                        .put(TextureKey.EAST,  fridgeBase.withSuffixedPath("_side"))
                        .put(TextureKey.WEST,  fridgeBase.withSuffixedPath("_side"))
        );

        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(ModBlocks.FRIDGE)
                        .coordinate(BlockStateModelGenerator.createBooleanModelMap(FridgeBlock.POWERED, fridgeModelOn, fridgeModelOff))
                        .coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates())
        );
        gen.registerParentedItemModel(ModBlocks.FRIDGE, fridgeModelOff);

        registerCubeWithHorizontalFacing(
                gen,
                ModBlocks.ENVIRONMENTAL_MONITOR,
                "_side",     // particle
                "_top",      // up
                "_bottom",   // down
                "_front",    // north
                "_side",     // south
                "_side",     // east
                "_side"      // west
        );

        Identifier treeTapOff = gen.createSubModel(
                ModBlocks.TREE_TAP,
                "",
                Models.CUBE,
                id -> new TextureMap()
                        .put(TextureKey.PARTICLE, id.withSuffixedPath("_side"))
                        .put(TextureKey.UP,    id.withSuffixedPath("_top"))
                        .put(TextureKey.DOWN,  id.withSuffixedPath("_bottom"))
                        .put(TextureKey.NORTH, id.withSuffixedPath("_side"))
                        .put(TextureKey.SOUTH, id.withSuffixedPath("_back"))
                        .put(TextureKey.EAST,  id.withSuffixedPath("_side"))
                        .put(TextureKey.WEST,  id.withSuffixedPath("_side"))
        );

        Identifier treeTapOn = gen.createSubModel(
                ModBlocks.TREE_TAP,
                "_on",
                Models.CUBE,
                unused -> {
                    Identifier baseId = TextureMap.getId(ModBlocks.TREE_TAP);
                    return new TextureMap()
                            .put(TextureKey.PARTICLE, baseId.withSuffixedPath("_side_on"))
                            .put(TextureKey.UP,    baseId.withSuffixedPath("_top"))
                            .put(TextureKey.DOWN,  baseId.withSuffixedPath("_bottom"))
                            .put(TextureKey.NORTH, baseId.withSuffixedPath("_side_on"))
                            .put(TextureKey.SOUTH, baseId.withSuffixedPath("_back"))
                            .put(TextureKey.EAST,  baseId.withSuffixedPath("_side_on"))
                            .put(TextureKey.WEST,  baseId.withSuffixedPath("_side_on"));
                }
        );
        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(ModBlocks.TREE_TAP)
                        // PROCESSING → model swap
                        .coordinate(
                                BlockStateModelGenerator.createBooleanModelMap(
                                        TreeTapBlock.PROCESSING,
                                        treeTapOn,
                                        treeTapOff
                                )
                        )
                        // FACING → rotation
                        .coordinate(
                                BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()
                        )
        );


    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

        // -- Simple Items
        // Food Items
        itemModelGenerator.register(ModItems.BEEF_JERKY, Models.GENERATED);
        itemModelGenerator.register(ModItems.MRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ROTTEN_FOOD, Models.GENERATED);
        // Material Items
        itemModelGenerator.register(ModItems.CIRCUIT_BOARD, Models.GENERATED);
        itemModelGenerator.register(ModItems.WIRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PLASTIC_SHEET, Models.GENERATED);
        itemModelGenerator.register(ModItems.RUBBER_SAP, Models.GENERATED);
        itemModelGenerator.register(ModItems.RUBBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.BIOMASS, Models.GENERATED);
        itemModelGenerator.register(ModItems.FOOD_POUCH, Models.GENERATED);
        itemModelGenerator.register(ModItems.IRON_PLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.IRON_ROD, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_COIL, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_PLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_ROD, Models.GENERATED);
        itemModelGenerator.register(ModItems.STEEL_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.STEEL_PLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.STEEL_ROD, Models.GENERATED);

        // -- Tool Items
        // Plastic Tools
        itemModelGenerator.register(ModItems.PLASTIC_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PLASTIC_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PLASTIC_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PLASTIC_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PLASTIC_SWORD, Models.HANDHELD);
        // Steel Tools
        itemModelGenerator.register(ModItems.STEEL_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STEEL_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STEEL_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STEEL_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STEEL_SWORD, Models.HANDHELD);
        // Special Items
        itemModelGenerator.register(ModItems.PORTABLE_SCANNER, Models.HANDHELD);

        // -- Armor Items
        // Plastic Armor
        itemModelGenerator.registerArmor((ArmorItem) ModItems.PLASTIC_ARMOR_HELMET);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.PLASTIC_ARMOR_CHESTPLATE);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.PLASTIC_ARMOR_LEGGINGS);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.PLASTIC_ARMOR_BOOTS);
        // Hazmat Armor
        itemModelGenerator.registerArmor((ArmorItem) ModItems.HAZMAT_ARMOR_HELMET);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.HAZMAT_ARMOR_CHESTPLATE);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.HAZMAT_ARMOR_LEGGINGS);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.HAZMAT_ARMOR_BOOTS);
    }

    public static void registerCubeWithHorizontalFacing(
            BlockStateModelGenerator gen,
            Block block,
            String particle,
            String top,
            String bottom,
            String north,
            String south,
            String east,
            String west
    ) {

        Identifier model = gen.createSubModel(
                block,
                "",
                Models.CUBE,
                unused -> new TextureMap()
                        .put(TextureKey.PARTICLE, unused.withSuffixedPath(particle))
                        .put(TextureKey.UP, unused.withSuffixedPath(top))
                        .put(TextureKey.DOWN, unused.withSuffixedPath(bottom))
                        .put(TextureKey.NORTH, unused.withSuffixedPath(north))
                        .put(TextureKey.SOUTH, unused.withSuffixedPath(south))
                        .put(TextureKey.EAST, unused.withSuffixedPath(east))
                        .put(TextureKey.WEST, unused.withSuffixedPath(west))
        );

        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(
                        block,
                        BlockStateVariant.create().put(VariantSettings.MODEL, model)
                ).coordinate(
                        BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()
                )
        );
    }

    public static void registerCubeAllWithHorizontalFacing(
            BlockStateModelGenerator gen,
            Block block,
            Identifier texture
    ) {
        Identifier model = gen.createSubModel(
                block,
                "",
                Models.CUBE_ALL,
                unused -> new TextureMap().put(TextureKey.ALL, texture)
        );

        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(
                        block,
                        BlockStateVariant.create().put(VariantSettings.MODEL, model)
                ).coordinate(
                        BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()
                )
        );
    }


}
