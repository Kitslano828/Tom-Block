package org.tomdang.customitemframework.stats;

import org.bukkit.entity.Player;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HeldItemStatModifierProvider implements PlayerStatModifierProvider {

	private final CustomItemResolver customItemResolver;

	public HeldItemStatModifierProvider(CustomItemResolver customItemResolver) {
		if (customItemResolver == null) throw new IllegalArgumentException("customItemResolver cannot be null");
		this.customItemResolver = customItemResolver;
	}

	@Override
	public Collection<PlayerStatModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		CustomItem customItem = customItemResolver.getCustomItem(player.getInventory().getItemInMainHand());
		if (customItem == null) return List.of();
		if (customItem.getItemCategory() == ItemCategory.ARMOR) return List.of();

		List<PlayerStatModifier> modifiers = new ArrayList<>();
		for (Map.Entry<PlayerStatType, Double> entry : customItem.getStatModifiers().asMap().entrySet()) {
			if (entry.getValue() == 0) continue;
			modifiers.add(new PlayerStatModifier(
					entry.getKey(),
					"equipment:main-hand:" + customItem.getId() + ":" + entry.getKey().getStorageKey(),
					entry.getValue()
			));
		}
		return List.copyOf(modifiers);
	}

}
