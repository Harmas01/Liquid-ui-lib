package liquidui.api;

import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A rotating particle vortex used while the current world is being closed. */
public final class ExitLoadingRenderer {
    private ExitLoadingRenderer() {
    }

    public static void draw(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(0, 0, width, height, 0xFFFFFFFF);

        double time = Util.getMillis() / 520.0;
        int centerX = width / 2;
        int centerY = height / 2;
        int dotCount = 14;
        for (int i = 0; i < dotCount; i++) {
            double progress = i / (double) dotCount;
            double angle = time + progress * Math.PI * 2.0;
            double wave = (Math.sin(time * 1.45 - i * 0.48) + 1.0) * 0.5;
            double radius = 19.0 + wave * 12.0;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius * 0.62);
            int dotRadius = 2 + (int) Math.round(wave * 2.0);
            drawCircle(graphics, x, y, dotRadius, 0xFF080808);
        }

        int centerRadius = 3 + (int) Math.round((Math.sin(time * 2.1) + 1.0) * 1.5);
        drawCircle(graphics, centerX, centerY, centerRadius, 0xFF080808);
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
