package liquidui.api;

import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Minimal black-dot loading animation for startup and world transitions. */
public final class DotLoadingRenderer {
    private DotLoadingRenderer() {
    }

    public static void drawFullScreen(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(0, 0, width, height, 0xFFFFFFFF);
        drawDots(graphics, width, height);
    }

    public static void drawDots(GuiGraphicsExtractor graphics, int width, int height) {
        double time = Util.getMillis() / 260.0;
        int centerX = width / 2;
        int centerY = height / 2;
        int count = 7;
        int spacing = 14;
        for (int i = 0; i < count; i++) {
            double wave = Math.sin(time - i * 0.62);
            double pulse = (wave + 1.0) * 0.5;
            int radius = 2 + (int) Math.round(pulse * 2.0);
            int x = centerX + (i - count / 2) * spacing;
            int y = centerY - (int) Math.round(pulse * 9.0);
            drawCircle(graphics, x, y, radius, 0xFF080808);
        }
    }

    private static void drawCircle(GuiGraphicsExtractor graphics, int centerX, int centerY,
                                   int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.floor(Math.sqrt(radius * radius - y * y));
            graphics.fill(centerX - halfWidth, centerY + y,
                    centerX + halfWidth + 1, centerY + y + 1, color);
        }
    }
}
