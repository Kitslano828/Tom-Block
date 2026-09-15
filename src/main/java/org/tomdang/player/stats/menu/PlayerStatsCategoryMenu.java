package org.tomdang.player.stats.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;
import org.tomdang.player.stats.presentation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStatsCategoryMenu implements InventoryHolder {
	private final Inventory inventory;
	private final PlayerStatCategory category;
	private final int backSlot;
	private final int closeSlot;
	private final Map<Integer, PlayerStatType> statSlots = new HashMap<>();

	public PlayerStatsCategoryMenu(PlayerStatCategoryPresentation categoryPresentation,
			PlayerStatsCategoryMenuConfiguration configuration, PlayerStatPresentationRegistry registry,
			PlayerStatEvaluation evaluation) {
		if (categoryPresentation == null || configuration == null || registry == null || evaluation == null) throw new IllegalArgumentException("menu inputs cannot be null");
		category = categoryPresentation.category();
		backSlot = configuration.backSlot();
		closeSlot = configuration.closeSlot();
		inventory = Bukkit.createInventory(this, configuration.size(), Component.text(configuration.titleFor(categoryPresentation.displayName())));
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		PlayerStatMenuItemRenderer renderer = new PlayerStatMenuItemRenderer();
		List<PlayerStatPresentation> stats = registry.getVisibleByCategory(category);
		List<Integer> slots = new CenteredStatSlotCalculator().calculate(stats.size());
		for (int index = 0; index < stats.size(); index++) {
			PlayerStatPresentation stat = stats.get(index);
			int slot = slots.get(index);
			inventory.setItem(slot, factory.create(renderer.render(stat, evaluation.getBreakdown(stat.statType()))));
			statSlots.put(slot, stat.statType());
		}
		inventory.setItem(backSlot, navigationItem(factory, configuration.backMaterial(), configuration.backName(), configuration.backColor()));
		inventory.setItem(closeSlot, navigationItem(factory, configuration.closeMaterial(), configuration.closeName(), configuration.closeColor()));
	}

	private org.bukkit.inventory.ItemStack navigationItem(PlayerStatMenuItemFactory factory, org.bukkit.Material material, String name, net.kyori.adventure.text.format.TextColor color) {
		return factory.create(new PlayerStatMenuItemDefinition(material, Component.text(name, color).decoration(TextDecoration.ITALIC, false), List.of()));
	}
	public PlayerStatCategory getCategory() { return category; }
	public boolean isBackSlot(int slot) { return slot == backSlot; }
	public boolean isCloseSlot(int slot) { return slot == closeSlot; }
	public PlayerStatType statAt(int slot) { return statSlots.get(slot); }
	@Override public Inventory getInventory() { return inventory; }
}
