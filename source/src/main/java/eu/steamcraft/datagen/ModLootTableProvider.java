package eu.steamcraft.datagen;

import eu.steamcraft.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.ELECTRIC_TORCH_WALl);
        addDrop(ModBlocks.ELECTRIC_TORCH);
        addDrop(ModBlocks.UNLIT_TORCH, Items.TORCH);
        addDrop(ModBlocks.UNLIT_WALL_TORCH, Items.TORCH);
        addDrop(ModBlocks.BATTERY);
        addDrop(ModBlocks.BATTERY_ADVANCED);
        addDrop(ModBlocks.ENVIRONMENTAL_MONITOR);
        addDrop(ModBlocks.RECYCLER);
        addDrop(ModBlocks.AIR_SCRUBBER);
        addDrop(ModBlocks.FRIDGE);
        addDrop(ModBlocks.RICH_SOIL);
        addDrop(ModBlocks.POISONED_SOIL);
        addDrop(ModBlocks.TREE_TAP);
    }
}
