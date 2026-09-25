package org.tomdang.quest.definition;

import java.util.List;
import java.util.Map;

public record QuestStageDefinition(
		String id,
		String displayName,
		List<QuestObjectiveDefinition> objectives,
		String nextStageId,
		Map<String, String> branches,
		List<QuestActionDefinition> enterActions,
		List<QuestActionDefinition> exitActions,
		List<QuestConditionDefinition> completionConditions
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
		if (enterActions == null || exitActions == null || completionConditions == null)
			throw new IllegalArgumentException("Stage orchestration definitions cannot be null");
		if (enterActions.stream().anyMatch(java.util.Objects::isNull)
				|| exitActions.stream().anyMatch(java.util.Objects::isNull)
				|| completionConditions.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("Stage orchestration definitions cannot contain null");
		if (java.util.stream.Stream.concat(enterActions.stream(), exitActions.stream())
				.map(QuestActionDefinition::id).distinct().count() != enterActions.size() + exitActions.size())
			throw new IllegalArgumentException("Stage action ids must be unique");
		if (completionConditions.stream().map(QuestConditionDefinition::id).distinct().count() != completionConditions.size())
			throw new IllegalArgumentException("Stage condition ids must be unique");
		if (branches.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
				|| entry.getValue() == null || entry.getValue().isBlank()))
			throw new IllegalArgumentException("Stage branches cannot contain blank choices or targets");
		if (nextStageId != null && !branches.isEmpty())
			throw new IllegalArgumentException("A stage cannot have both an automatic next stage and explicit branches");
		objectives = List.copyOf(objectives);
		branches = Map.copyOf(branches);
		enterActions = List.copyOf(enterActions);
		exitActions = List.copyOf(exitActions);
		completionConditions = List.copyOf(completionConditions);
	}

	public QuestStageDefinition(String id, String displayName, List<QuestObjectiveDefinition> objectives,
			String nextStageId, Map<String, String> branches) {
		this(id, displayName, objectives, nextStageId, branches, List.of(), List.of(), List.of());
	}

	public boolean terminal() {
		return nextStageId == null && branches.isEmpty();
	}
}
