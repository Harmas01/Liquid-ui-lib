package liquidui.api;

/** Visual parameters shared by the reusable liquid-glass widgets. */
public record LiquidTheme(float opacity, int hoverLift, float animationSpeed, int focusGrowth) {
    public static final LiquidTheme DEFAULT = new LiquidTheme(0.82F, 5, 0.22F, 3);

    public LiquidTheme {
        opacity = Math.max(0F, Math.min(1F, opacity));
        hoverLift = Math.max(0, hoverLift);
        animationSpeed = Math.max(0.01F, Math.min(1F, animationSpeed));
        focusGrowth = Math.max(0, focusGrowth);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float opacity = DEFAULT.opacity;
        private int hoverLift = DEFAULT.hoverLift;
        private float animationSpeed = DEFAULT.animationSpeed;
        private int focusGrowth = DEFAULT.focusGrowth;

        public Builder opacity(float value) {
            opacity = value;
            return this;
        }

        public Builder hoverLift(int value) {
            hoverLift = value;
            return this;
        }

        public Builder animationSpeed(float value) {
            animationSpeed = value;
            return this;
        }

        public Builder focusGrowth(int value) {
            focusGrowth = value;
            return this;
        }

        public LiquidTheme build() {
            return new LiquidTheme(opacity, hoverLift, animationSpeed, focusGrowth);
        }
    }
}
