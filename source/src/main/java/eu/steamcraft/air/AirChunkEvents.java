package eu.steamcraft.air;

import eu.steamcraft.SteamCraft;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

public final class AirChunkEvents {

    public static void register() {
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world.isClient()) return;

            if(!chunk.hasAttached(AirAttachments.AIR_QUALITY)) {
                chunk.setAttached(AirAttachments.AIR_QUALITY, new AirQualityData());
            }

            AirQualitySystem.registerChunk(world, chunk);
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            if (world.isClient()) return;

            // Safety net: flag the latest in-place mutations for saving before
            // the chunk is serialized on unload (the periodic re-attach in
            // AirQualitySystem may have missed the most recent changes).
            AirQualityData air = chunk.getAttached(AirAttachments.AIR_QUALITY);
            if (air != null && air.isDirty()) {
                chunk.setAttached(AirAttachments.AIR_QUALITY, air);
            }

            AirQualitySystem.unregisterChunk(world, chunk);
        });
    }
}