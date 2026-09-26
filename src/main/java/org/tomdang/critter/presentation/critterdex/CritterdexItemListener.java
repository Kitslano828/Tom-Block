package org.tomdang.critter.presentation.critterdex;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.guiframework.GuiService;

public final class CritterdexItemListener implements Listener {
    private final CustomItemResolver items;
    private final GuiService menus;

    public CritterdexItemListener(CustomItemResolver items, GuiService menus) {
        this.items = items; this.menus = menus;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var item = items.getCustomItem(event.getItem());
        if (item == null || !item.getId().equalsIgnoreCase("CRITTERDEX")) return;
        event.setCancelled(true);
        menus.open(event.getPlayer(), CritterdexIndexScreen.ID);
    }
}
