package eu.steamcraft.air;

import eu.steamcraft.SteamCraft;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public class AirAttachments {

    public static final AttachmentType<AirQualityData> AIR_QUALITY =
            AttachmentRegistry.<AirQualityData>builder()
                    .persistent(AirQualityData.CODEC)
                    .initializer(AirQualityData::new)
                    .buildAndRegister(Identifier.of(SteamCraft.MOD_ID, "air_quality"));

    // Prevent instantiation
    private AirAttachments() {    }
}
