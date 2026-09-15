package org.tomdang.player.command;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.tomdang.player.stats.menu.PlayerStatsOverviewMenu;
import org.tomdang.player.stats.menu.PlayerStatsCategoryMenu;
import org.tomdang.player.stats.menu.PlayerStatBreakdownMenu;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.tomdang.player.stats.presentation.PlayerStatsCategoryMenuConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsBreakdownMenuConfiguration;
import org.tomdang.player.stats.PlayerStatType;

public class PlayerMenuListener implements Listener {
	private final PlayerStatsService statsService;
	private final PlayerStatPresentationRegistry registry;
	private final PlayerStatsOverviewConfiguration overviewConfiguration;
	private final PlayerStatsCategoryMenuConfiguration categoryConfiguration;
	private final PlayerStatsBreakdownMenuConfiguration breakdownConfiguration;

	public PlayerMenuListener(PlayerStatsService statsService, PlayerStatPresentationRegistry registry,
			PlayerStatsOverviewConfiguration overviewConfiguration, PlayerStatsCategoryMenuConfiguration categoryConfiguration,
			PlayerStatsBreakdownMenuConfiguration breakdownConfiguration) {
		if (statsService == null || registry == null || overviewConfiguration == null || categoryConfiguration == null || breakdownConfiguration == null) throw new IllegalArgumentException("listener inputs cannot be null");
		this.statsService = statsService;
		this.registry = registry;
		this.overviewConfiguration = overviewConfiguration;
		this.categoryConfiguration = categoryConfiguration;
		this.breakdownConfiguration = breakdownConfiguration;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Object holder = event.getView().getTopInventory().getHolder();
		if (!(holder instanceof PlayerStatsOverviewMenu) && !(holder instanceof PlayerStatsCategoryMenu) && !(holder instanceof PlayerStatBreakdownMenu)) return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player player)) return;
		if (holder instanceof PlayerStatsOverviewMenu overview) {
			if (overview.isCloseSlot(event.getRawSlot())) { player.closeInventory(); return; }
			PlayerStatCategory category = overview.categoryAt(event.getRawSlot());
			if (category != null) openCategory(player, category);
			return;
		}
		if (holder instanceof PlayerStatsCategoryMenu categoryMenu) {
			if (categoryMenu.isCloseSlot(event.getRawSlot())) player.closeInventory();
			else if (categoryMenu.isBackSlot(event.getRawSlot())) openOverview(player);
			else {
				PlayerStatType statType = categoryMenu.statAt(event.getRawSlot());
				if (statType != null) openBreakdown(player, statType);
			}
			return;
		}
		PlayerStatBreakdownMenu breakdownMenu = (PlayerStatBreakdownMenu) holder;
		if (breakdownMenu.isCloseSlot(event.getRawSlot())) player.closeInventory();
		else if (breakdownMenu.isBackSlot(event.getRawSlot())) openCategory(player, breakdownMenu.getCategory());
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Object holder = event.getView().getTopInventory().getHolder();
		if (holder instanceof PlayerStatsOverviewMenu || holder instanceof PlayerStatsCategoryMenu || holder instanceof PlayerStatBreakdownMenu) event.setCancelled(true);
	}

	private void openOverview(Player player) {
		player.openInventory(new PlayerStatsOverviewMenu(overviewConfiguration, registry, statsService.evaluate(player)).getInventory());
	}

	private void openCategory(Player player, PlayerStatCategory category) {
		player.openInventory(new PlayerStatsCategoryMenu(overviewConfiguration.category(category), categoryConfiguration,
				registry, statsService.evaluate(player)).getInventory());
	}

	private void openBreakdown(Player player, PlayerStatType statType) {
		player.openInventory(new PlayerStatBreakdownMenu(statType, overviewConfiguration.category(statType.getCategory()).borderMaterial(), registry, breakdownConfiguration, statsService.evaluate(player)).getInventory());
	}
}
