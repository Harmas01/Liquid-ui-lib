package liquidui.api;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import liquidui.LiquidUiLibrary;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Captures the backdrop once per frame; never reads from the target being drawn to. */
public final class GlassRenderer {
    private static ShaderInstance shader;
    private static TextureTarget backdrop;

    private GlassRenderer() {}

    public static void initialize() {
        // Shader registration is delivered to the Forge mod event bus below.
    }

    @EventBusSubscriber(modid = LiquidUiLibrary.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ShaderEvents {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws java.io.IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    LiquidUiLibrary.id("liquid_glass"), DefaultVertexFormat.POSITION_TEX),
                    loaded -> shader = loaded);
        }
    }

    public static void capture(GuiGraphics graphics) {
        graphics.flush();
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        // Keep the captured backdrop at native framebuffer resolution. A
        // downscaled copy produced visible texel columns on large displays;
        // the shader itself provides the blur without stretching pixels.
        int w = Math.max(1, main.width);
        int h = Math.max(1, main.height);
        if (backdrop == null || backdrop.width != w || backdrop.height != h) {
            if (backdrop != null) backdrop.destroyBuffers();
            backdrop = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            backdrop.setFilterMode(GL11.GL_LINEAR);
        }
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backdrop.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, w, h,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
        main.bindWrite(true);
    }

    public static void draw(GuiGraphics graphics, int x, int y, int w, int h,
                            float opacity, boolean highlighted, boolean active) {
        draw(graphics, x, y, w, h, opacity, highlighted, active, false);
    }

    public static void drawDanger(GuiGraphics graphics, int x, int y, int w, int h,
                                  float opacity, boolean highlighted) {
        draw(graphics, x, y, w, h, opacity, highlighted, true, true,
                x + w * 0.5F, y + h * 0.5F, 0F, false, 1F);
    }

    public static void drawDangerInteractive(GuiGraphics graphics, int x, int y, int w, int h,
                                             float opacity, boolean highlighted,
                                             float mouseX, float mouseY, float hoverAmount) {
        draw(graphics, x, y, w, h, opacity, highlighted, true, true,
                mouseX, mouseY, hoverAmount, false, 1F);
    }

    public static void drawInteractive(GuiGraphics graphics, int x, int y, int w, int h,
                                       float opacity, boolean highlighted, boolean active,
                                       float mouseX, float mouseY, float hoverAmount) {
        draw(graphics, x, y, w, h, opacity, highlighted, active, false,
                mouseX, mouseY, hoverAmount, false, 1F);
    }

    public static void drawRevealingInteractive(GuiGraphics graphics, int x, int y, int w, int h,
                                                float opacity, boolean highlighted, boolean active,
                                                float mouseX, float mouseY, float hoverAmount,
                                                float revealAmount) {
        draw(graphics, x, y, w, h, opacity, highlighted, active, false,
                mouseX, mouseY, hoverAmount, false,
                Math.max(0F, Math.min(1F, revealAmount)));
    }

    public static void drawIconOverlayInteractive(GuiGraphics graphics, int x, int y, int w, int h,
                                                  float opacity, boolean highlighted,
                                                  float mouseX, float mouseY, float hoverAmount) {
        draw(graphics, x, y, w, h, opacity, highlighted, true, false,
                mouseX, mouseY, hoverAmount, true, 1F);
    }

    /** Draws a rounded image using exactly the same lift and pointer tilt as a glass tile. */
    public static void drawRoundedTextureInteractive(GuiGraphics graphics, ResourceLocation texture,
                                                     int x, int y, int w, int h, float opacity,
                                                     float mouseX, float mouseY, float hoverAmount) {
        if (w <= 0 || h <= 0) return;
        graphics.flush();
        ShaderInstance previous = RenderSystem.getShader();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1F, 1F, 1F, opacity);

        float pointerX = Math.max(-1F, Math.min(1F, (mouseX - x) / Math.max(1F, w) * 2F - 1F));
        float pointerY = Math.max(-1F, Math.min(1F, (mouseY - y) / Math.max(1F, h) * 2F - 1F));
        float tiltX = pointerX * hoverAmount;
        float tiltY = pointerY * hoverAmount;
        float visibleV = Math.min(1F, h / (float) w);
        float topV = (1F - visibleV) * 0.5F;
        // Match the exact radius used by liquid_glass.fsh. Keep the image two
        // GUI pixels inside the outline so texture texels cannot leak past it
        // while the plate is tilted or scaled.
        int edgeInset = 2;
        int radius = Math.min(18, Math.round(h * 0.30F));
        int innerRadius = Math.max(1, radius - edgeInset);
        int innerHeight = Math.max(1, h - edgeInset * 2);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int row = edgeInset; row < h - edgeInset; row++) {
            int innerRow = row - edgeInset;
            float cornerY = innerRow < innerRadius ? innerRadius - innerRow - 0.5F
                    : innerRow >= innerHeight - innerRadius
                    ? innerRow - (innerHeight - innerRadius) + 0.5F : 0F;
            int inset = cornerY > 0F
                    ? Math.max(0, (int) Math.ceil(innerRadius
                    - Math.sqrt(innerRadius * innerRadius - cornerY * cornerY))) : 0;
            float left = edgeInset + inset;
            float right = w - edgeInset - inset;
            if (right <= left) continue;
            float u0 = left / w;
            float u1 = right / w;
            float v0 = topV + row * visibleV / h;
            float v1 = topV + (row + 1F) * visibleV / h;
            addTiltedTextureVertex(buffer, matrix, x, y, w, h,
                    left, row + 1F, u0, v1, tiltX, tiltY);
            addTiltedTextureVertex(buffer, matrix, x, y, w, h,
                    right, row + 1F, u1, v1, tiltX, tiltY);
            addTiltedTextureVertex(buffer, matrix, x, y, w, h,
                    right, row, u1, v0, tiltX, tiltY);
            addTiltedTextureVertex(buffer, matrix, x, y, w, h,
                    left, row, u0, v0, tiltX, tiltY);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(() -> previous);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void draw(GuiGraphics graphics, int x, int y, int w, int h,
                             float opacity, boolean highlighted, boolean active, boolean danger) {
        draw(graphics, x, y, w, h, opacity, highlighted, active, danger,
                x + w * 0.5F, y + h * 0.5F, 0F, false, 1F);
    }

    private static void draw(GuiGraphics graphics, int x, int y, int w, int h,
                             float opacity, boolean highlighted, boolean active, boolean danger,
                             float mouseX, float mouseY, float hoverAmount, boolean iconMode,
                             float revealAmount) {
        if (shader == null || backdrop == null) return;
        graphics.flush();
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        ShaderInstance previous = RenderSystem.getShader();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, backdrop.getColorTextureId());
        shader.safeGetUniform("FrameSize").set((float) main.width, (float) main.height);
        shader.safeGetUniform("RectSize").set((float) w, (float) h);
        shader.safeGetUniform("GuiScale").set(scale);
        shader.safeGetUniform("Opacity").set(opacity);
        shader.safeGetUniform("Highlight").set(highlighted ? 1F : 0F);
        shader.safeGetUniform("Enabled").set(active ? 1F : 0F);
        shader.safeGetUniform("Danger").set(danger ? 1F : 0F);
        shader.safeGetUniform("IconMode").set(iconMode ? 1F : 0F);
        shader.safeGetUniform("Pointer").set(mouseX - x, mouseY - y);
        shader.safeGetUniform("Hover").set(hoverAmount);
        shader.safeGetUniform("Reveal").set(revealAmount);
        shader.safeGetUniform("Time").set((Util.getMillis() % 100000L) / 1000F);
        Matrix4f matrix = graphics.pose().last().pose();
        float pad = 5;
        float pointerX = Math.max(-1F, Math.min(1F, (mouseX - x) / Math.max(1F, w) * 2F - 1F));
        float pointerY = Math.max(-1F, Math.min(1F, (mouseY - y) / Math.max(1F, h) * 2F - 1F));
        float tiltX = pointerX * hoverAmount;
        float tiltY = pointerY * hoverAmount;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        addTiltedVertex(buffer, matrix, x, y, w, h, -pad, h + pad, tiltX, tiltY);
        addTiltedVertex(buffer, matrix, x, y, w, h, w + pad, h + pad, tiltX, tiltY);
        addTiltedVertex(buffer, matrix, x, y, w, h, w + pad, -pad, tiltX, tiltY);
        addTiltedVertex(buffer, matrix, x, y, w, h, -pad, -pad, tiltX, tiltY);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShader(() -> previous);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void addTiltedVertex(BufferBuilder buffer, Matrix4f matrix,
                                        int x, int y, int w, int h, float localX, float localY,
                                        float tiltX, float tiltY) {
        float nx = (localX - w * 0.5F) / Math.max(1F, w * 0.5F);
        float ny = (localY - h * 0.5F) / Math.max(1F, h * 0.5F);
        float perspective = 1F + (tiltX * nx + tiltY * ny) * 0.026F;
        float px = x + w * 0.5F + (localX - w * 0.5F) * perspective + tiltX * 1.4F;
        float py = y + h * 0.5F + (localY - h * 0.5F) * perspective + tiltY * 1.0F;
        buffer.addVertex(matrix, px, py, 0).setUv(localX, localY);
    }

    private static void addTiltedTextureVertex(BufferBuilder buffer, Matrix4f matrix,
                                               int x, int y, int w, int h,
                                               float localX, float localY, float u, float v,
                                               float tiltX, float tiltY) {
        float nx = (localX - w * 0.5F) / Math.max(1F, w * 0.5F);
        float ny = (localY - h * 0.5F) / Math.max(1F, h * 0.5F);
        float perspective = 1F + (tiltX * nx + tiltY * ny) * 0.026F;
        float px = x + w * 0.5F + (localX - w * 0.5F) * perspective + tiltX * 1.4F;
        float py = y + h * 0.5F + (localY - h * 0.5F) * perspective + tiltY * 1.0F;
        buffer.addVertex(matrix, px, py, 0).setUv(u, v);
    }

    public static void release() {
        if (backdrop != null) {
            backdrop.destroyBuffers();
            backdrop = null;
        }
        SmoothLabel.release();
    }
}
