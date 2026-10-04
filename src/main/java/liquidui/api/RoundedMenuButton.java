package liquidui.api;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

/** Smooth frosted-glass controls, retaining vanilla actions and keyboard navigation. */
public final class RoundedMenuButton extends Button {
    private final Button original;
    private float hoverLift;
    private float focusScale;
    private long entranceStartedAt = -1L;
    private long entranceDelayMs;

    public RoundedMenuButton(Button original, int x, int y, int width, int height) {
        this(original, x, y, width, height, button -> original.onPress());
    }

    public RoundedMenuButton(Button original, int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, original.getMessage(), onPress, DEFAULT_NARRATION);
        this.original = original;
        active = original.active;
        visible = original.visible;
        setTooltip(original.getTooltip());
    }

    /** Starts a soft entrance animation after the supplied delay. */
    public RoundedMenuButton withEntranceDelay(long delayMs) {
        entranceDelayMs = Math.max(0L, delayMs);
        entranceStartedAt = Util.getMillis();
        return this;
    }

    private float entranceProgress() {
        if (entranceStartedAt < 0L) return 1F;
        float progress = (Util.getMillis() - entranceStartedAt - entranceDelayMs) / 500F;
        progress = Math.max(0F, Math.min(1F, progress));
        // A sine curve starts visibly earlier than smootherstep while keeping
        // zero velocity at both ends, avoiding a late opacity jump.
        return 0.5F - (float) Math.cos(progress * Math.PI) * 0.5F;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return entranceProgress() > 0.9F && active && visible
                && mouseX >= getX() - 3 && mouseX < getX() + width + 3
                && mouseY >= getY() - 8 && mouseY < getY() + height + 3;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        active = original.active;
        visible = original.visible;
        setMessage(original.getMessage());
        if (alpha <= 0.02F) return;
        float entrance = entranceProgress();
        float target = active && isHovered() ? 1F : 0F;
        hoverLift += (target - hoverLift) * 0.22F;
        if (Math.abs(target - hoverLift) < 0.002F) hoverLift = target;
        float focusTarget = active && isFocused() && !isHovered() ? 1F : 0F;
        focusScale += (focusTarget - focusScale) * 0.20F;
        if (Math.abs(focusTarget - focusScale) < 0.002F) focusScale = focusTarget;
        float eased = 1F - (float) Math.pow(1F - hoverLift, 3);
        int grow = Math.round((1F - (float) Math.pow(1F - focusScale, 3)) * 3F);
        int drawX = getX() - grow;
        int drawY = getY() - Math.round(eased * 5F) - grow;
        int drawWidth = width + grow * 2;
        int drawHeight = height + grow * 2;
        float silhouetteAlpha = entrance;
        float reveal = entrance;
        float drawAlpha = alpha * silhouetteAlpha;
        GlassRenderer.drawRevealingInteractive(graphics, drawX, drawY, drawWidth, drawHeight,
                drawAlpha, active && isHoveredOrFocused(), active,
                mouseX, mouseY, hoverLift, reveal);
        SmoothLabel.draw(graphics, getMessage().getString(), drawX, drawY,
                drawWidth, drawHeight, alpha * entrance, active, hoverLift);
    }
}
