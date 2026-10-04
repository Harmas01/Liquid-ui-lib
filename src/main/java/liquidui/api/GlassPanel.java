package liquidui.api;

import net.minecraft.client.gui.GuiGraphics;

/** Helpers for drawing non-interactive glass panels in a custom screen. */
public final class GlassPanel {
    private GlassPanel() {
    }

    public static void capture(GuiGraphics graphics) {
        GlassRenderer.capture(graphics);
    }

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height,
                            float opacity) {
        GlassRenderer.draw(graphics, x, y, width, height, opacity, false, true);
    }
}
