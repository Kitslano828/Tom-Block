package org.tomdang.guiframework;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public final class GuiListener implements Listener {
    private final GuiService service;

    public GuiListener(GuiService service) { this.service = service; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiInventoryHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlot() >= 0 && event.getRawSlot() < topSize) {
            event.setCancelled(true);
            GuiInteraction interaction = switch (event.getClick()) {
                case LEFT -> GuiInteraction.LEFT;
                case RIGHT -> GuiInteraction.RIGHT;
                default -> null;
            };
            if (interaction != null) service.activate(player, holder, event.getRawSlot(), interaction);
            return;
        }
        if (event.isShiftClick() || event.getClick() == ClickType.DOUBLE_CLICK) event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiInventoryHolder)) return;
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GuiInventoryHolder holder && event.getPlayer() instanceof Player player) {
            service.onClosed(player, holder);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { service.closeSilently(event.getPlayer()); }
}
