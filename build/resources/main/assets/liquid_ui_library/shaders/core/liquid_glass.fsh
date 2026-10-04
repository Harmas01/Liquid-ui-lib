#version 150
// Liquid UI Library shared glass fragment shader.
uniform sampler2D Sampler0;
uniform vec2 FrameSize;
uniform vec2 RectSize;
uniform float GuiScale;
uniform float Opacity;
uniform float Highlight;
uniform float Enabled;
uniform float Danger;
uniform float IconMode;
uniform vec2 Pointer;
uniform float Hover;
uniform float Reveal;
uniform float Time;
in vec2 localPos;
out vec4 fragColor;

float roundedBox(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 p = localPos - RectSize * 0.5;
    float radius = min(min(18.0, RectSize.y * 0.30), RectSize.x * 0.5);
    float d = roundedBox(p, RectSize * 0.5, radius);
    // Derivatives follow physical framebuffer pixels, even with a large GUI scale.
    float aa = max(fwidth(d), 0.25 / GuiScale);
    float mask = 1.0 - smoothstep(-aa, aa, d);
    if (mask <= 0.001) {
        float shadow = exp(-max(d, 0.0) * 0.8) * 0.13 * Opacity;
        fragColor = vec4(0.02, 0.035, 0.055, shadow);
        return;
    }

    vec2 uv = gl_FragCoord.xy / FrameSize;
    vec2 direction = normalize(p / max(RectSize * 0.5, vec2(1.0)) + vec2(0.0001));
    direction.y = -direction.y;
    float rim = exp(-abs(d) / 3.0);
    // A small inward lens distortion makes the edges read as glass.
    uv -= direction * rim * 2.0 * GuiScale / FrameSize;
    // A small travelling ripple around the pointer makes the plate feel fluid.
    vec2 pointerDelta = localPos - Pointer;
    float pointerDistance = length(pointerDelta);
    float rippleFade = exp(-pointerDistance * 0.032) * Hover;
    float ripple = sin(pointerDistance * 0.34 - Time * 5.2) * rippleFade;
    uv += normalize(pointerDelta + vec2(0.001)) * ripple * 1.35 * GuiScale / FrameSize;
    // Sample neighbouring framebuffer pixels densely. The old wide 3.15-pixel
    // stride skipped columns in pixel-art scenery and produced a visible comb
    // pattern inside large glass plates.
    vec2 stepUV = vec2(1.35 * GuiScale) / FrameSize;
    vec3 blurred = vec3(0.0);
    float total = 0.0;
    for (int y = -4; y <= 4; y++) {
        for (int x = -4; x <= 4; x++) {
            float weight = exp(-float(x*x + y*y) / 10.0);
            vec2 sampleUV = clamp(uv + vec2(x, y) * stepUV,
                                  vec2(0.5) / FrameSize, 1.0 - vec2(0.5) / FrameSize);
            blurred += texture(Sampler0, sampleUV).rgb * weight;
            total += weight;
        }
    }
    blurred /= total;
    float topLight = 1.0 - clamp(localPos.y / RectSize.y, 0.0, 1.0);
    // Clear glass retains the actual background colours, without a white fill.
    vec3 glass = blurred * (0.92 + 0.06 * Highlight);
    float border = 1.0 - smoothstep(0.35, 1.15, abs(d + 0.55));
    glass = mix(glass, vec3(0.86, 0.94, 1.0), border * (0.16 + 0.32 * topLight + 0.20 * Highlight));
    // Cursor-following reflections: a soft bright spot and a narrow glass streak.
    vec2 reflectionDelta = (localPos - Pointer) / max(RectSize, vec2(1.0));
    float reflection = exp(-dot(reflectionDelta, reflectionDelta) * 42.0) * Hover;
    float streak = exp(-pow(reflectionDelta.x + reflectionDelta.y * 0.42, 2.0) * 180.0)
            * exp(-abs(reflectionDelta.y) * 3.5) * Hover;
    glass = mix(glass, vec3(0.94, 0.98, 1.0), reflection * 0.26 + streak * 0.13);
    glass += vec3(0.012) * rim;
    glass = mix(glass, vec3(0.82, 0.035, 0.055), Danger * (0.70 + 0.10 * Highlight));
    glass = mix(glass, vec3(dot(glass, vec3(0.299, 0.587, 0.114))), (1.0 - Enabled) * 0.55);
    // Entrance animation: the opaque dark silhouette resolves into clear glass.
    glass = mix(vec3(0.006, 0.008, 0.012), glass, Reveal);
    // Hairline outline along the rounded edge, kept close to one physical pixel.
    float outlineWidth = 0.24 / GuiScale;
    float blackOutline = 1.0 - smoothstep(outlineWidth, outlineWidth + aa, abs(d));
    if (IconMode > 0.5) {
        float lightAlpha = border * (0.13 + 0.18 * topLight + 0.12 * Highlight)
                + reflection * 0.18 + streak * 0.10 + rim * 0.018;
        float darkAlpha = blackOutline * 0.72;
        vec3 overlay = mix(vec3(0.93, 0.97, 1.0), vec3(0.006, 0.008, 0.012),
                clamp(blackOutline * 1.4, 0.0, 1.0));
        fragColor = vec4(overlay, mask * Opacity * clamp(lightAlpha + darkAlpha, 0.0, 0.84));
        return;
    }
    glass = mix(glass, vec3(0.006, 0.008, 0.012), blackOutline * 0.78);
    float glassAlpha = mix(0.62, 0.70, Highlight);
    glassAlpha = mix(glassAlpha, 0.82, Danger);
    glassAlpha = mix(0.92, glassAlpha, Reveal);
    fragColor = vec4(glass, mask * Opacity * glassAlpha);
}
