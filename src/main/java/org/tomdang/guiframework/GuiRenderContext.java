package org.tomdang.guiframework;

import java.util.Map;
import org.bukkit.entity.Player;

public record GuiRenderContext(Player player, Map<String, Object> state) {
    public GuiRenderContext {
        if (player == null) throw new IllegalArgumentException("GUI render player is required");
        state = state == null ? Map.of() : Map.copyOf(state);
    }
    public int intValue(String key, int fallback) {
        Object value = state.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }
    public String text(String key, String fallback) {
        Object value = state.get(key);
        return value instanceof String text ? text : fallback;
    }
}
