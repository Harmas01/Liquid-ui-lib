package liquidui;

import net.minecraft.resources.ResourceLocation;

public final class LiquidUiLibrary {
    public static final String MOD_ID = "liquid_ui_library";

    private LiquidUiLibrary() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
