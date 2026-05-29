package eu.steamcraft.datagen;

import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        genFoodRecipes(exporter);
        genIronRecipes(exporter);
        genSteelRecipes(exporter);
        genCopperRecipes(exporter);
        genPlasticRecipes(exporter);
        genHazmatRecipes(exporter);
        genElectricRecipes(exporter);
        genMachineRecipes(exporter);
    }

    // ── Food ────────────────────────────────────────────────────────────────

    private void genFoodRecipes(RecipeExporter exporter) {
        offerSmelting(exporter, List.of(Items.COOKED_BEEF),
                RecipeCategory.MISC, ModItems.BEEF_JERKY, 0.35f, 200, "beef_jerky");

        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, ModItems.MRE)
                .pattern(" B ")
                .pattern("JPJ")
                .pattern(" B ")
                .input('B', Items.BREAD)
                .input('J', ModItems.BEEF_JERKY)
                .input('P', Items.PAPER)
                .criterion(hasItem(ModItems.BEEF_JERKY), conditionsFromItem(ModItems.BEEF_JERKY))
                .offerTo(exporter);

        // Food pouch — leather bag with rubber seal and plastic lining
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.FOOD_POUCH)
                .pattern("LLL")
                .pattern("RPR")
                .pattern("LLL")
                .input('L', Items.LEATHER)
                .input('R', ModItems.RUBBER)
                .input('P', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.RUBBER), conditionsFromItem(ModItems.RUBBER))
                .offerTo(exporter);
    }

    // ── Iron ────────────────────────────────────────────────────────────────

    private void genIronRecipes(RecipeExporter exporter) {
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.IRON_PLATE, Items.IRON_INGOT, 2);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.IRON_ROD,   Items.IRON_INGOT, 4);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.IRON_ROD,   ModItems.IRON_PLATE, 2);
    }

    // ── Steel ───────────────────────────────────────────────────────────────

    private void genSteelRecipes(RecipeExporter exporter) {
        offerBlasting(exporter, List.of(Items.IRON_INGOT),
                RecipeCategory.MISC, ModItems.STEEL_INGOT, 0.7f, 400, "steel_ingot");

        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.STEEL_PLATE, ModItems.STEEL_INGOT, 2);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.STEEL_ROD,   ModItems.STEEL_INGOT, 4);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.STEEL_ROD,   ModItems.STEEL_PLATE, 2);

        SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_PICKAXE), Ingredient.ofItems(ModItems.STEEL_PLATE), RecipeCategory.COMBAT, ModItems.STEEL_PICKAXE)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter, "steel_pickaxe_from_smithing");
        SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_AXE),     Ingredient.ofItems(ModItems.STEEL_PLATE), RecipeCategory.COMBAT, ModItems.STEEL_AXE)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter, "steel_axe_from_smithing");
        SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_SHOVEL),  Ingredient.ofItems(ModItems.STEEL_PLATE), RecipeCategory.COMBAT, ModItems.STEEL_SHOVEL)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter, "steel_shovel_from_smithing");
        SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_HOE),     Ingredient.ofItems(ModItems.STEEL_PLATE), RecipeCategory.COMBAT, ModItems.STEEL_HOE)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter, "steel_hoe_from_smithing");
        SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(Items.IRON_SWORD),   Ingredient.ofItems(ModItems.STEEL_PLATE), RecipeCategory.COMBAT, ModItems.STEEL_SWORD)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter, "steel_sword_from_smithing");
    }

    // ── Copper & electronics ────────────────────────────────────────────────

    private void genCopperRecipes(RecipeExporter exporter) {
        // Basic copper stock — same pattern as iron
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.COPPER_PLATE, Items.COPPER_INGOT, 2);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.COPPER_ROD,   Items.COPPER_INGOT, 4);
        offerStonecuttingRecipe(exporter, RecipeCategory.MISC, ModItems.COPPER_ROD,   ModItems.COPPER_PLATE, 2);

        // Wire — pull a copper rod thin
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.WIRE, 2)
                .input(ModItems.COPPER_ROD)
                .criterion(hasItem(ModItems.COPPER_ROD), conditionsFromItem(ModItems.COPPER_ROD))
                .offerTo(exporter);

        // Copper coil — four wires wound around an iron-rod core
        // Layout:
        //   _ W _
        //   W I W
        //   _ W _
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.COPPER_COIL)
                .pattern(" W ")
                .pattern("WIW")
                .pattern(" W ")
                .input('W', ModItems.WIRE)
                .input('I', ModItems.IRON_ROD)
                .criterion(hasItem(ModItems.WIRE), conditionsFromItem(ModItems.WIRE))
                .offerTo(exporter);

        // Circuit board — copper traces on a plastic substrate, bonded with gold
        // Layout:
        //   C G C
        //   I P I
        //   C G C
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CIRCUIT_BOARD)
                .pattern("CGC")
                .pattern("IPI")
                .pattern("CGC")
                .input('C', ModItems.COPPER_PLATE)
                .input('G', Items.GOLD_NUGGET)
                .input('I', ModItems.IRON_PLATE)
                .input('P', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.COPPER_PLATE), conditionsFromItem(ModItems.COPPER_PLATE))
                .offerTo(exporter);
    }

    // ── Rubber & plastic ────────────────────────────────────────────────────

    private void genPlasticRecipes(RecipeExporter exporter) {
        offerSmelting(exporter,  List.of(ModItems.RUBBER_SAP), RecipeCategory.MISC, ModItems.RUBBER,        0.1f, 200, "rubber_from_smelting_rubber_sap");
        offerBlasting(exporter,  List.of(ModItems.RUBBER_SAP), RecipeCategory.MISC, ModItems.RUBBER,        0.1f, 100, "rubber_from_blasting_rubber_sap");
        offerSmelting(exporter,  List.of(ModItems.RUBBER),     RecipeCategory.MISC, ModItems.PLASTIC_SHEET, 0.1f, 200, "plastic_sheet_from_smelting");
        offerBlasting(exporter,  List.of(ModItems.RUBBER),     RecipeCategory.MISC, ModItems.PLASTIC_SHEET, 0.1f, 100, "plastic_sheet_from_blasting");

        // Plastic tools
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_PICKAXE)
                .pattern("lll").pattern(" s ").pattern(" s ")
                .input('s', Items.STICK).input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_AXE)
                .pattern("ll ").pattern("ls ").pattern(" s ")
                .input('s', Items.STICK).input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_SHOVEL)
                .pattern(" l ").pattern(" s ").pattern(" s ")
                .input('s', Items.STICK).input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_HOE)
                .pattern("ll ").pattern(" s ").pattern(" s ")
                .input('s', Items.STICK).input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_SWORD)
                .pattern(" l ").pattern(" l ").pattern(" s ")
                .input('s', Items.STICK).input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);

        // Plastic armor
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_ARMOR_HELMET)
                .pattern("lll").pattern("l l")
                .input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_ARMOR_CHESTPLATE)
                .pattern("l l").pattern("lll").pattern("lll")
                .input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_ARMOR_LEGGINGS)
                .pattern("lll").pattern("l l").pattern("l l")
                .input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PLASTIC_ARMOR_BOOTS)
                .pattern("l l").pattern("l l")
                .input('l', ModItems.PLASTIC_SHEET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
    }

    // ── Hazmat ──────────────────────────────────────────────────────────────

    private void genHazmatRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.HAZMAT_ARMOR_HELMET)
                .pattern("ppp").pattern("rxr").pattern("ppp")
                .input('p', ModItems.PLASTIC_SHEET).input('r', ModItems.RUBBER).input('x', Items.LEATHER_HELMET)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.HAZMAT_ARMOR_CHESTPLATE)
                .pattern("r r").pattern("pxp").pattern("ppp")
                .input('p', ModItems.PLASTIC_SHEET).input('r', ModItems.RUBBER).input('x', Items.LEATHER_CHESTPLATE)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.HAZMAT_ARMOR_LEGGINGS)
                .pattern("ppp").pattern("rxr").pattern("p p")
                .input('p', ModItems.PLASTIC_SHEET).input('r', ModItems.RUBBER).input('x', Items.LEATHER_LEGGINGS)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.HAZMAT_ARMOR_BOOTS)
                .pattern("p p").pattern("rxr").pattern("p p")
                .input('p', ModItems.PLASTIC_SHEET).input('r', ModItems.RUBBER).input('x', Items.LEATHER_BOOTS)
                .criterion(hasItem(ModItems.PLASTIC_SHEET), conditionsFromItem(ModItems.PLASTIC_SHEET))
                .offerTo(exporter);
    }

    // ── Electrical items ─────────────────────────────────────────────────────

    private void genElectricRecipes(RecipeExporter exporter) {
        // Electric torch — wire filament, glass bulb, iron-rod stem → 2 torches
        // Layout:
        //   _ W _
        //   _ G _
        //   _ I _
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ELECTRIC_TORCH_ITEM, 2)
                .pattern(" W ")
                .pattern(" G ")
                .pattern(" I ")
                .input('W', ModItems.WIRE)
                .input('G', Items.GLASS)
                .input('I', ModItems.IRON_ROD)
                .criterion(hasItem(ModItems.WIRE), conditionsFromItem(ModItems.WIRE))
                .offerTo(exporter);

        // Battery — iron shell, rubber insulation, copper-coil core, wire terminal
        // Layout:
        //   I R I
        //   C W C
        //   I R I
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.BATTERY)
                .pattern("IRI")
                .pattern("CWC")
                .pattern("IRI")
                .input('I', ModItems.IRON_PLATE)
                .input('R', ModItems.RUBBER)
                .input('C', ModItems.COPPER_COIL)
                .input('W', ModItems.WIRE)
                .criterion(hasItem(ModItems.COPPER_COIL), conditionsFromItem(ModItems.COPPER_COIL))
                .offerTo(exporter);

        // Advanced battery — steel-reinforced upgrade around a basic battery,
        // with extra coils and circuit boards for higher capacity
        // Layout:
        //   S C S
        //   c B c
        //   S C S
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.BATTERY_ADVANCED)
                .pattern("SCS")
                .pattern("cBc")
                .pattern("SCS")
                .input('S', ModItems.STEEL_PLATE)
                .input('C', ModItems.COPPER_COIL)
                .input('c', ModItems.CIRCUIT_BOARD)
                .input('B', ModBlocks.BATTERY)
                .criterion(hasItem(ModItems.CIRCUIT_BOARD), conditionsFromItem(ModItems.CIRCUIT_BOARD))
                .offerTo(exporter);

        // Portable scanner — circuit board with a copper sensor and rubber grip
        // Layout:
        //   _ C _
        //   I B I
        //   _ R _
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PORTABLE_SCANNER)
                .pattern(" C ")
                .pattern("IBI")
                .pattern(" R ")
                .input('C', ModItems.CIRCUIT_BOARD)
                .input('I', ModItems.IRON_ROD)
                .input('B', ModItems.COPPER_PLATE)
                .input('R', ModItems.RUBBER)
                .criterion(hasItem(ModItems.CIRCUIT_BOARD), conditionsFromItem(ModItems.CIRCUIT_BOARD))
                .offerTo(exporter);
    }

    // ── Machines ─────────────────────────────────────────────────────────────

    private void genMachineRecipes(RecipeExporter exporter) {
        // Tree tap — rubber-tipped iron tap with an iron-rod spike
        // Layout:
        //   _ R _
        //   I i I
        //   _ R _
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.TREE_TAP)
                .pattern(" R ")
                .pattern("IiI")
                .pattern(" R ")
                .input('R', Items.REDSTONE)
                .input('I', ModItems.IRON_PLATE)
                .input('i', ModItems.IRON_ROD)
                .criterion(hasItem(ModItems.IRON_PLATE), conditionsFromItem(ModItems.IRON_PLATE))
                .offerTo(exporter);

        // Environmental monitor — glass sensor panels in an iron frame, circuit board inside
        // Layout:
        //   G I G
        //   I C I
        //   G I G
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENVIRONMENTAL_MONITOR)
                .pattern("GIG")
                .pattern("ICI")
                .pattern("GIG")
                .input('G', Items.GLASS)
                .input('I', ModItems.IRON_PLATE)
                .input('C', ModItems.CIRCUIT_BOARD)
                .criterion(hasItem(ModItems.CIRCUIT_BOARD), conditionsFromItem(ModItems.CIRCUIT_BOARD))
                .offerTo(exporter);

        // Recycler — steel body, iron structural plates, rubber gaskets, circuit board control
        // Layout:
        //   S I S
        //   R C R
        //   S I S
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.RECYCLER)
                .pattern("SIS")
                .pattern("RCR")
                .pattern("SIS")
                .input('S', ModItems.STEEL_INGOT)
                .input('I', ModItems.IRON_PLATE)
                .input('R', ModItems.RUBBER)
                .input('C', ModItems.CIRCUIT_BOARD)
                .criterion(hasItem(ModItems.CIRCUIT_BOARD), conditionsFromItem(ModItems.CIRCUIT_BOARD))
                .offerTo(exporter);

        // Air scrubber — rubber filter media, copper-coil ioniser, plastic baffles, iron-rod frame
        // Layout:
        //   R C R
        //   P i P
        //   R C R
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.AIR_SCRUBBER)
                .pattern("RCR")
                .pattern("PiP")
                .pattern("RCR")
                .input('R', ModItems.RUBBER)
                .input('C', ModItems.COPPER_COIL)
                .input('P', ModItems.PLASTIC_SHEET)
                .input('i', ModItems.IRON_ROD)
                .criterion(hasItem(ModItems.COPPER_COIL), conditionsFromItem(ModItems.COPPER_COIL))
                .offerTo(exporter);

        // Fridge — insulated iron cabinet with an integrated battery-backed cooling core
        // Layout:
        //   I I I
        //   I B I
        //   I C I
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.FRIDGE)
                .pattern("III")
                .pattern("IBI")
                .pattern("ICI")
                .input('I', ModItems.IRON_PLATE)
                .input('B', ModBlocks.BATTERY)
                .input('C', ModItems.CIRCUIT_BOARD)
                .criterion(hasItem(ModBlocks.BATTERY), conditionsFromItem(ModBlocks.BATTERY))
                .offerTo(exporter);
    }
}
