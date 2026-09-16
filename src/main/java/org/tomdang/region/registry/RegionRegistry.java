package org.tomdang.region.registry;

import org.tomdang.region.definition.RegionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RegionRegistry {
	private final Map<String, RegionDefinition> definitions = new LinkedHashMap<>();

	public void register(RegionDefinition definition) {
		registerAll(List.of(definition));
	}

	public void registerAll(Collection<RegionDefinition> newDefinitions) {
		if (newDefinitions == null) throw new IllegalArgumentException("newDefinitions cannot be null");

		Map<String, RegionDefinition> candidate = new LinkedHashMap<>(definitions);
		for (RegionDefinition definition : newDefinitions) {
			if (definition == null) {
				throw new IllegalArgumentException("newDefinitions cannot contain null entries");
			}
			if (candidate.putIfAbsent(definition.id(), definition) != null) {
				throw new IllegalArgumentException("Region is already registered: " + definition.id());
			}
		}

		validateParents(candidate);
		validateAcyclic(candidate);
		definitions.clear();
		definitions.putAll(candidate);
	}

	public Optional<RegionDefinition> find(String id) {
		if (id == null || id.isBlank()) return Optional.empty();
		return Optional.ofNullable(definitions.get(id.trim()));
	}

	public RegionDefinition require(String id) {
		return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown region: " + id));
	}

	public Collection<RegionDefinition> all() {
		return Collections.unmodifiableList(new ArrayList<>(definitions.values()));
	}

	private void validateParents(Map<String, RegionDefinition> candidate) {
		for (RegionDefinition definition : candidate.values()) {
			definition.parentId().ifPresent(parentId -> {
				if (!candidate.containsKey(parentId)) {
					throw new IllegalArgumentException(
							"Region " + definition.id() + " references unknown parent: " + parentId);
				}
			});
		}
	}

	private void validateAcyclic(Map<String, RegionDefinition> candidate) {
		Map<String, VisitState> states = new LinkedHashMap<>();
		for (String id : candidate.keySet()) {
			visit(id, candidate, states, new ArrayList<>());
		}
	}

	private void visit(String id, Map<String, RegionDefinition> candidate,
			Map<String, VisitState> states, List<String> path) {
		VisitState state = states.get(id);
		if (state == VisitState.VISITED) return;
		if (state == VisitState.VISITING) {
			path.add(id);
			throw new IllegalArgumentException("Region parent cycle detected: " + String.join(" -> ", path));
		}

		states.put(id, VisitState.VISITING);
		path.add(id);
		candidate.get(id).parentId().ifPresent(parentId -> visit(parentId, candidate, states, path));
		path.remove(path.size() - 1);
		states.put(id, VisitState.VISITED);
	}

	private enum VisitState {
		VISITING,
		VISITED
	}
}
