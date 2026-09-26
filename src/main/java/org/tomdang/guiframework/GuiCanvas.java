package org.tomdang.guiframework;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/** Validated screen composition surface. */
public final class GuiCanvas {
    private final int size;
    private final Map<Integer, GuiSlot> slots = new LinkedHashMap<>();

    public GuiCanvas(int size) {
        if (size < 9 || size > 54 || size % 9 != 0) throw new IllegalArgumentException("GUI size must be 9-54 in rows of nine");
        this.size = size;
    }
    public GuiCanvas put(int slot, ItemStack item, GuiSlotRole role) {
        return put(slot, item, role, null);
    }
    public GuiCanvas put(int slot, ItemStack item, GuiSlotRole role, GuiAction action) {
        validate(slot);
        if (slots.putIfAbsent(slot, new GuiSlot(item, role, action)) != null) {
            throw new IllegalArgumentException("GUI slot " + slot + " is already occupied");
        }
        return this;
    }
    public GuiCanvas fillEmpty(ItemStack item) {
        for (int slot = 0; slot < size; slot++) if (!slots.containsKey(slot)) {
            slots.put(slot, new GuiSlot(item, GuiSlotRole.DECORATION, null));
        }
        return this;
    }
    public int size() { return size; }
    public Map<Integer, GuiSlot> slots() { return Map.copyOf(slots); }
    private void validate(int slot) {
        if (slot < 0 || slot >= size) throw new IllegalArgumentException("GUI slot is outside the inventory: " + slot);
    }
}
