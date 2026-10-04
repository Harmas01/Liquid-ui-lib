package liquidui.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Register on the client initialization thread; hosts create fresh widgets per screen. */
public final class LiquidMenus {
    private static final Map<ResourceLocation, Entry> CODE_ENTRIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Entry> DATA_ENTRIES = new LinkedHashMap<>();

    private LiquidMenus() {}

    /** Register an action. IDs must be unique and namespaced by the extension mod. */
    public static void register(ResourceLocation id, Component label, int order,
                                Consumer<Screen> action) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(action, "action");
        if (CODE_ENTRIES.containsKey(id)) {
            throw new IllegalArgumentException("Liquid UI menu entry already registered: " + id);
        }
        CODE_ENTRIES.put(id, new Entry(label.copy(), order, action));
    }

    /** The factory receives the parent screen, for Back/Escape navigation. */
    public static void registerScreen(ResourceLocation id, Component label, int order,
                                       Function<Screen, Screen> factory) {
        Objects.requireNonNull(factory, "factory");
        register(id, label, order, parent -> Minecraft.getInstance().setScreen(
                Objects.requireNonNull(factory.apply(parent), "screen factory result")));
    }

    /** Host API: add these fresh widgets once per init, then apply your layout. */
    public static List<Button> createButtons(Screen parent) {
        Objects.requireNonNull(parent, "parent");
        List<Button> buttons = new ArrayList<>();
        List<Entry> entries = new ArrayList<>(CODE_ENTRIES.values());
        entries.addAll(DATA_ENTRIES.values());
        entries.stream().sorted(Comparator.comparingInt(Entry::order)).forEach(entry -> {
            Button action = Button.builder(entry.label().copy(), button -> entry.action().accept(parent))
                    .bounds(0, 0, 132, 78).build();
            buttons.add(new RoundedMenuButton(action, 0, 0, 132, 78));
        });
        return List.copyOf(buttons);
    }

    static void replaceDataEntries(Map<ResourceLocation, DataEntry> replacements) {
        DATA_ENTRIES.clear();
        replacements.forEach((id, entry) -> DATA_ENTRIES.put(id,
                new Entry(entry.label().copy(), entry.order(), entry.action())));
    }

    static record DataEntry(Component label, int order, Consumer<Screen> action) {}

    private record Entry(Component label, int order, Consumer<Screen> action) {}
}
