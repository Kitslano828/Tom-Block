package org.tomdang.guiframework;

import java.util.Map;
import java.util.Comparator;
import java.util.List;

/** Designer-owned placement data; semantic keys keep screen code free of magic slot numbers. */
public record GuiLayout(String title, int size, Map<String, Integer> slots) {
    public GuiLayout {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("GUI layout title is required");
        if (size < 9 || size > 54 || size % 9 != 0) throw new IllegalArgumentException("GUI layout size must be 9-54 in rows of nine");
        if (slots == null) throw new IllegalArgumentException("GUI layout slots are required");
        slots = Map.copyOf(slots);
    }

    public int slot(String key) {
        Integer value = slots.get(key);
        if (value == null) throw new IllegalArgumentException("GUI layout has no slot named " + key);
        return value;
    }

    public List<Integer> slotsStartingWith(String prefix) {
        return slots.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix))
                .sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).toList();
    }
}
