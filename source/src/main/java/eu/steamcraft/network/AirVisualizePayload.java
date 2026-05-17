package eu.steamcraft.network;

import eu.steamcraft.SteamCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record AirVisualizePayload(int ttlTicks, List<CellSample> samples) implements CustomPayload {
    public static final Id<AirVisualizePayload> ID = new Id<>(Identifier.of(SteamCraft.MOD_ID, "air_visualize"));

    public static final PacketCodec<RegistryByteBuf, AirVisualizePayload> CODEC = new PacketCodec<>() {
        @Override
        public AirVisualizePayload decode(RegistryByteBuf buf) {
            int ttlTicks = buf.readVarInt();
            int count = buf.readVarInt();

            List<CellSample> samples = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                samples.add(new CellSample(
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readFloat()
                ));
            }

            return new AirVisualizePayload(ttlTicks, samples);
        }

        @Override
        public void encode(RegistryByteBuf buf, AirVisualizePayload value) {
            buf.writeVarInt(value.ttlTicks);
            buf.writeVarInt(value.samples.size());

            for (CellSample sample : value.samples) {
                buf.writeInt(sample.x());
                buf.writeInt(sample.y());
                buf.writeInt(sample.z());
                buf.writeFloat(sample.co2());
                buf.writeFloat(sample.toxins());
                buf.writeFloat(sample.plastics());
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record CellSample(int x, int y, int z, float co2, float toxins, float plastics) {
    }
}

