package org.tomdang.quest.definition;

import java.util.List;
import java.util.Map;

public record QuestStageDefinition(
		String id,
		String displayName,
		List<QuestObjectiveDefinition> objectives,
		String nextStageId,
		Map<String, String> branches
) {
	public QuestStageDefinition {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Stage id cannot be blank");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Stage display name cannot be blank");
		if (objectives == null || objectives.isEmpty()) throw new IllegalArgumentException("Stage requires at least one objective");
		if (objectives.stream().anyMatch(objective -> objective == null))
			throw new IllegalArgumentException("Stage objectives cannot contain null");
		if (objectives.stream().map(QuestObjectiveDefinition::id).distinct().count() != objectives.size())
			throw new IllegalArgumentException("Stage objective ids must be unique");
		if (nextStageId != null && nextStageId.isBlank()) throw new IllegalArgumentException("Next stage id cannot be blank");
		if (branches == null) throw new IllegalArgumentException("Stage branches cannot be null");
		if (branches.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
				|| entry.getValue() == null || entry.getValue().isBlank()))
			throw new IllegalArgumentException("Stage branches cannot contain blank choices or targets");
		if (nextStageId != null && !branches.isEmpty())
			throw new IllegalArgumentException("A stage cannot have both an automatic next stage and explicit branches");
		objectives = List.copyOf(objectives);
		branches = Map.copyOf(branches);
	}

	public boolean terminal() {
		return nextStageId == null && branches.isEmpty();
	}
}
