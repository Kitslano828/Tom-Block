package org.tomdang.player.stats.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.tomdang.player.stats.PlayerStatCategory;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.*;
import org.tomdang.player.stats.presentation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerStatBreakdownMenu implements InventoryHolder {
	private final Inventory inventory;
	private final PlayerStatCategory category;
	private final int backSlot;
	private final int closeSlot;

	public PlayerStatBreakdownMenu(PlayerStatType statType, org.bukkit.Material borderMaterial, PlayerStatPresentationRegistry registry,
			PlayerStatsBreakdownMenuConfiguration configuration, PlayerStatEvaluation evaluation) {
		if (statType == null || borderMaterial == null || registry == null || configuration == null || evaluation == null) throw new IllegalArgumentException("menu inputs cannot be null");
		PlayerStatPresentation statPresentation = registry.get(statType);
		PlayerStatBreakdown breakdown = evaluation.getBreakdown(statType);
		category = statType.getCategory();
		backSlot = configuration.backSlot();
		closeSlot = configuration.closeSlot();
		inventory = Bukkit.createInventory(this, configuration.size(), Component.text(configuration.titleFor(statType.getDisplayName())));
		new StatsMenuBorderRenderer().render(inventory, borderMaterial);
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		PlayerStatBreakdownItemRenderer renderer = new PlayerStatBreakdownItemRenderer();
		inventory.setItem(configuration.summarySlot(), factory.create(renderer.renderSummary(statPresentation, breakdown)));

		List<PlayerStatContribution> contributions = new ArrayList<>(breakdown.contributions());
		contributions.sort(Comparator.comparingInt(value -> value.source().ordinal()));
		List<Integer> slots = new BreakdownContributionSlotCalculator().calculate(Math.max(1, contributions.size()));
		if (contributions.isEmpty()) {
			inventory.setItem(slots.getFirst(), factory.create(new PlayerStatMenuItemDefinition(configuration.emptyMaterial(),
					Component.text(configuration.emptyName(), configuration.emptyColor()).decoration(TextDecoration.ITALIC,false), List.of())));
		} else {
			for (int index=0; index<contributions.size(); index++) {
				PlayerStatContribution contribution = contributions.get(index);
				inventory.setItem(slots.get(index), factory.create(renderer.renderContribution(contribution, configuration.source(contribution.source()))));
			}
		}
		inventory.setItem(backSlot, navigation(factory, configuration.backMaterial(), configuration.backName(), configuration.backColor()));
		inventory.setItem(closeSlot, navigation(factory, configuration.closeMaterial(), configuration.closeName(), configuration.closeColor()));
	}

	private org.bukkit.inventory.ItemStack navigation(PlayerStatMenuItemFactory factory, org.bukkit.Material material, String name, net.kyori.adventure.text.format.TextColor color) {
		return factory.create(new PlayerStatMenuItemDefinition(material, Component.text(name,color).decoration(TextDecoration.ITALIC,false), List.of()));
	}
	public PlayerStatCategory getCategory(){ return category; }
	public boolean isBackSlot(int slot){ return slot==backSlot; }
	public boolean isCloseSlot(int slot){ return slot==closeSlot; }
	@Override public Inventory getInventory(){ return inventory; }
}
