package eu.steamcraft.datagen;

import eu.steamcraft.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {


    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.MEAT).add(
                ModItems.BEEF_JERKY,
                ModItems.MRE
        );

        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR).add(
                ModItems.PLASTIC_ARMOR_HELMET,
                ModItems.PLASTIC_ARMOR_CHESTPLATE,
                ModItems.PLASTIC_ARMOR_LEGGINGS,
                ModItems.PLASTIC_ARMOR_BOOTS,
                ModItems.HAZMAT_ARMOR_HELMET,
                ModItems.HAZMAT_ARMOR_CHESTPLATE,
                ModItems.HAZMAT_ARMOR_LEGGINGS,
                ModItems.HAZMAT_ARMOR_BOOTS
        );
    }
}
