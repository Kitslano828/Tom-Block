package org.tomdang.quest.progress;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record QuestProgress(
		UUID playerId,
		String questId,
		QuestStatus status,
		String currentStageId,
		Map<QuestObjectiveProgressKey, Long> objectiveProgress,
		long revision,
		Instant startedAt,
		Instant updatedAt,
		Instant completedAt
) {
	public QuestProgress {
		if (playerId == null) throw new IllegalArgumentException("Player id cannot be null");
		if (questId == null || questId.isBlank()) throw new IllegalArgumentException("Quest id cannot be blank");
		if (status == null) throw new IllegalArgumentException("Quest status cannot be null");
		if (status == QuestStatus.ACTIVE && (currentStageId == null || currentStageId.isBlank()))
			throw new IllegalArgumentException("Active quest requires a current stage");
		if (objectiveProgress == null || objectiveProgress.entrySet().stream()
				.anyMatch(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0))
			throw new IllegalArgumentException("Objective progress is invalid");
		if (revision < 0) throw new IllegalArgumentException("Revision cannot be negative");
		if (startedAt == null || updatedAt == null) throw new IllegalArgumentException("Quest timestamps are required");
		if (status == QuestStatus.COMPLETED && completedAt == null)
			throw new IllegalArgumentException("Completed quest requires completedAt");
		objectiveProgress = Map.copyOf(objectiveProgress);
	}

	public static QuestProgress start(UUID playerId, String questId, String stageId, Instant now) {
		return new QuestProgress(playerId, questId, QuestStatus.ACTIVE, stageId, Map.of(), 0, now, now, null);
	}

	public long amount(String stageId, String objectiveId) {
		return objectiveProgress.getOrDefault(new QuestObjectiveProgressKey(stageId, objectiveId), 0L);
	}
}
