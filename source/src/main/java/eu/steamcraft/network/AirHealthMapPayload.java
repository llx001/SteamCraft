package eu.steamcraft.network;

import eu.steamcraft.SteamCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server -> client snapshot for the Environmental Monitor minimap.
 * {@code cells} is a {@code size * size} row-major grid of health bytes
 * (0 = clean, 255 = at/above the worst effect threshold).
 */
public record AirHealthMapPayload(int size, int ttlTicks, byte[] cells) implements CustomPayload {
    public static final Id<AirHealthMapPayload> ID =
            new Id<>(Identifier.of(SteamCraft.MOD_ID, "air_health_map"));

    public static final PacketCodec<RegistryByteBuf, AirHealthMapPayload> CODEC = new PacketCodec<>() {
        @Override
        public AirHealthMapPayload decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            int ttl = buf.readVarInt();
            byte[] cells = buf.readByteArray();
            return new AirHealthMapPayload(size, ttl, cells);
        }

        @Override
        public void encode(RegistryByteBuf buf, AirHealthMapPayload value) {
            buf.writeVarInt(value.size);
            buf.writeVarInt(value.ttlTicks);
            buf.writeByteArray(value.cells);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
