package eu.steamcraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FridgeScreen extends HandledScreen<FridgeScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("steamcraft", "textures/gui/fridge.png");

    public FridgeScreen(FridgeScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 222;
        this.playerInventoryTitleX = 8;
        this.playerInventoryTitleY = this.backgroundHeight - 94; // 128 — above player inv
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.titleY = 6;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        boolean on = this.handler.isOn();
        if (on) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } else {
            // Darken the background when the fridge has no power
            RenderSystem.setShaderColor(0.35f, 0.35f, 0.35f, 1f);
        }

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, 176, 222);

        // Reset color
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int textColor = this.handler.isOn() ? 0xFFFFFF : 0xAAAAAA;
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, textColor, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, textColor, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
