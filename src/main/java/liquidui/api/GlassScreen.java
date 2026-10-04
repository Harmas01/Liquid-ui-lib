package liquidui.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Base for extension screens. Background capture always precedes glass widgets. */
public abstract class GlassScreen extends Screen {
    private final Screen parent;

    protected GlassScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    /** Override to draw your background; do not draw glass controls here. */
    protected void renderGlassBackground(GuiGraphics graphics, int mouseX, int mouseY,
                                         float partialTick) {
        renderBackground(graphics);
    }

    /** Override for labels and decorations drawn above the background. */
    protected void renderGlassContent(GuiGraphics graphics, int mouseX, int mouseY,
                                      float partialTick) {
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
    }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderGlassBackground(graphics, mouseX, mouseY, partialTick);
        GlassRenderer.capture(graphics);
        renderGlassContent(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
