package liquidui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import liquidui.api.GlassRenderer;

@Mod(LiquidUiLibrary.MOD_ID)
public final class LiquidUiLibrary {
    public static final String MOD_ID = "liquid_ui_library";

    public LiquidUiLibrary() {
        GlassRenderer.initialize();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
