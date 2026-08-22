package org.tomdang.player.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerMenuListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Check if the clicked inventory exists and matches your GUI title
		// Note: In newer PaperMC versions, use event.getView().getTitle()
		TextComponent chestName = Component.text("            YOUR STATS").color(TextColor.color(0x290F0D));
		if (event.getView().title().equals(chestName)) {
			// Cancel the event so the player cannot pick up, move, or swap items
			event.setCancelled(true);
		}
	}
}
