package liquidui.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import liquidui.LiquidUiLibrary;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads no-code Liquid UI screens from assets/&lt;namespace&gt;/liquid_ui/extensions/*.json. */
public final class LiquidExtensionLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("liquid-ui-extensions");
    private static final ResourceLocation LISTENER_ID =
            new ResourceLocation(LiquidUiLibrary.MOD_ID, "extensions");
    private static final Map<ResourceLocation, ScreenDefinition> SCREENS = new LinkedHashMap<>();
    private static boolean initialized;

    private LiquidExtensionLoader() {}

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Listener());
    }

    private static void reload(ResourceManager manager) {
        Map<ResourceLocation, ScreenDefinition> loaded = new LinkedHashMap<>();
        manager.listResources("liquid_ui/extensions", id -> id.getPath().endsWith(".json"))
                .forEach((resourceId, resource) -> read(resourceId, resource, loaded));
        SCREENS.clear();
        SCREENS.putAll(loaded);

        Map<ResourceLocation, LiquidMenus.DataEntry> menuEntries = new LinkedHashMap<>();
        loaded.forEach((id, definition) -> {
            if (definition.menuLabel() == null || definition.menuLabel().isBlank()) return;
            menuEntries.put(id, new LiquidMenus.DataEntry(Component.literal(definition.menuLabel()),
                    definition.order(), parent -> Minecraft.getInstance().setScreen(
                    new DataScreen(parent, id, definition))));
        });
        LiquidMenus.replaceDataEntries(menuEntries);
        LOGGER.info("Loaded {} Liquid UI no-code screen(s)", loaded.size());
    }

    private static void read(ResourceLocation resourceId, Resource resource,
                             Map<ResourceLocation, ScreenDefinition> loaded) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            ResourceLocation id = new ResourceLocation(requiredString(root, "id"));
            String title = string(root, "title", id.getPath());
            JsonObject menu = root.has("menu") && root.get("menu").isJsonObject()
                    ? root.getAsJsonObject("menu") : new JsonObject();
            String menuLabel = string(menu, "label", title);
            int order = integer(menu, "order", 100);
            List<ButtonDefinition> buttons = new ArrayList<>();
            JsonArray elements = root.has("elements") && root.get("elements").isJsonArray()
                    ? root.getAsJsonArray("elements") : new JsonArray();
            for (JsonElement element : elements) {
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();
                if (!"button".equalsIgnoreCase(string(object, "type", "button"))) continue;
                JsonObject action = object.has("action") && object.get("action").isJsonObject()
                        ? object.getAsJsonObject("action") : new JsonObject();
                buttons.add(new ButtonDefinition(string(object, "text", "Button"),
                        integer(object, "x", -100), integer(object, "y", 0),
                        Math.max(40, integer(object, "width", 200)),
                        Math.max(20, integer(object, "height", 48)),
                        string(action, "type", "back"), string(action, "target", "")));
            }
            ScreenDefinition previous = loaded.put(id,
                    new ScreenDefinition(title, menuLabel, order, List.copyOf(buttons)));
            if (previous != null) LOGGER.warn("Duplicate Liquid UI screen id {}, last resource wins", id);
        } catch (Exception exception) {
            LOGGER.error("Could not load Liquid UI extension {}", resourceId, exception);
        }
    }

    private static String requiredString(JsonObject object, String name) {
        if (!object.has(name) || !object.get(name).isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing string property: " + name);
        }
        return object.get(name).getAsString();
    }

    private static String string(JsonObject object, String name, String fallback) {
        return object.has(name) && object.get(name).isJsonPrimitive()
                ? object.get(name).getAsString() : fallback;
    }

    private static int integer(JsonObject object, String name, int fallback) {
        try {
            return object.has(name) ? object.get(name).getAsInt() : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private record ScreenDefinition(String title, String menuLabel, int order,
                                    List<ButtonDefinition> buttons) {}

    private record ButtonDefinition(String text, int x, int y, int width, int height,
                                    String action, String target) {}

    private static final class DataScreen extends GlassScreen {
        private final ResourceLocation definitionId;
        private final ScreenDefinition definition;

        private DataScreen(Screen parent, ResourceLocation definitionId, ScreenDefinition definition) {
            super(Component.literal(definition.title()), parent);
            this.definitionId = definitionId;
            this.definition = definition;
        }

        @Override
        protected void init() {
            LiquidTheme theme = LiquidTheme.DEFAULT;
            for (ButtonDefinition button : definition.buttons()) {
                int x = width / 2 + button.x();
                int y = height / 2 + button.y();
                addRenderableWidget(GlassButton.glassBuilder(Component.literal(button.text()),
                        ignored -> perform(button)).position(x, y)
                        .size(button.width(), button.height()).theme(theme).build());
            }
        }

        @Override
        protected void renderGlassContent(GuiGraphics graphics, int mouseX, int mouseY,
                                          float partialTick) {
            graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        }

        private void perform(ButtonDefinition button) {
            String action = button.action().toLowerCase(Locale.ROOT);
            switch (action) {
                case "back" -> onClose();
                case "close" -> minecraft.setScreen(null);
                case "main_menu" -> minecraft.setScreen(new TitleScreen());
                case "options" -> minecraft.setScreen(new OptionsScreen(this, minecraft.options));
                case "language" -> minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options,
                        minecraft.getLanguageManager()));
                case "singleplayer" -> minecraft.setScreen(new SelectWorldScreen(this));
                case "multiplayer" -> minecraft.setScreen(new JoinMultiplayerScreen(this));
                case "open_screen" -> openTarget(button.target());
                default -> LOGGER.warn("Unknown action '{}' on screen {}", button.action(), definitionId);
            }
        }

        private void openTarget(String target) {
            try {
                ResourceLocation targetId = new ResourceLocation(target);
                ScreenDefinition targetDefinition = SCREENS.get(targetId);
                if (targetDefinition == null) {
                    LOGGER.warn("Unknown Liquid UI target screen {}", targetId);
                    return;
                }
                minecraft.setScreen(new DataScreen(this, targetId, targetDefinition));
            } catch (RuntimeException exception) {
                LOGGER.warn("Invalid Liquid UI target '{}'", target);
            }
        }
    }

    private static final class Listener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return LISTENER_ID;
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            LiquidExtensionLoader.reload(manager);
        }
    }
}
