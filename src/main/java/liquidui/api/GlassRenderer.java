package liquidui.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Liquid-glass primitives implemented with Minecraft 26.2's deferred GUI renderer. */
public final class GlassRenderer {
    private GlassRenderer() {}

    public static void initialize() {
        // Minecraft 26.2 owns GUI pipelines. Panels are submitted as render states below.
    }

    public static void capture(GuiGraphicsExtractor graphics) {
        graphics.blurBeforeThisStratum();
        graphics.nextStratum();
    }

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                            float opacity, boolean highlighted, boolean active) {
        drawPanel(graphics, x, y, w, h, opacity, highlighted, active, false,
                x + w * .5F, y + h * .5F, 0F, 1F);
    }

    public static void drawDanger(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                  float opacity, boolean highlighted) {
        drawPanel(graphics, x, y, w, h, opacity, highlighted, true, true,
                x + w * .5F, y + h * .5F, 0F, 1F);
    }

    public static void drawDangerInteractive(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                             float opacity, boolean highlighted,
                                             float mouseX, float mouseY, float hoverAmount) {
        drawPanel(graphics, x, y, w, h, opacity, highlighted, true, true,
                mouseX, mouseY, hoverAmount, 1F);
    }

    public static void drawInteractive(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                       float opacity, boolean highlighted, boolean active,
                                       float mouseX, float mouseY, float hoverAmount) {
        drawPanel(graphics, x, y, w, h, opacity, highlighted, active, false,
                mouseX, mouseY, hoverAmount, 1F);
    }

    public static void drawRevealingInteractive(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                                float opacity, boolean highlighted, boolean active,
                                                float mouseX, float mouseY, float hoverAmount,
                                                float revealAmount) {
        drawPanel(graphics, x, y, w, h, opacity, highlighted, active, false,
                mouseX, mouseY, hoverAmount, clamp(revealAmount));
    }

    public static void drawIconOverlayInteractive(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                                  float opacity, boolean highlighted,
                                                  float mouseX, float mouseY, float hoverAmount) {
        drawPanel(graphics, x, y, w, h, opacity * .72F, highlighted, true, false,
                mouseX, mouseY, hoverAmount, 1F);
    }

    public static void drawRoundedTextureInteractive(GuiGraphicsExtractor graphics, Identifier texture,
                                                     int x, int y, int w, int h, float opacity,
                                                     float mouseX, float mouseY, float hoverAmount) {
        if (w <= 0 || h <= 0 || opacity <= .01F) return;
        float px = clampSigned((mouseX - x) / Math.max(1F, w) * 2F - 1F) * hoverAmount;
        float py = clampSigned((mouseY - y) / Math.max(1F, h) * 2F - 1F) * hoverAmount;
        int radius = Math.min(18, Math.max(2, Math.round(h * .30F)));
        for (int row = 1; row < h - 1; row++) {
            int inset = roundedInset(row, h, radius) + 1;
            int shiftX = Math.round(px * (1.2F + (row - h * .5F) / Math.max(1F, h) * .7F));
            int shiftY = Math.round(py);
            int rw = w - inset * 2;
            if (rw <= 0) continue;
            float u0 = inset / (float) w;
            float u1 = 1F - u0;
            float v0 = row / (float) h;
            float v1 = (row + 1F) / h;
            graphics.blit(texture, x + inset + shiftX, y + row + shiftY, rw, 1, u0, v0, u1, v1);
        }
    }

    private static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                  float opacity, boolean highlighted, boolean active, boolean danger,
                                  float mouseX, float mouseY, float hoverAmount, float reveal) {
        if (w <= 0 || h <= 0 || opacity <= .01F || reveal <= .001F) return;
        float px = clampSigned((mouseX - x) / Math.max(1F, w) * 2F - 1F) * hoverAmount;
        float py = clampSigned((mouseY - y) / Math.max(1F, h) * 2F - 1F) * hoverAmount;
        int radius = Math.min(18, Math.max(3, Math.round(h * .30F)));
        int baseAlpha = Math.round(255F * clamp(opacity) * (active ? .42F : .25F) * reveal);
        int borderAlpha = Math.round(255F * clamp(opacity) * (highlighted ? .72F : .30F) * reveal);
        int rgb = danger ? 0x7A161B : 0x18202A;
        int borderRgb = danger ? 0xFF6A70 : 0xDCEBFA;

        for (int row = 0; row < h; row++) {
            int inset = roundedInset(row, h, radius);
            int shiftX = Math.round(px * (1.4F + (row - h * .5F) / Math.max(1F, h)));
            int shiftY = Math.round(py);
            int left = x + inset + shiftX;
            int right = x + w - inset + shiftX;
            int yy = y + row + shiftY;
            if (right <= left) continue;
            graphics.fill(left, yy, right, yy + 1, argb(baseAlpha, rgb));
            if (row == 0 || row == h - 1) {
                graphics.fill(left, yy, right, yy + 1, argb(borderAlpha, borderRgb));
            } else {
                graphics.fill(left, yy, left + 1, yy + 1, argb(borderAlpha, borderRgb));
                graphics.fill(right - 1, yy, right, yy + 1, argb(Math.max(1, borderAlpha / 2), 0x05070A));
            }
            if (row < Math.max(1, h / 3)) {
                float shine = 1F - row / (float) Math.max(1, h / 3);
                int center = Math.round((mouseX - x) / Math.max(1F, w) * w);
                int shineWidth = Math.max(8, w / 5);
                int sx0 = Math.max(left + 2, x + center - shineWidth / 2 + shiftX);
                int sx1 = Math.min(right - 2, sx0 + shineWidth);
                if (sx1 > sx0) graphics.fill(sx0, yy, sx1, yy + 1,
                        argb(Math.round(borderAlpha * shine * .20F * hoverAmount), 0xFFFFFF));
            }
        }
    }

    private static int roundedInset(int row, int height, int radius) {
        float cy = row < radius ? radius - row - .5F
                : row >= height - radius ? row - (height - radius) + .5F : 0F;
        if (cy <= 0F) return 0;
        return Math.max(0, (int) Math.ceil(radius - Math.sqrt(Math.max(0F, radius * radius - cy * cy))));
    }

    private static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static float clamp(float value) { return Math.max(0F, Math.min(1F, value)); }
    private static float clampSigned(float value) { return Math.max(-1F, Math.min(1F, value)); }

    public static void release() { SmoothLabel.release(); }
}
