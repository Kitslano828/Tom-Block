package org.tomdang.player.stats.modifier;

import org.bukkit.entity.Player;

import java.util.*;

public class CompositePlayerStatModifierProvider implements PlayerStatModifierProvider{

	private final Collection<PlayerStatModifierProvider> modifierProviders;

	public CompositePlayerStatModifierProvider(Collection<PlayerStatModifierProvider> modifierProviders) {
		if (modifierProviders == null) {
			throw new IllegalArgumentException("modifierProviders cannot be null");
		}
		if (modifierProviders.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("modifierProviders cannot contain null elements");
		}

		this.modifierProviders = List.copyOf(modifierProviders);
	}

	@Override
	public Collection<PlayerStatModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		List<PlayerStatModifier> combinedModifiers = new ArrayList<>();

		for (PlayerStatModifierProvider modifierProvider : modifierProviders) {
			Collection<PlayerStatModifier> modifiers = modifierProvider.getModifiers(player);
			if (modifiers == null) throw new IllegalStateException("modifiers cannot be null");
			if (modifiers.stream().anyMatch(Objects::isNull)) {
				throw new IllegalStateException("modifiers cannot contain null elements");
			}

			combinedModifiers.addAll(modifiers);
		}

		return List.copyOf(combinedModifiers);
	}
}
