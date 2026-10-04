package liquidui;

import net.minecraft.resources.Identifier;

public final class LiquidUiLibrary {
    public static final String MOD_ID = "liquid_ui_library";

    private LiquidUiLibrary() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
