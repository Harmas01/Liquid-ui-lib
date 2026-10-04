package liquidui.client;

import liquidui.api.GlassRenderer;
import liquidui.api.LiquidExtensionLoader;
import net.fabricmc.api.ClientModInitializer;

public final class LiquidUiLibraryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GlassRenderer.initialize();
        LiquidExtensionLoader.initialize();
    }
}
