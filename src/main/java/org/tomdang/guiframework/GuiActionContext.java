package org.tomdang.guiframework;

import java.util.Map;
import org.bukkit.entity.Player;

public record GuiActionContext(Player player, String screenId, int slot, GuiInteraction interaction,
                               Map<String, Object> state) {
    public GuiActionContext {
        if (player == null || screenId == null || screenId.isBlank() || slot < 0 || interaction == null) {
            throw new IllegalArgumentException("GUI action context is incomplete");
        }
        state = state == null ? Map.of() : Map.copyOf(state);
    }
}
