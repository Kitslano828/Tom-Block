package org.tomdang.guiframework;

import java.util.Map;

/** Declarative navigation result; actions never manipulate Bukkit inventories directly. */
public sealed interface GuiActionResult permits GuiActionResult.Stay, GuiActionResult.Refresh,
        GuiActionResult.Open, GuiActionResult.Back, GuiActionResult.Close {
    record Stay() implements GuiActionResult {}
    record Refresh(Map<String, Object> state) implements GuiActionResult {
        public Refresh { state = state == null ? Map.of() : Map.copyOf(state); }
    }
    record Open(String screenId, Map<String, Object> state) implements GuiActionResult {
        public Open {
            if (screenId == null || screenId.isBlank()) throw new IllegalArgumentException("Screen ID is required");
            state = state == null ? Map.of() : Map.copyOf(state);
        }
    }
    record Back() implements GuiActionResult {}
    record Close() implements GuiActionResult {}

    static GuiActionResult stay() { return new Stay(); }
    static GuiActionResult refresh(Map<String, Object> state) { return new Refresh(state); }
    static GuiActionResult open(String screenId, Map<String, Object> state) { return new Open(screenId, state); }
    static GuiActionResult back() { return new Back(); }
    static GuiActionResult close() { return new Close(); }
}
