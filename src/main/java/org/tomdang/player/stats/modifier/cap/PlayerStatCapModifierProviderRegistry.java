package org.tomdang.player.stats.modifier.cap;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerStatCapModifierProviderRegistry implements PlayerStatCapModifierProvider {
	private final Map<String, PlayerStatCapModifierProvider> providers = new LinkedHashMap<>();

	public void register(String id, PlayerStatCapModifierProvider provider) {
		validateId(id);
		if (provider == null) throw new IllegalArgumentException("provider cannot be null");
		if (providers.containsKey(id)) {
			throw new IllegalStateException("Stat cap modifier provider is already registered: " + id);
		}
		providers.put(id, provider);
	}

	public boolean unregister(String id) {
		validateId(id);
		return providers.remove(id) != null;
	}

	public boolean isRegistered(String id) {
		validateId(id);
		return providers.containsKey(id);
	}

	public Set<String> getRegisteredIds() {
		return Set.copyOf(providers.keySet());
	}

	@Override
	public Collection<PlayerStatCapModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		List<PlayerStatCapModifier> combined = new ArrayList<>();
		for (Map.Entry<String, PlayerStatCapModifierProvider> entry : providers.entrySet()) {
			Collection<PlayerStatCapModifier> modifiers = entry.getValue().getModifiers(player);
			if (modifiers == null) {
				throw new IllegalStateException("Stat cap modifier provider " + entry.getKey() + " returned null");
			}
			if (modifiers.stream().anyMatch(java.util.Objects::isNull)) {
				throw new IllegalStateException("Stat cap modifier provider " + entry.getKey() + " returned a null entry");
			}
			combined.addAll(modifiers);
		}
		return List.copyOf(combined);
	}

	private void validateId(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
	}
}
