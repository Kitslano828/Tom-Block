package org.tomdang.player.stats.modifier.cap;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CompositePlayerStatCapModifierProvider implements PlayerStatCapModifierProvider {

	private final List<PlayerStatCapModifierProvider> providers;

	public CompositePlayerStatCapModifierProvider(Collection<PlayerStatCapModifierProvider> providers) {
		if (providers == null) throw new IllegalArgumentException("providers cannot be null");
		if (providers.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("providers cannot contain null elements");
		}
		this.providers = List.copyOf(providers);
	}

	@Override
	public Collection<PlayerStatCapModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		List<PlayerStatCapModifier> combined = new ArrayList<>();
		for (PlayerStatCapModifierProvider provider : providers) {
			Collection<PlayerStatCapModifier> modifiers = provider.getModifiers(player);
			if (modifiers == null) throw new IllegalStateException("cap modifiers cannot be null");
			if (modifiers.stream().anyMatch(Objects::isNull)) {
				throw new IllegalStateException("cap modifiers cannot contain null elements");
			}
			combined.addAll(modifiers);
		}
		return List.copyOf(combined);
	}
}
