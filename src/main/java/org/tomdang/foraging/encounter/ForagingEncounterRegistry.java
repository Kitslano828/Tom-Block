package org.tomdang.foraging.encounter;

import org.bukkit.block.Block;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ForagingEncounterRegistry {
	private final Map<String, ForagingEncounterDefinition> definitions = new LinkedHashMap<>();
	private final Map<String, ForagingEncounterDefinition> byNode = new LinkedHashMap<>();

	public void register(ForagingEncounterDefinition definition) {
		if (definitions.putIfAbsent(definition.id(), definition) != null) throw new IllegalArgumentException("Duplicate encounter " + definition.id());
		for (EncounterNode node : definition.nodes()) {
			if (byNode.putIfAbsent(node.key(definition.world()), definition) != null)
				throw new IllegalArgumentException("Overlapping encounter node " + node);
		}
	}

	public ForagingEncounterDefinition require(String id) {
		ForagingEncounterDefinition result = definitions.get(id.toUpperCase());
		if (result == null) throw new IllegalArgumentException("Unknown foraging encounter: " + id);
		return result;
	}

	public Optional<ForagingEncounterDefinition> at(Block block) {
		return Optional.ofNullable(byNode.get(new EncounterNode(block.getX(), block.getY(), block.getZ()).key(block.getWorld().getName())));
	}

	public Collection<ForagingEncounterDefinition> all() { return definitions.values(); }
}
