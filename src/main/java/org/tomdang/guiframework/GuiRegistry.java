package org.tomdang.guiframework;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class GuiRegistry {
    private final Map<String, GuiScreen> screens = new LinkedHashMap<>();
    private boolean sealed;

    public void register(GuiScreen screen) {
        if (sealed) throw new IllegalStateException("GUI registry is sealed");
        if (screen == null) throw new IllegalArgumentException("GUI screen is required");
        String id = key(screen.id());
        validateSize(screen.size());
        if (screens.putIfAbsent(id, screen) != null) throw new IllegalArgumentException("Duplicate GUI screen " + id);
    }
    public GuiScreen require(String id) {
        GuiScreen screen = screens.get(key(id));
        if (screen == null) throw new IllegalArgumentException("Unknown GUI screen " + id);
        return screen;
    }
    public void seal() { sealed = true; }
    public boolean isSealed() { return sealed; }
    private String key(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("GUI screen ID is required");
        return id.trim().toUpperCase(Locale.ROOT);
    }
    private void validateSize(int size) {
        if (size < 9 || size > 54 || size % 9 != 0) throw new IllegalArgumentException("Invalid GUI screen size " + size);
    }
}
