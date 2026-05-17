package eu.steamcraft.datagen;

import eu.steamcraft.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(
                        ModBlocks.BATTERY,
                        ModBlocks.BATTERY_ADVANCED,
                        ModBlocks.ENVIRONMENTAL_MONITOR,
                        ModBlocks.RECYCLER,
                        ModBlocks.AIR_SCRUBBER,
                        ModBlocks.FRIDGE,
                        ModBlocks.TREE_TAP
                );

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(
                        ModBlocks.BATTERY,
                        ModBlocks.BATTERY_ADVANCED,
                        ModBlocks.ENVIRONMENTAL_MONITOR,
                        ModBlocks.RECYCLER,
                        ModBlocks.AIR_SCRUBBER,
                        ModBlocks.FRIDGE,
                        ModBlocks.RICH_SOIL,
                        ModBlocks.POISONED_SOIL
                );

        getOrCreateTagBuilder(BlockTags.DIRT)
                .add(ModBlocks.RICH_SOIL, ModBlocks.POISONED_SOIL);
    }
}
