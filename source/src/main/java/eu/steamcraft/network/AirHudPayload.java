package eu.steamcraft.network;

import eu.steamcraft.SteamCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AirHudPayload(float co2, float oxygen, float toxins) implements CustomPayload {
    public static final Id<AirHudPayload> ID = new Id<>(Identifier.of(SteamCraft.MOD_ID, "air_hud"));

    public static final PacketCodec<RegistryByteBuf, AirHudPayload> CODEC = new PacketCodec<>() {
        @Override
        public AirHudPayload decode(RegistryByteBuf buf) {
            return new AirHudPayload(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void encode(RegistryByteBuf buf, AirHudPayload value) {
            buf.writeFloat(value.co2());
            buf.writeFloat(value.oxygen());
            buf.writeFloat(value.toxins());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
