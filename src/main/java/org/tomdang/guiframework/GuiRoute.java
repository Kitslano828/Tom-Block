package org.tomdang.guiframework;

import java.util.Locale;
import java.util.Map;

public record GuiRoute(String screenId, Map<String, Object> state) {
    public GuiRoute {
        if (screenId == null || screenId.isBlank()) throw new IllegalArgumentException("GUI route screen is required");
        screenId = screenId.trim().toUpperCase(Locale.ROOT);
        state = state == null ? Map.of() : Map.copyOf(state);
    }
}
