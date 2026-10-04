package liquidui;

import net.minecraft.resources.Identifier;
import net.minecraftforge.fml.common.Mod;
import liquidui.api.GlassRenderer;

@Mod(LiquidUiLibrary.MOD_ID)
public final class LiquidUiLibrary {
    public static final String MOD_ID = "liquid_ui_library";

    public LiquidUiLibrary() {
        GlassRenderer.initialize();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
