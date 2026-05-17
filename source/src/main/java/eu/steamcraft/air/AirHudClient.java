package eu.steamcraft.air;

import eu.steamcraft.network.AirHudPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class AirHudClient {
    public static float co2    = 0f;
    public static float oxygen = 100f;
    public static float toxins = 0f;

    private AirHudClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AirHudPayload.ID, (payload, context) ->
            context.client().execute(() -> {
                co2    = payload.co2();
                oxygen = payload.oxygen();
                toxins = payload.toxins();
            })
        );
    }
}
