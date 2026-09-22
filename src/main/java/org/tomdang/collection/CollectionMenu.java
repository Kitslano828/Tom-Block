package org.tomdang.collection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CollectionMenu implements InventoryHolder {
	private final Inventory inventory = Bukkit.createInventory(this, 54, Component.text("Collections", NamedTextColor.DARK_GREEN));
	public CollectionMenu(Player player, CollectionService service) {
		int slot = 10;
		for (CollectionDefinition definition : service.definitions()) {
			long amount = service.amount(player.getUniqueId(), definition);
			Long next = definition.nextMilestone(amount);
			ItemStack item = ItemStack.of(definition.material());
			var meta = item.getItemMeta();
			meta.displayName(Component.text(definition.displayName(), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text(definition.category(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());
			lore.add(Component.text("Collected: ", NamedTextColor.GRAY).append(Component.text(amount, NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
			lore.add(next == null ? Component.text("All current milestones reached", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
					: Component.text("Next milestone: " + amount + " / " + next, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
			meta.lore(lore); item.setItemMeta(meta); inventory.setItem(slot++, item);
			if (slot % 9 == 8) slot += 2;
		}
	}
	@Override public Inventory getInventory() { return inventory; }
}
