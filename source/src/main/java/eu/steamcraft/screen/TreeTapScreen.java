package eu.steamcraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TreeTapScreen extends HandledScreen<TreeTapScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.of("steamcraft", "textures/gui/tree_tap.png");
    private static final Identifier PROGRESS_TEXTURE =
            Identifier.of("steamcraft", "textures/gui/tree_tap_progress.png");

    // Anchor of progress bar top-left in texture space
    private static final int PROGRESS_X = 100;
    private static final int PROGRESS_Y = 31;
    private static final int PROGRESS_W = 5;
    private static final int PROGRESS_H = 24;

    public TreeTapScreen(TreeTapScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // Centre title horizontally
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Main background (176 × 166 texture)
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, 176, 166);

        // Progress bar — fills from bottom to top
        int progress    = this.handler.getProgress();
        int maxProgress = this.handler.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            int filledHeight = (progress * PROGRESS_H) / maxProgress;
            int srcV         = PROGRESS_H - filledHeight; // skip the unfilled top portion of texture

            context.drawTexture(
                    PROGRESS_TEXTURE,
                    x + PROGRESS_X,
                    y + PROGRESS_Y + srcV,   // screen position rises as fill grows
                    0, srcV,                  // texture UV (take bottom slice)
                    PROGRESS_W, filledHeight, // draw region
                    PROGRESS_W, PROGRESS_H    // full texture size
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

