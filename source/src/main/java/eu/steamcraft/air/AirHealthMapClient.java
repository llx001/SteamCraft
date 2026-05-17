package eu.steamcraft.air;

import eu.steamcraft.network.AirHealthMapPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;

/**
 * Client receiver + HUD overlay for the Environmental Monitor minimap.
 * Mirrors {@code AirVisualizationClient}'s world-time TTL pattern.
 */
public final class AirHealthMapClient {

    private static final int CELL_PX = 12;
    private static final int GAP_PX = 1;
    private static final int TOP_MARGIN = 24;

    private static int size = 0;
    private static byte[] cells = null;
    private static long expiresAtTick = 0L;

    private AirHealthMapClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AirHealthMapPayload.ID, (payload, context) -> {
            int s = payload.size();
            byte[] c = payload.cells();
            int ttl = payload.ttlTicks();
            context.client().execute(() -> {
                size = s;
                cells = c;
                expiresAtTick = (context.client().world == null || ttl <= 0)
                        ? 0L
                        : context.client().world.getTime() + ttl;
            });
        });

        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> draw(ctx));
    }

    private static void draw(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || cells == null || size <= 0) {
            return;
        }
        if (client.world.getTime() > expiresAtTick) {
            cells = null;
            return;
        }

        int totalW = size * CELL_PX + (size - 1) * GAP_PX;
        int x0 = (ctx.getScaledWindowWidth() - totalW) / 2;
        int y0 = TOP_MARGIN;

        ctx.fill(x0 - 3, y0 - 3, x0 + totalW + 3, y0 + totalW + 3, 0xA0000000);

        for (int gz = 0; gz < size; gz++) {
            for (int gx = 0; gx < size; gx++) {
                int idx = gz * size + gx;
                if (idx >= cells.length) continue;

                int px = x0 + gx * (CELL_PX + GAP_PX);
                int py = y0 + gz * (CELL_PX + GAP_PX);
                ctx.fill(px, py, px + CELL_PX, py + CELL_PX, colorFor(cells[idx] & 0xFF));
            }
        }
    }

    /** Green (clean) -> yellow -> red (harmful). */
    private static int colorFor(int health) {
        float t = health / 255f;
        int r = (int) (MathHelper.clamp(t * 2f, 0f, 1f) * 255f);
        int g = (int) (MathHelper.clamp(2f * (1f - t), 0f, 1f) * 255f);
        return 0xD0000000 | (r << 16) | (g << 8);
    }
}
