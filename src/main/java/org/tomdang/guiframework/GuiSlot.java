package org.tomdang.guiframework;

import org.bukkit.inventory.ItemStack;

public record GuiSlot(ItemStack item, GuiSlotRole role, GuiAction action) {
    public GuiSlot {
        if (item == null || role == null) throw new IllegalArgumentException("GUI slot requires an item and role");
        if (role.interactive() != (action != null)) {
            throw new IllegalArgumentException(role.interactive()
                    ? "Interactive GUI slots require an action" : "Non-interactive GUI slots cannot have an action");
        }
        item = item.clone();
    }
    @Override public ItemStack item() { return item.clone(); }
}
