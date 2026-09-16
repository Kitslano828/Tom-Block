package org.tomdang.region.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class RegionBrushItemService {
	private static final byte MARKER_VALUE = 1;
	private final NamespacedKey markerKey;

	public RegionBrushItemService(NamespacedKey markerKey) {
		if (markerKey == null) throw new IllegalArgumentException("markerKey cannot be null");
		this.markerKey = markerKey;
	}

	public ItemStack create() {
		ItemStack brush = new ItemStack(Material.BRUSH);
		ItemMeta meta = brush.getItemMeta();
		meta.displayName(Component.text("Region Brush", NamedTextColor.AQUA)
				.decoration(TextDecoration.BOLD, true)
				.decoration(TextDecoration.ITALIC, false));
		meta.lore(List.of(
				line("Left-click: include block", NamedTextColor.GREEN),
				line("Right-click: exclude block", NamedTextColor.RED),
				line("Sneak + right-click: clear override", NamedTextColor.YELLOW)
		));
		meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, MARKER_VALUE);
		brush.setItemMeta(meta);
		return brush;
	}

	public boolean isBrush(ItemStack item) {
		if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
		Byte marker = item.getItemMeta().getPersistentDataContainer().get(markerKey, PersistentDataType.BYTE);
		return marker != null && marker == MARKER_VALUE;
	}

	private Component line(String text, NamedTextColor color) {
		return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
	}
}
