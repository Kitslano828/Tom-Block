package org.tomdang.guiframework;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Unforgeable server-side identity and action table for one rendered menu revision. */
public final class GuiInventoryHolder implements InventoryHolder {
    private final UUID playerId;
    private final UUID sessionToken;
    private final String screenId;
    private final long revision;
    private final Map<Integer, GuiSlot> slots;
    private Inventory inventory;

    GuiInventoryHolder(UUID playerId, UUID sessionToken, String screenId, long revision, Map<Integer, GuiSlot> slots) {
        this.playerId = playerId;
        this.sessionToken = sessionToken;
        this.screenId = screenId;
        this.revision = revision;
        this.slots = Map.copyOf(slots);
    }

    void bind(Inventory inventory) {
        if (this.inventory != null) throw new IllegalStateException("GUI inventory is already bound");
        this.inventory = inventory;
    }

    Optional<GuiSlot> slot(int index) { return Optional.ofNullable(slots.get(index)); }
    public UUID playerId() { return playerId; }
    public UUID sessionToken() { return sessionToken; }
    public String screenId() { return screenId; }
    public long revision() { return revision; }

    @Override
    public Inventory getInventory() {
        if (inventory == null) throw new IllegalStateException("GUI inventory has not been bound");
        return inventory;
    }
}
