package liquidui.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Standalone liquid-glass button for custom Fabric screens. */
public final class GlassButton extends Button {
    private final LiquidTheme theme;
    private float hoverAmount;
    private float focusAmount;

    private GlassButton(int x, int y, int width, int height, Component message,
                        OnPress onPress, LiquidTheme theme) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.theme = theme;
    }

    public static Builder glassBuilder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float hoverTarget = active && isHovered() ? 1F : 0F;
        hoverAmount += (hoverTarget - hoverAmount) * theme.animationSpeed();
        float focusTarget = active && isFocused() && !isHovered() ? 1F : 0F;
        focusAmount += (focusTarget - focusAmount) * theme.animationSpeed();

        float hoverEase = easeOut(hoverAmount);
        int grow = Math.round(easeOut(focusAmount) * theme.focusGrowth());
        int drawX = getX() - grow;
        int drawY = getY() - Math.round(hoverEase * theme.hoverLift()) - grow;
        int drawWidth = width + grow * 2;
        int drawHeight = height + grow * 2;
        GlassRenderer.drawInteractive(graphics, drawX, drawY, drawWidth, drawHeight,
                alpha * theme.opacity(), active && isHoveredOrFocused(), active,
                mouseX, mouseY, hoverAmount);
        SmoothLabel.draw(graphics, getMessage().getString(), drawX, drawY,
                drawWidth, drawHeight, alpha, active, hoverAmount);
    }

    private static float easeOut(float value) {
        return 1F - (float) Math.pow(1F - value, 3);
    }

    public static final class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = 150;
        private int height = 40;
        private LiquidTheme theme = LiquidTheme.DEFAULT;

        private Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder theme(LiquidTheme theme) {
            this.theme = theme;
            return this;
        }

        public GlassButton build() {
            return new GlassButton(x, y, width, height, message, onPress, theme);
        }
    }
}
