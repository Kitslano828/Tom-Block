package org.tomdang.quest.definition;

import java.util.Map;

public record QuestObjectiveDefinition(
		String id,
		QuestObjectiveType type,
		String target,
		long requiredAmount,
		boolean optional,
		Map<String, String> parameters
) {
	public QuestObjectiveDefinition {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Objective id cannot be blank");
		if (type == null) throw new IllegalArgumentException("Objective type cannot be null");
		if (target == null || target.isBlank()) throw new IllegalArgumentException("Objective target cannot be blank");
		if (requiredAmount <= 0) throw new IllegalArgumentException("Objective required amount must be positive");
		if (parameters == null) throw new IllegalArgumentException("Objective parameters cannot be null");
		parameters = Map.copyOf(parameters);
	}
}
