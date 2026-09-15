package org.tomdang.customitemframework.stats;

import org.bukkit.entity.Player;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifier;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeldItemStatCapModifierProvider implements PlayerStatCapModifierProvider {
	private final CustomItemResolver resolver;

	public HeldItemStatCapModifierProvider(CustomItemResolver resolver) {
		if (resolver == null) throw new IllegalArgumentException("resolver cannot be null");
		this.resolver = resolver;
	}

	@Override
	public Collection<PlayerStatCapModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		CustomItem item = resolver.getCustomItem(player.getInventory().getItemInMainHand());
		if (item == null || item.getItemCategory() == ItemCategory.ARMOR) return List.of();
		List<PlayerStatCapModifier> modifiers = new ArrayList<>();
		item.getStatCapModifiers().asMap().forEach((statType, amount) -> {
			if (amount == 0) return;
			modifiers.add(new PlayerStatCapModifier(
					statType,
					"equipment:main-hand:" + item.getId() + ":" + statType.getStorageKey() + ":cap",
					PlayerStatContributionSource.HELD_ITEM,
					item.getDisplayName(),
					amount
			));
		});
		return List.copyOf(modifiers);
	}
}
