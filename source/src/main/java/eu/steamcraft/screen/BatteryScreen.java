package eu.steamcraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BatteryScreen extends HandledScreen<BatteryScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.of("steamcraft", "textures/gui/battery.png");
    private static final Identifier PROGRESS_TEXTURE =
            Identifier.of("steamcraft", "textures/gui/battery_progress.png");

    private static final int PROGRESS_X = 100;
    private static final int PROGRESS_Y = 31;
    private static final int PROGRESS_W = 5;
    private static final int PROGRESS_H = 24;

    public BatteryScreen(BatteryScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, 176, 166);

        int energy = this.handler.getEnergy();
        int maxEnergy = this.handler.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int filledHeight = (energy * PROGRESS_H) / maxEnergy;
            int srcV = PROGRESS_H - filledHeight;

            context.drawTexture(
                    PROGRESS_TEXTURE,
                    x + PROGRESS_X,
                    y + PROGRESS_Y + srcV,
                    0, srcV,
                    PROGRESS_W, filledHeight,
                    PROGRESS_W, PROGRESS_H
            );
        }

        String text = "Charge: " + energy + " FE";
        context.drawText(this.textRenderer, text, x + 55, y + 60, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
