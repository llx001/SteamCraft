package eu.steamcraft.sound;

import eu.steamcraft.SteamCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent FRIDGE_HUM = register("block.fridge.hum");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(SteamCraft.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        SteamCraft.LOGGER.info("Registering Sounds for " + SteamCraft.MOD_ID);
    }
}

