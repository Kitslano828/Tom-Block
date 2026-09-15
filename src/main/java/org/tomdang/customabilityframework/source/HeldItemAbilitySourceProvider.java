package org.tomdang.customabilityframework.source;

import org.bukkit.entity.Player;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeldItemAbilitySourceProvider implements AbilitySourceProvider {

	private final CustomItemResolver customItemResolver;

	public HeldItemAbilitySourceProvider(CustomItemResolver customItemResolver) {
		if (customItemResolver == null) throw new IllegalArgumentException("customItemResolver cannot be null");
		this.customItemResolver = customItemResolver;
	}

	@Override
	public Collection<AbilitySource> getAbilitySources(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		CustomItem customItem = customItemResolver.getCustomItem(player.getInventory().getItemInMainHand());
		if (customItem == null || customItem.getCustomAbilities().isEmpty()) return List.of();

		List<AbilitySource> sources = new ArrayList<>();
		for (CustomAbility ability : customItem.getCustomAbilities()) {
			sources.add(new AbilitySource(
					ability,
					customItem,
					"equipment:main-hand:" + customItem.getId() + ":" + ability.getAbilityID(),
					AbilitySourceType.MAIN_HAND
			));
		}
		return List.copyOf(sources);
	}
}
