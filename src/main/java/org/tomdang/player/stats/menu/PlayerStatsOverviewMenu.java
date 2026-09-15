package org.tomdang.player.stats.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;
import org.tomdang.player.stats.presentation.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatsOverviewMenu implements InventoryHolder {
	private final Inventory inventory;
	private final int closeSlot;
	private final Map<Integer, PlayerStatCategory> categorySlots = new HashMap<>();

	public PlayerStatsOverviewMenu(PlayerStatsOverviewConfiguration configuration,
			PlayerStatPresentationRegistry registry, PlayerStatEvaluation evaluation) {
		if (configuration == null || registry == null || evaluation == null) throw new IllegalArgumentException("menu inputs cannot be null");
		closeSlot = configuration.closeSlot();
		inventory = Bukkit.createInventory(this, configuration.size(), Component.text(configuration.title()));
		new StatsMenuBorderRenderer().render(inventory, configuration.borderMaterial());
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		PlayerStatCategoryMenuItemRenderer renderer = new PlayerStatCategoryMenuItemRenderer();
		for (PlayerStatCategoryPresentation category : configuration.categories()) {
			if (!category.visible()) continue;
			inventory.setItem(category.slot(), factory.create(renderer.render(category, registry, evaluation)));
			categorySlots.put(category.slot(), category.category());
		}
		inventory.setItem(closeSlot, factory.create(new PlayerStatMenuItemDefinition(configuration.closeMaterial(),
				Component.text(configuration.closeName(), configuration.closeColor()).decoration(TextDecoration.ITALIC, false), java.util.List.of())));
	}

	public boolean isCloseSlot(int slot) { return slot == closeSlot; }
	public PlayerStatCategory categoryAt(int slot) { return categorySlots.get(slot); }
	@Override public Inventory getInventory() { return inventory; }
}
