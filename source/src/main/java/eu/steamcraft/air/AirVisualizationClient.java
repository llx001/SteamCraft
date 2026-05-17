package eu.steamcraft.air;

import eu.steamcraft.network.AirVisualizePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class AirVisualizationClient {
    private static final int CELL_SIZE = 4;

    private static List<CellSample> samples = List.of();
    private static long expiresAtTick = 0L;

    private AirVisualizationClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AirVisualizePayload.ID, (payload, context) -> {
            List<CellSample> nextSamples = new ArrayList<>(payload.samples().size());
            for (AirVisualizePayload.CellSample sample : payload.samples()) {
                nextSamples.add(new CellSample(sample.x(), sample.y(), sample.z(), sample.co2(), sample.toxins(), sample.plastics()));
            }

            context.client().execute(() -> {
                samples = nextSamples;
                if (context.client().world == null || payload.ttlTicks() <= 0) {
                    expiresAtTick = 0L;
                } else {
                    expiresAtTick = context.client().world.getTime() + payload.ttlTicks();
                }
            });
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || samples.isEmpty()) {
                return;
            }

            if (client.world.getTime() > expiresAtTick) {
                samples = List.of();
                return;
            }

            Vec3d camPos = context.camera().getPos();
            MatrixStack matrices = context.matrixStack();
            Matrix4f posMatrix = matrices.peek().getPositionMatrix();

            // --- Translucent filled boxes via Tessellator (avoids VertexConsumerProvider issues) ---
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            com.mojang.blaze3d.systems.RenderSystem.disableCull();
            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
            com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            Tessellator tessellator = Tessellator.getInstance();
            var buf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            for (CellSample sample : samples) {
                float[] color = computeColor(sample.co2, sample.toxins, sample.plastics);
                float alpha = MathHelper.clamp(color[3] * 0.35f, 0.06f, 0.30f);

                float x1 = (float) (sample.x - camPos.x);
                float y1 = (float) (sample.y - camPos.y);
                float z1 = (float) (sample.z - camPos.z);
                float x2 = x1 + CELL_SIZE;
                float y2 = y1 + CELL_SIZE;
                float z2 = z1 + CELL_SIZE;
                float r = color[0], g = color[1], b = color[2];

                // -Y
                buf.vertex(posMatrix, x1, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y1, z1).color(r, g, b, alpha);
                // +Y
                buf.vertex(posMatrix, x1, y2, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y2, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y2, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y2, z2).color(r, g, b, alpha);
                // -Z
                buf.vertex(posMatrix, x1, y2, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y1, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y2, z1).color(r, g, b, alpha);
                // +Z
                buf.vertex(posMatrix, x2, y2, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y2, z2).color(r, g, b, alpha);
                // -X
                buf.vertex(posMatrix, x1, y2, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y1, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x1, y2, z1).color(r, g, b, alpha);
                // +X
                buf.vertex(posMatrix, x2, y2, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z1).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y1, z2).color(r, g, b, alpha);
                buf.vertex(posMatrix, x2, y2, z2).color(r, g, b, alpha);
            }

            BufferRenderer.drawWithGlobalProgram(buf.end());

            com.mojang.blaze3d.systems.RenderSystem.enableCull();
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();

            // --- Wireframe outlines via context consumer (no topology mismatch here) ---
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());

            for (CellSample sample : samples) {
                float[] color = computeColor(sample.co2, sample.toxins, sample.plastics);

                double minX = sample.x - camPos.x;
                double minY = sample.y - camPos.y;
                double minZ = sample.z - camPos.z;

                WorldRenderer.drawBox(
                        matrices,
                        lines,
                        minX,
                        minY,
                        minZ,
                        minX + CELL_SIZE,
                        minY + CELL_SIZE,
                        minZ + CELL_SIZE,
                        color[0],
                        color[1],
                        color[2],
                        MathHelper.clamp(color[3] * 0.85f, 0.2f, 0.9f)
                );
            }
        });
    }

    private static float[] computeColor(float co2, float toxins, float plastics) {
        float co2N = MathHelper.clamp(co2 / 100.0f, 0.0f, 1.0f);
        float toxinsN = MathHelper.clamp(toxins / 100.0f, 0.0f, 1.0f);
        float plasticsN = MathHelper.clamp(plastics / 100.0f, 0.0f, 1.0f);

        float total = Math.max(co2N, Math.max(toxinsN, plasticsN));
        float alpha = MathHelper.clamp(0.1f + total * 0.75f, 0.15f, 0.85f);

        // CO2 -> red, toxins -> green, microplastics -> blue.
        float r = MathHelper.clamp(co2N + toxinsN * 0.2f, 0.0f, 1.0f);
        float g = MathHelper.clamp(toxinsN + co2N * 0.1f, 0.0f, 1.0f);
        float b = MathHelper.clamp(plasticsN + toxinsN * 0.15f, 0.0f, 1.0f);

        return new float[]{r, g, b, alpha};
    }

    private record CellSample(int x, int y, int z, float co2, float toxins, float plastics) {
    }
}

