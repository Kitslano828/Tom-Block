package org.tomdang.collection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CollectionRegistry {
	private final Map<String, CollectionDefinition> definitions = new LinkedHashMap<>();
	public void register(CollectionDefinition definition) {
		if (definitions.putIfAbsent(definition.id(), definition) != null) throw new IllegalArgumentException("Duplicate collection: " + definition.id());
	}
	public Collection<CollectionDefinition> all() { return List.copyOf(definitions.values()); }
	public CollectionDefinition require(String id) {
		CollectionDefinition definition = definitions.get(id);
		if (definition == null) throw new IllegalArgumentException("Unknown collection: " + id);
		return definition;
	}
}
