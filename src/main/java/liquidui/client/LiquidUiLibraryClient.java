package liquidui.client;

import liquidui.api.GlassRenderer;
import net.fabricmc.api.ClientModInitializer;

public final class LiquidUiLibraryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GlassRenderer.initialize();
    }
}
