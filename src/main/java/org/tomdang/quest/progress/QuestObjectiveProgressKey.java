package org.tomdang.quest.progress;

public record QuestObjectiveProgressKey(String stageId, String objectiveId) {
	public QuestObjectiveProgressKey {
		if (stageId == null || stageId.isBlank()) throw new IllegalArgumentException("Stage id cannot be blank");
		if (objectiveId == null || objectiveId.isBlank()) throw new IllegalArgumentException("Objective id cannot be blank");
	}
}
