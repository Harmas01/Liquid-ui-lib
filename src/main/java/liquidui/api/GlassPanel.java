package liquidui.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Helpers for drawing non-interactive glass panels in a custom screen. */
public final class GlassPanel {
    private GlassPanel() {
    }

    public static void capture(GuiGraphicsExtractor graphics) {
        GlassRenderer.capture(graphics);
    }

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                            float opacity) {
        GlassRenderer.draw(graphics, x, y, width, height, opacity, false, true);
    }
}
