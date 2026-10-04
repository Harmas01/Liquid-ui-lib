package liquidui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Draws captions with Minecraft's vanilla font and an optional watery hover blur. */
public final class SmoothLabel {
    private SmoothLabel() {}

    public static void draw(GuiGraphics graphics, String text, int x, int y, int width, int height,
                            float alpha, boolean active) {
        draw(graphics, text, x, y, width, height, alpha, active, 0F);
    }

    public static void draw(GuiGraphics graphics, String text, int x, int y, int width, int height,
                            float alpha, boolean active, float blurAmount) {
        if (alpha <= 0.01F) return;
        Font font = Minecraft.getInstance().font;
        String fitted = fit(font, text, Math.max(8, width - 16));
        int textWidth = font.width(fitted);
        int drawX = x + (width - textWidth) / 2;
        int drawY = y + (height - font.lineHeight) / 2;
        float blur = Math.max(0F, Math.min(1F, blurAmount));

        if (blur > 0.01F) {
            int radius = blur > 0.55F ? 2 : 1;
            int glowAlpha = Math.max(1, Math.min(255, Math.round(alpha * blur * 42F)));
            int glow = (glowAlpha << 24) | 0xDDF5FF;
            for (int oy = -radius; oy <= radius; oy++) {
                for (int ox = -radius; ox <= radius; ox++) {
                    if (ox == 0 && oy == 0) continue;
                    if (ox * ox + oy * oy > radius * radius + 1) continue;
                    graphics.drawString(font, fitted, drawX + ox, drawY + oy, glow, false);
                }
            }
        }

        float brightness = active ? 1F : 0.58F;
        int channel = Math.round(brightness * 255F);
        int textAlpha = Math.max(1, Math.min(255, Math.round(alpha * (1F - blur * 0.08F) * 255F)));
        int color = (textAlpha << 24) | (channel << 16) | (channel << 8) | channel;
        graphics.drawString(font, fitted, drawX, drawY, color, true);
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        return font.plainSubstrByWidth(text, Math.max(1, maxWidth - font.width(ellipsis))) + ellipsis;
    }

    public static void release() {
        // Vanilla owns and releases its font textures.
    }
}
