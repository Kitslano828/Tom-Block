package org.tomdang.customabilityframework.source;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CompositeAbilitySourceProvider implements AbilitySourceProvider {

	private final Collection<AbilitySourceProvider> sourceProviders;

	public CompositeAbilitySourceProvider(Collection<AbilitySourceProvider> sourceProviders) {
		if (sourceProviders == null) throw new IllegalArgumentException("sourceProviders cannot be null");
		if (sourceProviders.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("sourceProviders cannot contain null elements");
		}
		this.sourceProviders = List.copyOf(sourceProviders);
	}

	@Override
	public Collection<AbilitySource> getAbilitySources(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		List<AbilitySource> combinedSources = new ArrayList<>();
		for (AbilitySourceProvider sourceProvider : sourceProviders) {
			Collection<AbilitySource> sources = sourceProvider.getAbilitySources(player);
			if (sources == null) throw new IllegalStateException("ability sources cannot be null");
			if (sources.stream().anyMatch(Objects::isNull)) {
				throw new IllegalStateException("ability sources cannot contain null elements");
			}
			combinedSources.addAll(sources);
		}

		return List.copyOf(combinedSources);
	}
}
